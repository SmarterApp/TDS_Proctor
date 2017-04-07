package TDS.Proctor.services;

import TDS.Proctor.Services.remote.RemoteTestOpportunityService;
import TDS.Proctor.Sql.Data.Abstractions.AssessmentRepository;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.performance.dao.TestOpportunityExamMapDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.UUID;

import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_DENIED;
import static tds.exam.ExamStatusStage.IN_USE;

@RunWith(MockitoJUnitRunner.class)
public class RemoteTestOpportunityServiceTest {
    private RemoteTestOpportunityService service;

    @Mock
    private ITestOpportunityService legacyTestOpportunityService;

    @Mock
    private ExamRepository mockExamRepository;

    @Mock
    private AssessmentRepository mockAssessmentRepository;

    @Mock
    private TestOpportunityExamMapDao mockTestOpportunityExamMapDao;

    @Before
    public void setup() {
        service = new RemoteTestOpportunityService(legacyTestOpportunityService, true, true, mockExamRepository, mockAssessmentRepository, mockTestOpportunityExamMapDao);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldCallPendingApproval() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();

        service.getTestsForApproval(sessionId, proctorKey, browserKey);
        verify(legacyTestOpportunityService).getTestsForApproval(sessionId, proctorKey, browserKey);
        verify(mockExamRepository).findExamsPendingApproval(sessionId);
    }

    @Test
    public void shouldMapPendingApprovalToTestOpportunity() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();

        Exam examPendingApproval = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withAssessmentId("test-assessment-id")
            .withAssessmentKey("test-assessment-key")
            .withAttempts(2)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING), Instant.now())
            .withLoginSSID("ssid1")
            .withStudentName("student1")
            .withCustomAccommodation(true)
            .build();

        when(mockExamRepository.findExamsPendingApproval(sessionId)).thenReturn(Arrays.asList(examPendingApproval));

        TestOpps testOpps = service.getTestsForApproval(sessionId, proctorKey, browserKey);
        verify(legacyTestOpportunityService).getTestsForApproval(sessionId, proctorKey, browserKey);
        verify(mockExamRepository).findExamsPendingApproval(sessionId);

        assertThat(testOpps).hasSize(1);
        TestOpportunity testOpp = testOpps.get(0);
        assertThat(testOpp.getTestID()).isEqualTo(examPendingApproval.getAssessmentId());
        assertThat(testOpp.getTestKey()).isEqualTo(examPendingApproval.getAssessmentKey());
        assertThat(testOpp.getOpp()).isEqualTo(examPendingApproval.getAttempts());
        assertThat(testOpp.getStatus()).isEqualTo(examPendingApproval.getStatus().getCode());
        assertThat(testOpp.getSsid()).isEqualTo(examPendingApproval.getLoginSSID());
        assertThat(testOpp.getName()).isEqualTo(examPendingApproval.getStudentName());
        assertThat(testOpp.isCustAccs()).isEqualTo(examPendingApproval.isCustomAccommodations());
    }

    @Test
    public void shouldCallApproveExam() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();
        UUID legacyOpportunityId = UUID.randomUUID();

        when(mockTestOpportunityExamMapDao.getTestOpportunityId(examId)).thenReturn(legacyOpportunityId);

        service.approveOpportunity(examId, sessionId, proctorKey, browserKey);
        verify(legacyTestOpportunityService).approveOpportunity(legacyOpportunityId, sessionId, proctorKey, browserKey);
        verify(mockExamRepository).updateStatus(examId, STATUS_APPROVED, IN_USE.getType(), null);
    }

    @Test
    public void shouldCallDenyExam() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();
        UUID legacyOpportunityId = UUID.randomUUID();
        String reason = "some reason";

        when(mockTestOpportunityExamMapDao.getTestOpportunityId(examId)).thenReturn(legacyOpportunityId);

        service.denyOpportunity(examId, sessionId, proctorKey, browserKey, reason);
        verify(legacyTestOpportunityService).denyOpportunity(legacyOpportunityId, sessionId, proctorKey, browserKey, reason);
        verify(mockExamRepository).updateStatus(examId, STATUS_DENIED, IN_USE.getType(), reason);
    }

    @Test
    public void shouldCallApproveAccommodations() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();
        UUID legacyOpportunityId = UUID.randomUUID();
        String accs = "acc1;acc2";

        when(mockTestOpportunityExamMapDao.getTestOpportunityId(examId)).thenReturn(legacyOpportunityId);

        service.approveAccommodations(examId, sessionId, proctorKey, browserKey, 0, accs);
        service.approveAccommodations(examId, sessionId, browserKey, accs);

        verify(legacyTestOpportunityService).approveAccommodations(legacyOpportunityId, sessionId, proctorKey, browserKey, 0, accs);
        verify(mockExamRepository).approveAccommodations(isA(UUID.class), isA(ApproveAccommodationsRequest.class));
    }

    @Test
    public void shouldReturnTestOppsListForSessionId() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        // Exam 1
        Exam exam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withStudentName("Student name1")
            .withAttempts(2)
            .withAssessmentKey("assessmentKey1")
            .withAssessmentId("assessmentId1")
            .withLoginSSID("LOGINSSID1")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .withMaxItems(4)
            .withCustomAccommodation(false)
            .build();
        ExamAccommodation examAccommodation1 = new ExamAccommodation.Builder(UUID.randomUUID())
            .withType("Type One")
            .withValue("Accommodation Description 1 ")
            .build();
        ExamAccommodation examAccommodation2 = new ExamAccommodation.Builder(UUID.randomUUID())
            .withType("Type Two")
            .withValue("Accommodation Description 2")
            .build();
        ExpandableExam expandableExam1 = new ExpandableExam.Builder(exam)
            .withItemsResponseCount(3)
            .withMultiStageBraille(true)
            .withRequestCount(7)
            .withExamAccommodations(Arrays.asList(examAccommodation1, examAccommodation2))
            .build();

        Instant datePaused = DateTime.now().minusMinutes(5).toInstant();

        // Exam 2
        Exam pausedExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withId(UUID.randomUUID())
            .withStudentName("Student name2")
            .withAttempts(0)
            .withAssessmentKey("assessmentKey2")
            .withAssessmentId("assessmentId2")
            .withLoginSSID("LOGINSSID2")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), datePaused)
            .withMaxItems(4)
            .withCustomAccommodation(false)
            .build();
        ExamAccommodation examAccommodation3 = new ExamAccommodation.Builder(UUID.randomUUID())
            .withType("Type Three")
            .withValue("Accommodation Description 3")
            .build();
        ExpandableExam pausedExpandaleExam = new ExpandableExam.Builder(pausedExam)
            .withItemsResponseCount(4)
            .withMultiStageBraille(false)
            .withRequestCount(0)
            .withExamAccommodations(Arrays.asList(examAccommodation3))
            .build();

        when(mockExamRepository.findExamsForSessionId(sessionId)).thenReturn(Arrays.asList(expandableExam1, pausedExpandaleExam));
        TestOpps testOpps = service.getCurrentSessionTestees(sessionId, 1234, UUID.randomUUID());
        assertThat(testOpps).hasSize(2);
        TestOpportunity testOpp1 = testOpps.get(0);
        assertThat(testOpp1.getName()).isEqualTo(exam.getStudentName());
        assertThat(testOpp1.getOppKey()).isEqualTo(exam.getId());
        assertThat(testOpp1.getOpp()).isEqualTo(exam.getAttempts());
        assertThat(testOpp1.getDisplayStatus()).isEqualTo("started, 3/4");
        assertThat(testOpp1.getItemcount()).isEqualTo(exam.getMaxItems());
        assertThat(testOpp1.getRequestCount()).isEqualTo(expandableExam1.getRequestCount());
        assertThat(testOpp1.isMsb()).isEqualTo(expandableExam1.isMultiStageBraille());
        assertThat(testOpp1.getSsid()).isEqualTo(exam.getLoginSSID());
        assertThat(testOpp1.getTestID()).isEqualTo(exam.getAssessmentId());
        assertThat(testOpp1.getTestKey()).isEqualTo(exam.getAssessmentKey());
        assertThat(testOpp1.getResponseCount()).isEqualTo(expandableExam1.getItemsResponseCount());
        assertThat(testOpp1.getTestName()).isEqualTo(exam.getAssessmentId());
        assertThat(testOpp1.getAccs()).isEqualTo("Type One: Accommodation Description 1  | Type Two: Accommodation Description 2");

        TestOpportunity testOpp2 = testOpps.get(1);
        assertThat(testOpp2.getName()).isEqualTo(pausedExam.getStudentName());
        assertThat(testOpp2.getOppKey()).isEqualTo(pausedExam.getId());
        assertThat(testOpp2.getOpp()).isEqualTo(pausedExam.getAttempts());
        assertThat(testOpp2.getDisplayStatus()).isEqualTo(String.format("paused, 4/4, %s min", 5));
        assertThat(testOpp2.getItemcount()).isEqualTo(pausedExam.getMaxItems());
        assertThat(testOpp2.getRequestCount()).isEqualTo(pausedExpandaleExam.getRequestCount());
        assertThat(testOpp2.isMsb()).isEqualTo(pausedExpandaleExam.isMultiStageBraille());
        assertThat(testOpp2.getSsid()).isEqualTo(pausedExam.getLoginSSID());
        assertThat(testOpp2.getTestID()).isEqualTo(pausedExam.getAssessmentId());
        assertThat(testOpp2.getTestKey()).isEqualTo(pausedExam.getAssessmentKey());
        assertThat(testOpp2.getResponseCount()).isEqualTo(pausedExpandaleExam.getItemsResponseCount());
        assertThat(testOpp2.getTestName()).isEqualTo(pausedExam.getAssessmentId());
        assertThat(testOpp2.getAccs()).isEqualTo("Type Three: Accommodation Description 3");
    }
}

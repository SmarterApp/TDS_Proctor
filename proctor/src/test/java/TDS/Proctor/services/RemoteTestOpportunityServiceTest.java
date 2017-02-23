package TDS.Proctor.services;

import TDS.Proctor.Services.remote.RemoteTestOpportunityService;
import TDS.Proctor.Sql.Data.Abstractions.AssessmentRepository;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.performance.dao.TestOpportunityExamMapDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.ApproveAccommodationsRequest;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
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
    public void shouldCallApproveExam() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();

        service.approveOpportunity(examId, sessionId, proctorKey, browserKey);
        verify(legacyTestOpportunityService).approveOpportunity(examId, sessionId, proctorKey, browserKey);
        verify(mockExamRepository).updateStatus(examId, STATUS_APPROVED, IN_USE.getType(), null);
    }

    @Test
    public void shouldCallDenyExam() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();
        String reason = "some reason";

        service.denyOpportunity(examId, sessionId, proctorKey, browserKey, reason);
        verify(legacyTestOpportunityService).denyOpportunity(examId, sessionId, proctorKey, browserKey, reason);
        verify(mockExamRepository).updateStatus(examId, STATUS_DENIED, IN_USE.getType(), reason);
    }

    @Test
    public void shouldCallApproveAccommodations() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Long proctorKey = 99L;
        UUID browserKey = UUID.randomUUID();
        String accs = "acc1;acc2";

        service.approveAccommodations(examId, sessionId, proctorKey, browserKey, 0, accs);
        service.approveAccommodations(examId, sessionId, browserKey, accs);

        verify(legacyTestOpportunityService).approveAccommodations(examId, sessionId, proctorKey, browserKey, 0, accs);
        verify(mockExamRepository).approveAccommodations(isA(UUID.class), isA(ApproveAccommodationsRequest.class));
    }
}

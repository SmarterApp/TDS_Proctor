package TDS.Proctor.Services;

import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.Sql.Data.Accommodations.AccType;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccValue;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;

public class TestOpportunityRestService implements ITestOpportunityService {
    private static final Logger logger = LoggerFactory.getLogger(TestOpportunityRestService.class);

    private final TestOpportunityService testOpportunityService;
    private final RestTemplate restTemplate;
    private final UriComponentsBuilder examsBuilder;
    private final UriComponentsBuilder assessmentAccommodationsBuilder;
    private final UriComponentsBuilder examAccommodationsBuilder;
    private final UriComponentsBuilder approveAccommodationsBuilder;
    private final UriComponentsBuilder approveExamBuilder;
    private final UriComponentsBuilder denyExamBuilder;
    private final boolean legacyEnabled;
    private final boolean restEnabled;


    @Autowired
    public TestOpportunityRestService(final TestOpportunityService testOpportunityService,
                                      final RestTemplate restTemplate,
                                      final String examUrl,
                                      final String assessmentUrl,
                                      final boolean legacyEnabled,
                                      final boolean restEnabled) {
        this.testOpportunityService = testOpportunityService;
        this.restTemplate = restTemplate;
        examsBuilder = UriComponentsBuilder.fromUriString(examUrl);
        examsBuilder.pathSegment("pending-approval");
        examsBuilder.pathSegment("{sessionId}");

        assessmentAccommodationsBuilder = UriComponentsBuilder.fromUriString(assessmentUrl);
        assessmentAccommodationsBuilder.pathSegment("{clientName}");
        assessmentAccommodationsBuilder.pathSegment("assessments");
        assessmentAccommodationsBuilder.pathSegment("accommodations");
        assessmentAccommodationsBuilder.query("assessmentKey={assessmentKey}");

        examAccommodationsBuilder = UriComponentsBuilder.fromUriString(examUrl);
        examAccommodationsBuilder.pathSegment("{examId}");
        examAccommodationsBuilder.pathSegment("accommodations");

        approveAccommodationsBuilder = UriComponentsBuilder.fromUriString(examUrl);
        approveAccommodationsBuilder.pathSegment("{examId}");
        approveAccommodationsBuilder.pathSegment("accommodations");

        approveExamBuilder = UriComponentsBuilder.fromUriString(examUrl);
        approveExamBuilder.pathSegment("approve");
        approveExamBuilder.pathSegment("{examId}");

        denyExamBuilder = UriComponentsBuilder.fromUriString(examUrl);
        denyExamBuilder.pathSegment("deny");
        denyExamBuilder.pathSegment("{examId}");

        this.legacyEnabled = legacyEnabled;
        this.restEnabled = restEnabled;
    }

    @Override
    public TestOpps getCurrentSessionTestees(UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        return testOpportunityService.getCurrentSessionTestees(sessionKey, proctorKey, browserKey);
    }

    @Override
    public TestOpps getTestsForApproval(UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        logger.debug("session-id: {}", sessionKey.toString());
        logger.debug("browser-id: {}", browserKey.toString());

        if (legacyEnabled && restEnabled) {
            final TestOpps legacyTestOpps = testOpportunityService.getTestsForApproval(sessionKey, proctorKey, browserKey);
            final TestOpps testOpps = getTestsForApproval(sessionKey);
            // TODO compare legacy and rest responses
            return testOpps;
        } else if (legacyEnabled && !restEnabled) {
            return testOpportunityService.getTestsForApproval(sessionKey, proctorKey, browserKey);
        } else {
            // case legacyEnabled == false
            return getTestsForApproval(sessionKey);
        }
    }

    private TestOpps getTestsForApproval(UUID sessionKey) throws ReturnStatusException {
        final String examsUrl = examsBuilder.buildAndExpand(sessionKey.toString()).toUriString();
        final Exam[] exams = restTemplate.getForObject(examsUrl, Exam[].class);

        final TestOpps testOpps = new TestOpps();
        for (final Exam exam : exams) {
            final TestOpportunity testOpportunity = new TestOpportunity(exam.getId());
            testOpportunity.setTestID(exam.getAssessmentId());
            testOpportunity.setOpp(exam.getAttempts());
            testOpportunity.setTestKey(exam.getAssessmentKey());
            testOpportunity.setStatus(exam.getStatus().getCode());
            testOpportunity.setSsid(exam.getLoginSSID());
            testOpportunity.setName(exam.getStudentName());

            final String assessmentAccommodationsUrl = assessmentAccommodationsBuilder.buildAndExpand(exam.getClientName(), exam.getAssessmentKey()).toUriString();
            final String examAccommodationsUrl = examAccommodationsBuilder.buildAndExpand(exam.getId().toString()).toUriString();
            final Accommodation[] assessmentAccommodations = restTemplate.getForObject(assessmentAccommodationsUrl, Accommodation[].class);
            final ExamAccommodation[] examAccommodations = restTemplate.getForObject(examAccommodationsUrl, ExamAccommodation[].class);

            final Map<String, ExamAccommodation> examAccommodationsMap = new HashMap<>();
            for (final ExamAccommodation examAccommodation : examAccommodations) {
                examAccommodationsMap.put(examAccommodation.getCode(), examAccommodation);
            }

            final AccTypes accTypes = new AccTypes();
            for (final Accommodation assessmentAccommodation : assessmentAccommodations) {
                String code = assessmentAccommodation.getCode();
                if (examAccommodationsMap.containsKey(code)) {
                    ExamAccommodation examAccommodation = examAccommodationsMap.get(code);
                    if (examAccommodation.getType().equals(assessmentAccommodation.getType())) {
                        final List<AccValue> values;
                        final String accTypeKey = assessmentAccommodation.getType();
                        AccType accType = accTypes.get(accTypeKey);
                        // accValues are grouped by accTypeKey and added to accType
                        if (accType == null) {
                            accType = new AccType(accTypeKey);
                            values = new ArrayList<>();
                            accType.setSelectable(assessmentAccommodation.isSelectable());
                            accType.setAllowChange(assessmentAccommodation.isAllowChange());
                            accType.setVisible(assessmentAccommodation.isVisible());
                            accType.setSortOrder(assessmentAccommodation.getToolTypeSortOrder());
                            accType.setDependOnType(assessmentAccommodation.getDependsOnToolType());
                        } else {
                            values = accType.getValues();
                        }
                        final AccValue accValue = new AccValue(assessmentAccommodation.getValue(),
                            assessmentAccommodation.getCode(), true);

                        values.add(accValue);
                        accType.setValues(values);

                        accTypes.put(assessmentAccommodation.getType(), accType);
                    }
                }
            }
            testOpportunity.setAccTypesList(Arrays.asList(accTypes));
            testOpps.add(testOpportunity);
        }
        return testOpps;
    }

    @Override
    public boolean approveOpportunity(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        return testOpportunityService.approveOpportunity(oppKey, sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean approveAccommodations(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException {
        return testOpportunityService.approveAccommodations(oppKey, sessionKey, proctorKey, browserKey, segment, segmentAccs);
    }

    @Override
    public boolean denyOpportunity(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException {
        return testOpportunityService.denyOpportunity(oppKey, sessionKey, proctorKey, browserKey, reason);
    }

    @Override
    public boolean pauseOpportunity(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        return testOpportunityService.pauseOpportunity(oppKey, sessionKey, proctorKey, browserKey);
    }
}

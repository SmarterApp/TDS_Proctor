package TDS.Proctor.Services.remote;

import TDS.Proctor.Services.TestOpportunityService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import tds.accommodation.Accommodation;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;

import static java.util.regex.Pattern.compile;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_DENIED;
import static tds.exam.ExamStatusStage.IN_USE;

public class RemoteTestOpportunityService implements ITestOpportunityService {
    private static final Logger logger = LoggerFactory.getLogger(RemoteTestOpportunityService.class);

    private final TestOpportunityService testOpportunityService;
    private final RestTemplate restTemplate;
    private final UriComponentsBuilder examsBuilder;
    private final UriComponentsBuilder assessmentAccommodationsBuilder;
    private final UriComponentsBuilder examAccommodationsBuilder;
    private final UriComponentsBuilder approveAccommodationsBuilder;
    private final UriComponentsBuilder updateExamStatusBuilder;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;


    private static Pattern accommodationPattern = compile(Pattern.quote("|"));
    private static Pattern segmentPattern = compile(";");

    @Autowired
    public RemoteTestOpportunityService(final TestOpportunityService testOpportunityService,
                                      final RestTemplate restTemplate,
                                      @Value("${tds.exam.remote.url}") final String examUrl,
                                      @Value("${tds.assessment.remote.url}") final String assessmentUrl,
                                      @Value("${tds.exam.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                      @Value("${tds.exam.remote.enabled}") final boolean isRemoteCallsEnabled) {
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

        updateExamStatusBuilder = UriComponentsBuilder.fromUriString(examUrl);
        updateExamStatusBuilder.pathSegment("{examId}");
        updateExamStatusBuilder.pathSegment("status");
        updateExamStatusBuilder.queryParam("status", "{status}");
        updateExamStatusBuilder.queryParam("stage", "{stage}");
        updateExamStatusBuilder.queryParam("reason", "{reason}");

        this.isLegacyCallsEnabled = isLegacyCallsEnabled;
        this.isRemoteCallsEnabled = isRemoteCallsEnabled;
    }

    @Override
    public TestOpps getCurrentSessionTestees(UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        return testOpportunityService.getCurrentSessionTestees(sessionKey, proctorKey, browserKey);
    }

    @Override
    public TestOpps getTestsForApproval(UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        logger.debug("session-id: {}", sessionKey.toString());
        logger.debug("browser-id: {}", browserKey.toString());

        TestOpps testOpps = null;

        if (isLegacyCallsEnabled) {
            testOpps = testOpportunityService.getTestsForApproval(sessionKey, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return testOpps;
        }

        return getTestsForApproval(sessionKey);
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
        boolean isApproveSuccessful = false;

        if (isLegacyCallsEnabled) {
            isApproveSuccessful = testOpportunityService.approveOpportunity(oppKey, sessionKey, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return isApproveSuccessful;
        }

        approveExam(oppKey);
        return true;
    }

    @Override
    public boolean denyOpportunity(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException {
        boolean isDenySuccessful = false;

        if (isLegacyCallsEnabled) {
            isDenySuccessful = testOpportunityService.denyOpportunity(oppKey, sessionKey, proctorKey, browserKey, reason);
        }

        if (!isRemoteCallsEnabled) {
            return isDenySuccessful;
        }

        denyExam(oppKey, reason);
        return true;
    }

    private void approveExam(UUID examId) throws ReturnStatusException {
        final String approveExamUrl = updateExamStatusBuilder.buildAndExpand(examId.toString(), STATUS_APPROVED, IN_USE.getType(), "").toUriString();
        restTemplate.put(approveExamUrl, null);
    }

    private void denyExam(UUID examId, String reason) throws ReturnStatusException {
        final String denyExamUrl = updateExamStatusBuilder.buildAndExpand(examId.toString(), STATUS_DENIED, IN_USE.getType(), reason).toUriString();

        restTemplate.put(denyExamUrl, null);
    }

    // legacy implementation of approveAccommodations
    // called once per segment
    @Override
    public boolean approveAccommodations(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException {
        if (isLegacyCallsEnabled) {
            return testOpportunityService.approveAccommodations(oppKey, sessionKey, proctorKey, browserKey, segment, segmentAccs);
        }
        return true;
    }

    // rest implementation of approveAccommodations
    // called once per examination
    @Override
    public void approveAccommodations(UUID examId, UUID sessionKey, UUID browserKey, String accommodationsString) throws ReturnStatusException {
        if (isRemoteCallsEnabled) {
            Map<Integer, Set<String>> accommodations = parseAccommodations(accommodationsString);
            approveAccommodations(examId, sessionKey, browserKey, accommodations);
        }
    }

    private void approveAccommodations(UUID examId, UUID sessionKey, UUID browserKey, Map<Integer, Set<String>> accommodations) {
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionKey, browserKey, accommodations);
        final String approveAccommodationsUrl = approveAccommodationsBuilder.buildAndExpand(examId.toString()).toUriString();
        restTemplate.postForObject(approveAccommodationsUrl, request, ApproveAccommodationsRequest.class);
    }


    @Override
    public boolean pauseOpportunity(UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
        return testOpportunityService.pauseOpportunity(oppKey, sessionKey, proctorKey, browserKey);
    }

    public static Map<Integer, Set<String>> parseAccommodations(final String accommodationsString) {
        final Map<Integer, Set<String>> accommodations = new HashMap<>();

        // remove last character from accommodationsString. (extra at end ';')
        final String accommodationsString2 = accommodationsString.substring(0, accommodationsString.length () - 1);
        final String[] segmentAccommodationsArray = segmentPattern.split(accommodationsString2);

        // segments are one-based array
        // zero index indicates accommodations applies to all segments
        int segmentIndex = 0;
        for(String segmentString: segmentAccommodationsArray) {
            final Set<String> segmentAccommodationsSet = new HashSet<>(Arrays.asList(accommodationPattern.split(segmentString))); //ImmutableSet.copyOf(accommodationPattern.split(segmentString));
            accommodations.put(segmentIndex, segmentAccommodationsSet);
        }

        return accommodations;
    }
}

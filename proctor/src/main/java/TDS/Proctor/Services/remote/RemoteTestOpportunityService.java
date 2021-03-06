/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package TDS.Proctor.Services.remote;

import TDS.Proctor.Sql.Data.Abstractions.AssessmentRepository;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.Sql.Data.Accommodations.AccType;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccValue;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Proctor.performance.dao.TestOpportunityExamMapDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;

import static java.util.regex.Pattern.compile;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_DENIED;
import static tds.exam.ExamStatusStage.IN_USE;

public class RemoteTestOpportunityService implements ITestOpportunityService {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteTestOpportunityService.class);

    private final ITestOpportunityService testOpportunityService;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;
    private final ExamRepository examRepository;
    private final AssessmentRepository assessmentRepository;
    private final TestOpportunityExamMapDao testOpportunityExamMapDao;
    private final ProctorUserDao proctorUserDao;

    private static Pattern accommodationPattern = compile(Pattern.quote("|"));
    private static Pattern segmentPattern = compile(";");

    @Autowired
    public RemoteTestOpportunityService(final ITestOpportunityService testOpportunityService,
                                        @Value("${tds.exam.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                        @Value("${tds.exam.remote.enabled}") final boolean isRemoteCallsEnabled,
                                        final ExamRepository examRepository,
                                        final AssessmentRepository assessmentRepository,
                                        final TestOpportunityExamMapDao testOpportunityExamMapDao,
                                        final ProctorUserDao proctorUserDao) {

        if (!isRemoteCallsEnabled && !isLegacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
        }

        this.testOpportunityService = testOpportunityService;
        this.examRepository = examRepository;
        this.assessmentRepository = assessmentRepository;
        this.testOpportunityExamMapDao = testOpportunityExamMapDao;
        this.proctorUserDao = proctorUserDao;
        this.isLegacyCallsEnabled = isLegacyCallsEnabled;
        this.isRemoteCallsEnabled = isRemoteCallsEnabled;
    }

    @Override
    public TestOpps getCurrentSessionTestees(final UUID sessionId, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        TestOpps testOpps = null;

        if (isLegacyCallsEnabled) {
            testOpps = testOpportunityService.getCurrentSessionTestees(sessionId, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return testOpps;
        }

        return mapExpandableExamsToTestOpps(examRepository.findExamsForSessionId(sessionId));
    }

    @Override
    public TestOpps getTestsForApproval(final UUID sessionId, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        TestOpps testOpps = null;

        if (isLegacyCallsEnabled) {
            testOpps = testOpportunityService.getTestsForApproval(sessionId, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return testOpps;
        }

        return getTestsForApproval(sessionId);
    }

    private TestOpps getTestsForApproval(final UUID sessionId) throws ReturnStatusException {
        final List<Exam> exams = examRepository.findExamsPendingApproval(sessionId);

        final TestOpps testOpps = new TestOpps();
        for (final Exam exam : exams) {
            final TestOpportunity testOpportunity = new TestOpportunity(exam.getId());
            testOpportunity.setTestID(exam.getAssessmentId());
            testOpportunity.setOpp(exam.getAttempts());
            testOpportunity.setTestKey(exam.getAssessmentKey());
            testOpportunity.setStatus(exam.getStatus().getCode());
            testOpportunity.setSsid(exam.getLoginSSID());
            testOpportunity.setName(exam.getStudentName());
            testOpportunity.setCustAccs(exam.isCustomAccommodations());

            final List<Accommodation> assessmentAccommodations = assessmentRepository.findAccommodations(exam.getClientName(), exam.getAssessmentKey());
            final List<ExamAccommodation> examAccommodations = examRepository.findAllAccommodations(exam.getId());

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
    public boolean approveOpportunity(final UUID examId, final UUID sessionId, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        boolean isApproveSuccessful = false;

        if (isLegacyCallsEnabled) {
            isApproveSuccessful = testOpportunityService.approveOpportunity(getTestOpportunityId(examId), sessionId, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return isApproveSuccessful;
        }

        Optional<ValidationError> maybeError = examRepository.updateStatus(examId, STATUS_APPROVED, IN_USE.getType(), null);

        //Consistent with the legacy implementation.  If it fails for any reason it throws.
        if(maybeError.isPresent()) {
            if (maybeError.get().getCode().equals("badStatusTransition")) {
                // See TestSessionServiceImpl.java line 388 - failed update status call should result in a customized message.
                final String error = "This student is no longer awaiting approval.";
                LOG.warn("Unable to approve opportunity: {}", error);
                throw new ReturnStatusException(error);
            }

            LOG.warn("Unable to approve opportunity: {}", maybeError.get().getMessage());
            throw new ReturnStatusException(maybeError.get().getMessage());
        }

        return true;
    }

    @Override
    public boolean denyOpportunity(final UUID examId, final UUID sessionId, final long proctorKey, final UUID browserKey, final String reason) throws ReturnStatusException {
        boolean isDenySuccessful = false;

        if (isLegacyCallsEnabled) {
            try {
                isDenySuccessful = testOpportunityService.denyOpportunity(getTestOpportunityId(examId), sessionId, proctorKey, browserKey, reason);
            } catch (ReturnStatusException rse) {
                // the legacy normal flow throws the exception
                //  so if we are also calling the remote services then we need to swallow it
                if (!isRemoteCallsEnabled) {
                    throw rse;
                }
            }
        }

        if (!isRemoteCallsEnabled) {
            return isDenySuccessful;
        }

        Optional<ValidationError> maybeError = examRepository.updateStatus(examId, STATUS_DENIED, IN_USE.getType(), reason);

        //Consistent with the legacy implementation.  If it fails for any reason it throws.
        if(maybeError.isPresent()) {
            if (maybeError.get().getCode().equals("badStatusTransition")) {
                // See TestSessionServiceImpl.java line 388 - failed update status call should result in a customized message.
                final String error = "This student is no longer awaiting approval.";
                LOG.warn("Unable to approve opportunity: {}", error);
                throw new ReturnStatusException(error);
            }

            LOG.warn("Unable to deny opportunity: {}", maybeError.get().getMessage());
            throw new ReturnStatusException(maybeError.get().getMessage());
        }

        return true;
    }

    // legacy implementation of approveAccommodations
    // called once per segment
    @Override
    public boolean approveAccommodations(final UUID examId, final UUID sessionId, final long proctorKey, final UUID browserKey, final int segment, final String segmentAccs) throws ReturnStatusException {
        if (isLegacyCallsEnabled) {
            return testOpportunityService.approveAccommodations(getTestOpportunityId(examId), sessionId, proctorKey, browserKey, segment, segmentAccs);
        }

        return true;
    }

    // rest implementation of approveAccommodations
    // called once per examination
    @Override
    public void approveAccommodations(final UUID examId, final UUID sessionId, final UUID browserKey, final String accommodationsString) throws ReturnStatusException {
        if (isRemoteCallsEnabled) {
            Map<Integer, Set<String>> accommodations = parseAccommodations(accommodationsString);
            ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionId, browserKey, accommodations);

            examRepository.approveAccommodations(examId, request);
        }
    }

    @Override
    public boolean pauseOpportunity(final UUID examId, final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        boolean successful = false;

        if (isLegacyCallsEnabled) {
            final UUID opportunityId = testOpportunityExamMapDao.getTestOpportunityId(examId);
            successful = testOpportunityService.pauseOpportunity(opportunityId, sessionKey, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return successful;
        }

        final String validationError = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (validationError != null) {
            throw new ReturnStatusException(validationError);
        }

        examRepository.pauseExam(examId);

        return true;
    }

    private static Map<Integer, Set<String>> parseAccommodations(final String accommodationsString) {
        final Map<Integer, Set<String>> accommodations = new HashMap<>();

        // remove last character from accommodationsString. (extra at end ';')
        final String accommodationsString2 = accommodationsString.substring(0, accommodationsString.length() - 1);
        final String[] segmentAccommodationsArray = segmentPattern.split(accommodationsString2);

        // segments are one-based array
        // zero index indicates accommodations applies to all segments
        int segmentIndex = 0;
        for (String segmentString : segmentAccommodationsArray) {
            final Set<String> segmentAccommodationsSet = new HashSet<>(Arrays.asList(accommodationPattern.split(segmentString))); //ImmutableSet.copyOf(accommodationPattern.split(segmentString));
            accommodations.put(segmentIndex, segmentAccommodationsSet);
        }

        return accommodations;
    }

    private UUID getTestOpportunityId(UUID examId) {
        if (!isRemoteCallsEnabled) return examId;

        return testOpportunityExamMapDao.getTestOpportunityId(examId);
    }

    /* TestOpportunityRepository.java loadCurrentSessionTestees() */
    private static TestOpps mapExpandableExamsToTestOpps(final List<ExpandableExam> exams) {
        TestOpps testOpportunities = new TestOpps();

        for (ExpandableExam expandableExam : exams) {
            final Exam exam = expandableExam.getExam();
            final String examStatus = exam.getStatus().getCode();
            final int responseCount = expandableExam.getItemsResponseCount();

            TestOpportunity opportunity = new TestOpportunity(exam.getId());
            opportunity.setName(exam.getStudentName());
            opportunity.setOpp(exam.getAttempts());
            opportunity.setTestKey(exam.getAssessmentKey());
            opportunity.setSsid(exam.getLoginSSID());
            opportunity.setStatus(examStatus);
            opportunity.setTestID(exam.getAssessmentId());
            opportunity.setTestName(exam.getAssessmentId()); // TestOpportunityRepository line 99 sets testName to testId
            opportunity.setItemcount(exam.getMaxItems());
            opportunity.setResponseCount(responseCount);
            opportunity.setIsMsb(expandableExam.isMultiStageBraille());
            opportunity.setRequestCount(expandableExam.getRequestCount());
            opportunity.setAccs(buildAccommodationStringFromExamAccommodations(expandableExam.getExamAccommodations()));

            // Skip first conditional (getScore() != null) - score is always null
            if (exam.getCompletedAt() == null) {
                final String pausedString = ExamStatusCode.STATUS_PAUSED.equals(examStatus)
                    ? String.format(", %s min", Minutes.minutesBetween(exam.getStatusChangedAt(), Instant.now()).getMinutes())
                    : StringUtils.EMPTY;

                opportunity.setDisplayStatus(String.format("%s, %d/%d%s", examStatus, responseCount, exam.getMaxItems(), pausedString));
            } else {
                opportunity.setDisplayStatus(examStatus);
            }

            opportunity.setCustAccs(exam.isCustomAccommodations());

            testOpportunities.add(opportunity);
        }

        return testOpportunities;
    }

    private static String buildAccommodationStringFromExamAccommodations(final List<ExamAccommodation> examAccommodations) {
        /* Accommodation String Syntax:
            <accType1>: <accValue1> | <accType2>: <accValue2> | ...
         */
        StringBuilder builder = new StringBuilder();
        ExamAccommodation examAccommodation;

        for (int i = 0; i < examAccommodations.size(); ++i) {
            examAccommodation = examAccommodations.get(i);
            builder
                .append(examAccommodation.getType())
                .append(": ")
                .append(examAccommodation.getValue());

            // If this is not the last element, add a pipe delimiter
            if (i != examAccommodations.size() - 1) {
                builder.append(" | ");
            }
        }

        return builder.toString();
    }

}

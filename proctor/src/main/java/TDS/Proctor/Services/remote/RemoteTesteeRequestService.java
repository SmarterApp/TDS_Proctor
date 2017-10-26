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

import TDS.Proctor.Sql.Data.Abstractions.ExamPrintRequestRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestService;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;

public class RemoteTesteeRequestService implements ITesteeRequestService {
    private final ITesteeRequestService legacyTesteeRequestService;
    private final ExamPrintRequestRepository examPrintRequestRepository;
    private final ProctorUserDao proctorUserDao;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;

    @Autowired
    public RemoteTesteeRequestService(final ITesteeRequestService testeeRequestService,
                                      final ExamPrintRequestRepository examPrintRequestRepository,
                                      @Value("${tds.exam.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                      @Value("${tds.exam.remote.enabled}") final boolean isRemoteCallsEnabled,
                                      final ProctorUserDao proctorUserDao) {

        if (!isRemoteCallsEnabled && !isLegacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
        }

        this.legacyTesteeRequestService = testeeRequestService;
        this.examPrintRequestRepository = examPrintRequestRepository;
        this.isLegacyCallsEnabled = isLegacyCallsEnabled;
        this.isRemoteCallsEnabled = isRemoteCallsEnabled;
        this.proctorUserDao = proctorUserDao;
    }

    @Override
    public TesteeRequests getCurrentTesteeRequests(final UUID opportunityKey, final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        TesteeRequests requests = null;

        if (isLegacyCallsEnabled) {
            requests = legacyTesteeRequestService.getCurrentTesteeRequests(opportunityKey, sessionKey, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return requests;
        }

        validateAccessAndThrowIfDenied(sessionKey, proctorKey, browserKey);

        return mapPrintRequestsToTesteeRequests(examPrintRequestRepository.findUnfulfilledRequests(opportunityKey, sessionKey));
    }

    private void validateAccessAndThrowIfDenied(final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        final String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (accessDenied != null) {
            throw new ReturnStatusException(accessDenied);
        }
    }

    @Override
    public TesteeRequests getApprovedTesteeRequests(final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException {
        TesteeRequests requests = null;

        if (isLegacyCallsEnabled) {
            requests = legacyTesteeRequestService.getApprovedTesteeRequests(sessionKey, proctorKey, browserKey);
        }

        if (!isRemoteCallsEnabled) {
            return requests;
        }

        validateAccessAndThrowIfDenied(sessionKey, proctorKey, browserKey);

        return mapPrintRequestsToTesteeRequests(examPrintRequestRepository.findApprovedRequests(sessionKey));
    }

    @Override
    public TesteeRequest getTesteeRequestValues(final UUID sessionKey, final long proctorKey, final UUID browserKey, final UUID requestKey, final boolean markFulfilled) throws ReturnStatusException {
        TesteeRequest testeeRequest = null;

        if (isLegacyCallsEnabled) {
            testeeRequest = legacyTesteeRequestService.getTesteeRequestValues(sessionKey, proctorKey, browserKey, requestKey, markFulfilled);
        }

        if (!isRemoteCallsEnabled) {
            return testeeRequest;
        }

        validateAccessAndThrowIfDenied(sessionKey, proctorKey, browserKey);

        ExpandableExamPrintRequest request = examPrintRequestRepository.findRequestAndApprove(requestKey);

        // markFulfilled is always "true" as used in the legacy app
        return mapExpandablePrintRequestToTesteeRequest(request);
    }

    @Override
    public boolean denyTesteeRequest(final UUID sessionKey, final long proctorKey, final UUID browserKey, final UUID requestKey, final String reason) throws ReturnStatusException {
        boolean successful = false;

        if (isLegacyCallsEnabled) {
            successful = legacyTesteeRequestService.denyTesteeRequest(sessionKey, proctorKey, browserKey, requestKey, reason);
        }

        if (!isRemoteCallsEnabled) {
            return successful;
        }

        validateAccessAndThrowIfDenied(sessionKey, proctorKey, browserKey);

        examPrintRequestRepository.denyPrintRequest(requestKey, reason);

        return true;
    }

    @Override
    public void convertDates(final TesteeRequests testeeRequests, final int timeZoneOffset) {
        legacyTesteeRequestService.convertDates(testeeRequests, timeZoneOffset);
    }

    /* Port of TesteeRequestRepository.loadTesteeRequests() */
    private TesteeRequests mapPrintRequestsToTesteeRequests(final List<ExamPrintRequest> printRequests) {
        TesteeRequests testeeRequests = new TesteeRequests();

        for (ExamPrintRequest printRequest : printRequests) {
            TesteeRequest testeeRequest = mapPrintRequestToTesteeRequest(printRequest);
            testeeRequests.add(testeeRequest);
        }

        return testeeRequests;
    }

    private TesteeRequest mapExpandablePrintRequestToTesteeRequest(final ExpandableExamPrintRequest expandableExamPrintRequest) {
        ExamPrintRequest printRequest = expandableExamPrintRequest.getExamPrintRequest();
        TesteeRequest testeeRequest = mapPrintRequestToTesteeRequest(printRequest);

        Exam exam = expandableExamPrintRequest.getExam();
        /* TesteeRequestRepository.java - line 95 */
        // Exam-specific data for request details
        if (exam != null) {
            testeeRequest.setTestID(exam.getAssessmentId());
            testeeRequest.setTesteeKey(exam.getStudentId());
            testeeRequest.setTesteeName(exam.getStudentName());
            /* In ProctorDLL.java, lines [1698-1699] and [1790-1791], these two properties are set to the same "lang" variable */
            testeeRequest.setLanguage(exam.getLanguageCode());
            testeeRequest.setAccCode(exam.getLanguageCode());
            testeeRequest.setRequestParameters(printRequest.getParameters());
            testeeRequest.setOpportunity(exam.getAttempts());
            testeeRequest.setTesteeID(exam.getLoginSSID());

            if (printRequest.getType().equals(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)) {
                testeeRequest.setItemResponse(printRequest.getItemResponse());
            }
        }

        return testeeRequest;
    }

    private TesteeRequest mapPrintRequestToTesteeRequest(final ExamPrintRequest printRequest) {
        TesteeRequest testeeRequest = new TesteeRequest();
        testeeRequest.setKey(printRequest.getId());
        testeeRequest.setOppKey(printRequest.getExamId());
        testeeRequest.setSessionKey(printRequest.getSessionId());
        testeeRequest.setRequestType(printRequest.getType());
        testeeRequest.setRequestValue(printRequest.getValue());
        testeeRequest.setDateSubmitted(printRequest.getCreatedAt().toDate());

        if (printRequest.isDenied()) {
            testeeRequest.setDateFulfilled(printRequest.getChangedAt().toDate());
            testeeRequest.setDeniedReason(printRequest.getReasonDenied());
        } else if (printRequest.isApproved()) {
            testeeRequest.setDateFulfilled(printRequest.getChangedAt().toDate());
        }

        testeeRequest.setItemPage(printRequest.getPagePosition());
        testeeRequest.setItemPosition(printRequest.getItemPosition());
        testeeRequest.setRequestDesc(printRequest.getDescription());

        return testeeRequest;
    }
}

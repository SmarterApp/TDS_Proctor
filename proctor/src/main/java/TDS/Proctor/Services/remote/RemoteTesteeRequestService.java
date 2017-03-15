package TDS.Proctor.Services.remote;

import TDS.Proctor.Sql.Data.Abstractions.ExamPrintRequestRepository;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
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
import tds.exam.ExamPrintRequestStatus;

public class RemoteTesteeRequestService implements ITesteeRequestService {
    private final ITesteeRequestService legacyTesteeRequestService;
    private final ExamPrintRequestRepository examPrintRequestRepository;
    private final ExamRepository examRepository;
    private final ProctorUserDao proctorUserDao;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;

    @Autowired
    public RemoteTesteeRequestService(final ITesteeRequestService testeeRequestService,
                                      final ExamPrintRequestRepository examPrintRequestRepository,
                                      final ExamRepository examRepository,
                                      @Value("${tds.exam.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                      @Value("${tds.exam.remote.enabled}") final boolean isRemoteCallsEnabled,
                                      final ProctorUserDao proctorUserDao) {
        this.legacyTesteeRequestService = testeeRequestService;
        this.examPrintRequestRepository = examPrintRequestRepository;
        this.examRepository = examRepository;
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

        final String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (accessDenied != null) {
            throw new ReturnStatusException(accessDenied);
        }

        return mapPrintRequestsToTesteeRequests(examPrintRequestRepository.findUnfulfilledRequests(opportunityKey, sessionKey));
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

        final String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (accessDenied != null) {
            throw new ReturnStatusException(accessDenied);
        }

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

        final String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (accessDenied != null) {
            throw new ReturnStatusException(accessDenied);
        }

        ExamPrintRequest request = examPrintRequestRepository.findRequestAndApprove(requestKey);
        Exam exam = examRepository.getExamById(request.getExamId());

        // markFulfilled is always "true" as used in the legacy app
        return mapPrintRequestToTesteeRequest(request, exam);
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

        final String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

        if (accessDenied != null) {
            throw new ReturnStatusException(accessDenied);
        }

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
            TesteeRequest testeeRequest = mapPrintRequestToTesteeRequest(printRequest, null);
            testeeRequests.add(testeeRequest);
        }

        return testeeRequests;
    }

    private TesteeRequest mapPrintRequestToTesteeRequest(final ExamPrintRequest printRequest, final Exam exam) {
        TesteeRequest testeeRequest = new TesteeRequest();
        testeeRequest.setKey(printRequest.getId());
        testeeRequest.setOppKey(printRequest.getExamId());
        testeeRequest.setSessionKey(printRequest.getSessionId());
        testeeRequest.setRequestType(printRequest.getType());
        testeeRequest.setRequestValue(printRequest.getValue());
        testeeRequest.setDateSubmitted(printRequest.getCreatedAt().toDate());

        if (printRequest.getStatus() == ExamPrintRequestStatus.DENIED) {
            testeeRequest.setDateFulfilled(printRequest.getChangedAt().toDate());
            testeeRequest.setDeniedReason(printRequest.getReasonDenied());
        } else if (printRequest.getStatus() == ExamPrintRequestStatus.APPROVED) {
            testeeRequest.setDateFulfilled(printRequest.getChangedAt().toDate());
        }

        testeeRequest.setItemPage(printRequest.getPagePosition());
        testeeRequest.setItemPosition(printRequest.getItemPosition());
        testeeRequest.setRequestDesc(printRequest.getDescription());

        /* TesteeRequestRepository.java - line 95 */
        // Exam-specific data for request details
        if (exam != null) {
            testeeRequest.setTestID(exam.getAssessmentId());
            testeeRequest.setTesteeKey(exam.getStudentId());
            testeeRequest.setTesteeName(exam.getStudentName());
            testeeRequest.setLanguage(exam.getLanguageCode());
            testeeRequest.setAccCode(exam.getLanguageCode());
            testeeRequest.setRequestParameters(printRequest.getParameters());
            testeeRequest.setOpportunity(exam.getAttempts());

            if (printRequest.getType().equals(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)) {
                testeeRequest.setItemResponse(printRequest.getItemResponse());
            }
        }

        return testeeRequest;
    }
}

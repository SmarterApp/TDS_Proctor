package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPrintRequest;

/**
 * Repository to interact with exam print request data
 */
public interface ExamPrintRequestRepository {
    List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) throws ReturnStatusException;

    void denyPrintRequest(final UUID requestId, final String reason) throws ReturnStatusException;

    ExamPrintRequest findRequestAndApprove(final UUID requestId) throws ReturnStatusException;

    List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) throws ReturnStatusException;
}

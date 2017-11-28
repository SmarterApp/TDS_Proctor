package TDS.Proctor.Services;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.exam.ExpiredExamResponse;

/**
 * Handles expiring exams
 */
public interface ExamExpirationService {
    /**
     * Expires exams for given client name
     *
     * @param clientName the client name for the exams
     * @return {@link tds.exam.ExpiredExamResponse} for the expired exams
     * @throws ReturnStatusException if there is any unexpected issue expiring exams
     */
    ExpiredExamResponse expireExams(final String clientName) throws ReturnStatusException;
}

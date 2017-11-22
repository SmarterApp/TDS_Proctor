package TDS.Proctor.Services;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;

import tds.exam.ExpiredExamInformation;

/**
 * Handles expiring exams
 */
public interface ExamExpirationService {
    /**
     * Expires exams for given client name
     *
     * @param clientName the client name for the exams
     * @return a list of {@link tds.exam.ExpiredExamInformation} for the expired exams
     * @throws ReturnStatusException if there is any unexpected issue expiring exams
     */
    List<ExpiredExamInformation> expireExams(final String clientName) throws ReturnStatusException;
}

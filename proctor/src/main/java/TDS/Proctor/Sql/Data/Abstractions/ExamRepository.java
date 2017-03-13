package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;

import java.util.List;
import java.util.UUID;

import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExpandableExam;

/**
 * Repository to interact with exam data
 */
public interface ExamRepository {
    /**
     * Fetches all exams pending approval for a specific session
     *
     * @param sessionId the id of the session
     * @return the list of {@link tds.exam.Exam}s pending approval
     * @throws ReturnStatusException in the event the call failed
     */
    List<Exam> findExamsPendingApproval(UUID sessionId) throws ReturnStatusException;

    /**
     * Fetches the collection of {@link tds.exam.ExamAccommodation}s for an exam
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return the list of {@link tds.exam.ExamAccommodation}s
     * @throws ReturnStatusException in the event the call failed
     */
    List<ExamAccommodation> findAllAccommodations(UUID examId) throws ReturnStatusException;

    /**
     * Creates a request for the exam service to approve {@link tds.exam.ExamAccommodation}s
     *
     * @param examId                       the id of the {@link tds.exam.Exam}
     * @param approveAccommodationsRequest the {@link tds.exam.ApproveAccommodationsRequest} containing approval request data
     * @throws ReturnStatusException in the event the call failed
     */
    void approveAccommodations(final UUID examId, final ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException;

    /**
     * Creates a request to update the status of an exam
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @param status the status to update the exam to
     * @param stage  the stage of the exam
     * @param reason the reason for the exam status update
     * @return An optional {@link tds.common.ValidationError}, if one occurs during the processing of the request
     * @throws ReturnStatusException in the event the call failed
     */
    Optional<ValidationError> updateStatus(final UUID examId, final String status, final String stage, final String reason) throws ReturnStatusException;

    /**
     * Fetches a list of {@link tds.exam.ExpandableExam}s for the session
     *
     * @param sessionId the id of the session
     * @return a list of exams for the session
     * @throws ReturnStatusException in the event the call failed
     */
    List<ExpandableExam> findExamsForSessionId(final UUID sessionId) throws ReturnStatusException;

    /**
     * Update all the {@link tds.exam.Exam}s to "paused" status for the specified {@link tds.session.Session} id
     *
     * @param sessionId The id of the session
     * @throws ReturnStatusException in the event the call failed
     */
    void pauseAllExamsInSession(final UUID sessionId) throws ReturnStatusException;
}

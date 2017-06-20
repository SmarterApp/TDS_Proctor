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

package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;

/**
 * Repository to interact with exam print request data
 */
public interface ExamPrintRequestRepository {
    /**
     * Fetches a list of unfulfilled {@link tds.exam.ExamPrintRequest}s
     *
     * @param examId    The id of the exam the {@link tds.exam.ExamPrintRequest}s belong to
     * @param sessionId The id of the session the {@link tds.exam.ExamPrintRequest}s belong to
     * @return A {@link List<tds.exam.ExamPrintRequest>} of unfulfilled requests ordered by ExamIds and submission time
     * @throws ReturnStatusException
     */
    List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) throws ReturnStatusException;

    /**
     * Creates a request to deny an {@link tds.exam.ExamPrintRequest}
     *
     * @param requestId The id of the {@link tds.exam.ExamPrintRequest} to deny
     * @param reason    The reason the {@link tds.exam.ExamPrintRequest} is being denied
     * @throws ReturnStatusException
     */
    void denyPrintRequest(final UUID requestId, final String reason) throws ReturnStatusException;

    /**
     * Marks an {@link tds.exam.ExamPrintRequest} as approved and fetches the updated request
     *
     * @param requestId The id of the {@link tds.exam.ExamPrintRequest} to approve
     * @return The approved {@link tds.exam.ExamPrintRequest}
     * @throws ReturnStatusException
     */
    ExpandableExamPrintRequest findRequestAndApprove(final UUID requestId) throws ReturnStatusException;

    /**
     * Creates a request to fetch a list of approved {@link tds.exam.ExamPrintRequest} for the specified session
     *
     * @param sessionId The id of the session to fetch approved {@link tds.exam.ExamPrintRequest} for
     * @return The list of approved {@link tds.exam.ExamPrintRequest} for the session
     * @throws ReturnStatusException
     */
    List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) throws ReturnStatusException;
}

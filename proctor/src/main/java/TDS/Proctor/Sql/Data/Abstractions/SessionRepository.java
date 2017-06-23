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

import java.util.UUID;

import tds.common.Response;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

/**
 * Repository to interact with session data
 */
public interface SessionRepository {
    /**
     * Call the {@code SessionService#pause} method in the TDS_SessionService to pause a {@link tds.session.Session} and
     * its associated {@link tds.exam.Exam}s.
     *
     * @param sessionId The unique identifier of the {@link tds.session.Session} to pause
     * @param request The proctor key and proctor's browser key to identify
     * @return A {@link tds.common.Response<tds.session.PauseSessionResponse>} describing the state of the
     * {@link tds.session.Session} that has been paused.
     * @throws ReturnStatusException
     */
    Response<PauseSessionResponse> pause(final UUID sessionId, final PauseSessionRequest request) throws ReturnStatusException;

    /**
     *  Updates the "date visited" of the session to prevent timeout
     *
     * @param sessionId The unique identifier of the {@link tds.session.Session} to extend
     */
    void updateDateVisited(UUID sessionId) throws ReturnStatusException;
}

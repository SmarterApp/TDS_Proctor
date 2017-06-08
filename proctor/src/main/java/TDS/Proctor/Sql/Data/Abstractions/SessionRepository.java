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

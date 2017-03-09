package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

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
     * @return A {@link tds.session.PauseSessionResponse} describing the
     * @throws ReturnStatusException
     */
    PauseSessionResponse pause(final UUID sessionId, final PauseSessionRequest request) throws ReturnStatusException;
}

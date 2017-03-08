package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

/**
 * Repository to interact with session data
 */
public interface SessionRepository {
    PauseSessionResponse pause(final UUID sessionId, final PauseSessionRequest request) throws ReturnStatusException;
}

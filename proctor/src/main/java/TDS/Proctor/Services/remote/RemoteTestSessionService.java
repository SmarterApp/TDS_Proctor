package TDS.Proctor.Services.remote;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Proctor.Sql.Data.Abstractions.SessionRepository;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

@Service("remoteTestSessionService")
@Scope("prototype")
public class RemoteTestSessionService implements ITestSessionService {
    private final ITestSessionService testSessionService;
    private final SessionRepository sessionRepository;

    @Autowired
    public RemoteTestSessionService(@Qualifier("legacyTestSessionService") ITestSessionService testSessionService,
                                    final SessionRepository sessionRepository) {
        this.testSessionService = testSessionService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<TestSession> getCurrentSessions(final long proctorKey) throws ReturnStatusException {
        return testSessionService.getCurrentSessions(proctorKey);
    }

    @Override
    public TestSession getCurrentSession(final long proctorKey, final UUID curBrowserKey) throws ReturnStatusException {
        return testSessionService.getCurrentSession(proctorKey, curBrowserKey);
    }

    @Override
    public boolean pauseSession(final UUID sessionKey,
                                final long proctorKey,
                                final UUID browserKey) throws ReturnStatusException {
        PauseSessionResponse response = sessionRepository.pause(sessionKey,
            new PauseSessionRequest(proctorKey, browserKey));

        return response.getStatus().equalsIgnoreCase("closed");
    }

    @Override
    public TestSession createSession(final long proctorKey,
                                     final UUID browserKey,
                                     final String sessionName,
                                     final String proctorID,
                                     final String proctorName,
                                     final Date dateBegin,
                                     final Date dateEnd) throws ReturnStatusException {
        return testSessionService.createSession(proctorKey, browserKey,
            sessionName,
            proctorID,
            proctorName,
            dateBegin,
            dateEnd);
    }

    @Override
    public boolean insertSessionTest(final UUID sessionKey,
                                     final long proctorKey,
                                     final UUID browserKey,
                                     final String testKey,
                                     final String testID) throws ReturnStatusException {
        return testSessionService.insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testID);
    }

    @Override
    public List<String> getSessionTests(final UUID sessionKey,
                                        final long proctorKey,
                                        final UUID browserKey) throws ReturnStatusException {
        return testSessionService.getSessionTests(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean setSessionDateVisited(final UUID sessionKey,
                                         final long proctorKey,
                                         final UUID browserKey) throws ReturnStatusException {
        return testSessionService.setSessionDateVisited(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean hasActiveOpps(final UUID sessionKey,
                                 final long proctorKey,
                                 final UUID browserKey) throws ReturnStatusException {
        return testSessionService.hasActiveOpps(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean handoffSession(final long proctorKey,
                                  final UUID browserKey,
                                  final String sessionID,
                                  final _Ref<UUID> sessionKey) throws ReturnStatusException {
        return testSessionService.handoffSession(proctorKey, browserKey, sessionID, sessionKey);
    }
}

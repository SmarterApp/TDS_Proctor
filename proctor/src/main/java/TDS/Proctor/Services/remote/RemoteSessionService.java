package TDS.Proctor.Services.remote;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Proctor.Sql.Data.Abstractions.SessionRepository;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

@Service("remoteSessionService")
public class RemoteSessionService implements ITestSessionService {
    private final ITestSessionService legacyTestSessionService;
    private final SessionRepository sessionRepository;
    private final ExamRepository examRepository;
    private final boolean isLegacyCallsEnabled;
    private final boolean isRemoteCallsEnabled;

    @Autowired
    public RemoteSessionService(@Qualifier("legacyTestSessionService") final ITestSessionService legacyTestSessionService,
                                final SessionRepository sessionRepository,
                                final ExamRepository examRepository,
                                @Value("${tds.session.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                @Value("${tds.session.remote.enabled}") final boolean isRemoteCallsEnabled) {
        if (!isRemoteCallsEnabled && !isLegacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for the 'tds.session.legacy.enabled' and 'tds.session.remote.enabled' values");
        }

        this.legacyTestSessionService = legacyTestSessionService;
        this.sessionRepository = sessionRepository;
        this.examRepository = examRepository;
        this.isLegacyCallsEnabled = isLegacyCallsEnabled;
        this.isRemoteCallsEnabled = isRemoteCallsEnabled;
    }

    @Override
    public List<TestSession> getCurrentSessions(final long proctorKey) throws ReturnStatusException {
        return legacyTestSessionService.getCurrentSessions(proctorKey);
    }

    @Override
    public TestSession getCurrentSession(final long proctorKey, final UUID curBrowserKey) throws ReturnStatusException {
        return legacyTestSessionService.getCurrentSession(proctorKey, curBrowserKey);
    }

    @Override
    public boolean pauseSession(final UUID sessionKey,
                                final long proctorKey,
                                final UUID browserKey) throws ReturnStatusException {
        boolean sessionIsPaused;

        // If configured only to make calls to the legacy session service, then execute the legacy pause session
        // method (which also pauses the session's associated opportunities) and return.
        if (isLegacyCallsEnabled) {
            sessionIsPaused = legacyTestSessionService.pauseSession(sessionKey, proctorKey, browserKey);

            // If configured to make calls to the legacy service AND the TDS_SessionService, then call the legacy
            // service to pause the session (and all its associated opportunities) and call the
            // ExamService#pauseAllExamsInSession to pause all the associated exams.
            // This has to happen because both the legacy service and TDS_SessionService both affect the session.session
            // table.  If the TDS_SessionService SessionService#pause method attempts to pause a session that has
            // already been paused, an exception is thrown indicating the session is already paused. To ensure the
            // session and exam databases are in synch, only the exams in the exam database need to be paused.
            if (isRemoteCallsEnabled) {
                examRepository.pauseAllExamsInSession(sessionKey);
            }

            return sessionIsPaused;
        }

        // If configured only to make calls to the TDS_SessionService, make a call to the SessionService#pause method
        // (which will call the exam microservice to pause all the associated exams) and return.
        Response<PauseSessionResponse> response = sessionRepository.pause(sessionKey,
            new PauseSessionRequest(proctorKey, browserKey));

        if (!response.hasError() && !response.getData().isPresent()) {
            throw new ReturnStatusException("Invalid response from the session service");
        }

        if (response.getError().isPresent()) {
            ValidationError validationError = response.getError().get();
            String errorMessage = validationError.getTranslatedMessage().isPresent()
                ? validationError.getTranslatedMessage().get()
                : validationError.getMessage();

            throw new ReturnStatusException(errorMessage);
        }

        if (!response.getData().isPresent()) {
            throw new ReturnStatusException("Invalid response from the session service");
        }

        return response.getData().get().isPaused();
    }

    @Override
    public TestSession createSession(final long proctorKey,
                                     final UUID browserKey,
                                     final String sessionName,
                                     final String proctorID,
                                     final String proctorName,
                                     final Date dateBegin,
                                     final Date dateEnd) throws ReturnStatusException {
        return legacyTestSessionService.createSession(proctorKey, browserKey,
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
        return legacyTestSessionService.insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testID);
    }

    @Override
    public List<String> getSessionTests(final UUID sessionKey,
                                        final long proctorKey,
                                        final UUID browserKey) throws ReturnStatusException {
        return legacyTestSessionService.getSessionTests(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean setSessionDateVisited(final UUID sessionKey,
                                         final long proctorKey,
                                         final UUID browserKey) throws ReturnStatusException {
        return legacyTestSessionService.setSessionDateVisited(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean hasActiveOpps(final UUID sessionKey,
                                 final long proctorKey,
                                 final UUID browserKey) throws ReturnStatusException {
        return legacyTestSessionService.hasActiveOpps(sessionKey, proctorKey, browserKey);
    }

    @Override
    public boolean handoffSession(final long proctorKey,
                                  final UUID browserKey,
                                  final String sessionID,
                                  final _Ref<UUID> sessionKey) throws ReturnStatusException {
        return legacyTestSessionService.handoffSession(proctorKey, browserKey, sessionID, sessionKey);
    }
}

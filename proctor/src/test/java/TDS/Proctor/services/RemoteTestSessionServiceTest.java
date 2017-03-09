package TDS.Proctor.services;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Services.remote.RemoteTestSessionService;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Proctor.Sql.Data.Abstractions.SessionRepository;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteTestSessionServiceTest {
    private RemoteTestSessionService remoteTestSessionService;

    @Mock
    private SessionRepository mockSessionRepository;

    @Mock
    private ITestSessionService legacyTestSessionService;

    @Before
    public void setup() {
        remoteTestSessionService = new RemoteTestSessionService(legacyTestSessionService, mockSessionRepository);
    }

    @Test
    public void shouldGetCurrentSessions() throws ReturnStatusException {
        long proctorKey = 9L;
        List<TestSession> mockSessions = Arrays.asList(
            new TestSession(UUID.randomUUID(), proctorKey, UUID.randomUUID()),
            new TestSession(UUID.randomUUID(), proctorKey, UUID.randomUUID()));
        when(legacyTestSessionService.getCurrentSessions(proctorKey))
            .thenReturn(mockSessions);

        List<TestSession> result = remoteTestSessionService.getCurrentSessions(proctorKey);
        verify(legacyTestSessionService).getCurrentSessions(proctorKey);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockSessions);
    }

    @Test
    public void shouldGetCurrentSession() throws ReturnStatusException {
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();

        TestSession session = new TestSession(UUID.randomUUID(), proctorKey, browserKey);
        when(legacyTestSessionService.getCurrentSession(proctorKey, browserKey))
            .thenReturn(session);

        TestSession result = remoteTestSessionService.getCurrentSession(proctorKey, browserKey);
        verify(legacyTestSessionService).getCurrentSession(proctorKey, browserKey);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).isEqualTo(session);
    }

    @Test
    public void shouldPauseASession() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        long proctorId = 9L;
        UUID browserKey = UUID.randomUUID();

        PauseSessionResponse mockPauseSessionResponse = new PauseSessionResponse(new Session.Builder()
            .withId(sessionId)
            .withStatus("closed")
            .build());

        when(mockSessionRepository.pause(isA(UUID.class), isA(PauseSessionRequest.class)))
            .thenReturn(mockPauseSessionResponse);

        boolean response = remoteTestSessionService.pauseSession(sessionId, proctorId, browserKey);
        verify(mockSessionRepository).pause(isA(UUID.class), isA(PauseSessionRequest.class));
        verifyZeroInteractions(legacyTestSessionService);

        assertThat(response).isTrue();
    }

    @Test
    public void shouldCreateASession() throws ReturnStatusException {
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();
        String sessionName = "UNIT TEST SESSION";
        String proctorId = "PROCTOR ID";
        String proctorName = "PROCTOR NAME";
        Date dateBegin = new Date();
        Date dateEnd = new Date();

        TestSession mockSession = new TestSession(UUID.randomUUID(), proctorKey, browserKey);
        mockSession.setName(sessionName);
        mockSession.setId(proctorId);
        when(legacyTestSessionService.createSession(proctorKey,
            browserKey,
            sessionName,
            proctorId,
            proctorName,
            dateBegin,
            dateEnd))
            .thenReturn(mockSession);

        TestSession result = remoteTestSessionService.createSession(proctorKey,
            browserKey,
            sessionName,
            proctorId,
            proctorName,
            dateBegin,
            dateEnd);
        verify(legacyTestSessionService).createSession(proctorKey,
            browserKey,
            sessionName,
            proctorId,
            proctorName,
            dateBegin,
            dateEnd);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).isEqualTo(mockSession);
    }

    @Test
    public void shouldInsertSessionTest() throws ReturnStatusException {
        UUID sessionKey = UUID.randomUUID();
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();
        String testKey = "TEST KEY";
        String testId = "TEST ID";

        when(legacyTestSessionService.insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testId))
            .thenReturn(true);

        boolean result = legacyTestSessionService.insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testId);
        verify(legacyTestSessionService).insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testId);
        verifyZeroInteractions(mockSessionRepository);
    }

    @Test
    public void shouldGetSessionTests() throws ReturnStatusException {
        UUID sessionKey = UUID.randomUUID();
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();

        List<String> testKeys = Arrays.asList("first-test-key", "second-test-key", "third-test-key");
        when(legacyTestSessionService.getSessionTests(sessionKey, proctorKey, browserKey))
            .thenReturn(testKeys);

        List<String> result = legacyTestSessionService.getSessionTests(sessionKey, proctorKey, browserKey);
        verify(legacyTestSessionService).getSessionTests(sessionKey, proctorKey, browserKey);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).isEqualTo(testKeys);
    }

    @Test
    public void shouldSetSessionDateVisited() throws ReturnStatusException {
        UUID sessionKey = UUID.randomUUID();
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();

        when(legacyTestSessionService.setSessionDateVisited(sessionKey, proctorKey, browserKey))
            .thenReturn(true);

        boolean result = legacyTestSessionService.setSessionDateVisited(sessionKey, proctorKey, browserKey);
        verify(legacyTestSessionService).setSessionDateVisited(sessionKey, proctorKey, browserKey);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldHaveActiveOpps() throws ReturnStatusException {
        UUID sessionKey = UUID.randomUUID();
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();

        when(legacyTestSessionService.hasActiveOpps(sessionKey, proctorKey, browserKey))
            .thenReturn(true);

        boolean result = legacyTestSessionService.hasActiveOpps(sessionKey, proctorKey, browserKey);
        verify(legacyTestSessionService).hasActiveOpps(sessionKey, proctorKey, browserKey);
        verifyZeroInteractions(mockSessionRepository);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldHandoffSession() throws ReturnStatusException {
        long proctorKey = 9L;
        UUID browserKey = UUID.randomUUID();
        String sessionId = "SESSION ID";
        _Ref<UUID> sessionKey = new _Ref<>(UUID.randomUUID());

        when(legacyTestSessionService.handoffSession(proctorKey, browserKey, sessionId, sessionKey))
            .thenReturn(true);

        boolean result = legacyTestSessionService.handoffSession(proctorKey, browserKey, sessionId, sessionKey);

        verify(legacyTestSessionService).handoffSession(proctorKey, browserKey, sessionId, sessionKey);
        verifyZeroInteractions(mockSessionRepository);
    }
}

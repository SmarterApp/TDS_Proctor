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

package TDS.Proctor.services;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Services.remote.RemoteSessionService;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
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

import tds.common.Response;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteSessionServiceTest {
    private RemoteSessionService remoteSessionService;

    @Mock
    private SessionRepository mockRemoteSessionRepository;

    @Mock
    private ExamRepository mockRemoteExamRepository;

    @Mock
    private ITestSessionService legacyTestSessionService;

    @Before
    public void setup() {
        remoteSessionService = new RemoteSessionService(legacyTestSessionService,
            mockRemoteSessionRepository,
            mockRemoteExamRepository,
            true,
            true);
    }

    @Test
    public void shouldGetCurrentSessions() throws ReturnStatusException {
        long proctorKey = 9L;
        List<TestSession> mockSessions = Arrays.asList(
            new TestSession(UUID.randomUUID(), proctorKey, UUID.randomUUID()),
            new TestSession(UUID.randomUUID(), proctorKey, UUID.randomUUID()));
        when(legacyTestSessionService.getCurrentSessions(proctorKey))
            .thenReturn(mockSessions);

        List<TestSession> result = remoteSessionService.getCurrentSessions(proctorKey);
        verify(legacyTestSessionService).getCurrentSessions(proctorKey);
        verifyZeroInteractions(mockRemoteSessionRepository);

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

        TestSession result = remoteSessionService.getCurrentSession(proctorKey, browserKey);
        verify(legacyTestSessionService).getCurrentSession(proctorKey, browserKey);
        verifyZeroInteractions(mockRemoteSessionRepository);

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
        Response<PauseSessionResponse> mockResponse = new Response<>(mockPauseSessionResponse);

        when(mockRemoteSessionRepository.pause(isA(UUID.class), isA(PauseSessionRequest.class)))
            .thenReturn(mockResponse);
        when(legacyTestSessionService.pauseSession(sessionId, proctorId, browserKey)).thenReturn(true);

        boolean response = remoteSessionService.pauseSession(sessionId, proctorId, browserKey);
        verifyZeroInteractions(mockRemoteSessionRepository);
        verify(legacyTestSessionService).pauseSession(sessionId, proctorId, browserKey);
        verify(mockRemoteExamRepository).pauseAllExamsInSession(sessionId);

        assertThat(response).isTrue();
    }

    @Test
    public void shouldPauseASessionUsingOnlyLegacySessionService() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        long proctorId = 9L;
        UUID browserKey = UUID.randomUUID();
        RemoteSessionService remoteSessionServiceConfiguredForLegacyOnly =
            new RemoteSessionService(legacyTestSessionService,
                mockRemoteSessionRepository,
                mockRemoteExamRepository,
                true,
                false);

        when(legacyTestSessionService.pauseSession(sessionId, proctorId, browserKey)).thenReturn(true);

        boolean response = remoteSessionServiceConfiguredForLegacyOnly.pauseSession(sessionId, proctorId, browserKey);
        verifyZeroInteractions(mockRemoteSessionRepository);
        verify(legacyTestSessionService).pauseSession(sessionId, proctorId, browserKey);

        assertThat(response).isTrue();
    }

    @Test
    public void shouldPauseASessionUsingOnlyRemoteSessionService() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        long proctorId = 9L;
        UUID browserKey = UUID.randomUUID();
        RemoteSessionService remoteSessionServiceConfiguredForRemoteOnly =
            new RemoteSessionService(legacyTestSessionService,
                mockRemoteSessionRepository,
                mockRemoteExamRepository,
                false,
                true);

        PauseSessionResponse mockPauseSessionResponse = new PauseSessionResponse(new Session.Builder()
            .withId(sessionId)
            .withStatus("closed")
            .build());
        Response<PauseSessionResponse> mockResponse = new Response<>(mockPauseSessionResponse);

        when(mockRemoteSessionRepository.pause(isA(UUID.class), isA(PauseSessionRequest.class)))
            .thenReturn(mockResponse);
        doNothing().when(mockRemoteExamRepository).pauseAllExamsInSession(sessionId);

        boolean response = remoteSessionServiceConfiguredForRemoteOnly.pauseSession(sessionId, proctorId, browserKey);
        verify(mockRemoteSessionRepository).pause(isA(UUID.class), isA(PauseSessionRequest.class));
        verifyZeroInteractions(legacyTestSessionService);
        verifyZeroInteractions(mockRemoteExamRepository);

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

        TestSession result = remoteSessionService.createSession(proctorKey,
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
        verifyZeroInteractions(mockRemoteSessionRepository);

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
        verifyZeroInteractions(mockRemoteSessionRepository);
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
        verifyZeroInteractions(mockRemoteSessionRepository);

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
        verifyZeroInteractions(mockRemoteSessionRepository);

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
        verifyZeroInteractions(mockRemoteSessionRepository);

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
        verifyZeroInteractions(mockRemoteSessionRepository);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldUpdateStatus() throws ReturnStatusException {
        final UUID sessionId = UUID.randomUUID();
        boolean result = remoteSessionService.setSessionDateVisited(sessionId, 2112, UUID.randomUUID());
        assertThat(result).isTrue();
        verify(mockRemoteSessionRepository).updateDateVisited(sessionId);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenIsLegacyEnabledAndIsRemoteEnabledAreBothFalse() {
        new RemoteSessionService(legacyTestSessionService,
            mockRemoteSessionRepository,
            mockRemoteExamRepository,
            false,
            false);
    }
}

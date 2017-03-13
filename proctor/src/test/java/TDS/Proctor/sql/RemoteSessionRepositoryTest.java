package TDS.Proctor.sql;

import TDS.Proctor.Sql.Repository.RemoteSessionRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import tds.common.Response;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteSessionRepositoryTest {
    private RemoteSessionRepository remoteSessionRepository;

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Before
    public void setup() {
        remoteSessionRepository = new RemoteSessionRepository(mockRestTemplate,
            "http://localhost:8080/sessions",
            mockObjectMapper);
    }

    @Test
    public void shouldPauseASession() throws ReturnStatusException {
        UUID sessionId = UUID.randomUUID();
        PauseSessionRequest pauseSessionRequest = new PauseSessionRequest(9L, UUID.randomUUID());

        PauseSessionResponse mockPauseSessionResponse = new PauseSessionResponse(new Session.Builder()
            .withId(sessionId)
            .withStatus("closed")
            .build());
        ResponseEntity<Response<PauseSessionResponse>> mockResponseEntity =
            new ResponseEntity<>(new Response<>(mockPauseSessionResponse), HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class),
            isA(HttpMethod.class),
            isA(HttpEntity.class),
            isA(ParameterizedTypeReference.class)))
            .thenReturn(mockResponseEntity);

        Response<PauseSessionResponse> result = remoteSessionRepository.pause(sessionId, pauseSessionRequest);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(mockPauseSessionResponse);
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusException() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class),
            isA(HttpMethod.class),
            isA(HttpEntity.class),
            isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("failure"));

        remoteSessionRepository.pause(UUID.randomUUID(), new PauseSessionRequest(9L, UUID.randomUUID()));
    }
}

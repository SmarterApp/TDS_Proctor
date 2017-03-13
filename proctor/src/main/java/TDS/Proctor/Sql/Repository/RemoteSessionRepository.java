package TDS.Proctor.Sql.Repository;

import TDS.Proctor.Sql.Data.Abstractions.SessionRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

import tds.common.Response;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

@Repository
@Scope("prototype")
public class RemoteSessionRepository implements SessionRepository {
    private final RestTemplate restTemplate;
    private final String sessionUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public RemoteSessionRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                   @Value("${tds.session.remote.url}") final String sessionUrl,
                                   @Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.sessionUrl = sessionUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public Response<PauseSessionResponse> pause(final UUID sessionId,
                                                final PauseSessionRequest request) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(request, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/pause", sessionUrl, sessionId));

        try {
            ResponseEntity<Response<PauseSessionResponse>> responseEntity = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                new ParameterizedTypeReference<Response<PauseSessionResponse>>() {
                });

            return responseEntity.getBody();
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                return handleErrorResponse(hce.getResponseBodyAsString());
            }

            throw new ReturnStatusException(hce);
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    /**
     * Convert the message body from an {@link org.springframework.web.client.HttpClientErrorException} to a
     * {@link tds.common.Response<tds.session.PauseSessionResponse>} to extract the {@link tds.common.ValidationError}.
     *
     * @param responseBody The exception's response body string
     * @return A {@link tds.common.Response<tds.session.PauseSessionResponse>} extracted from the exception's body
     * @throws ReturnStatusException Wraps the potential {@link java.io.IOException} in an exception well-understood by
     * the Proctor application.
     */
    private Response<PauseSessionResponse> handleErrorResponse(String responseBody) throws ReturnStatusException {
        try {
            JavaType type = objectMapper.getTypeFactory().constructParametricType(Response.class, PauseSessionResponse.class);
            return objectMapper.readValue(responseBody, type);
        } catch (IOException e) {
            throw new ReturnStatusException(e);
        }
    }
}

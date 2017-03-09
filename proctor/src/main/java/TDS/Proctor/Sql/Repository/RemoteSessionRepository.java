package TDS.Proctor.Sql.Repository;

import TDS.Proctor.Sql.Data.Abstractions.SessionRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import tds.common.Response;
import tds.session.PauseSessionRequest;
import tds.session.PauseSessionResponse;

@Repository
@Scope("prototype")
public class RemoteSessionRepository implements SessionRepository {
    private final RestTemplate restTemplate;
    private final String sessionUrl;

    @Autowired
    public RemoteSessionRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                   @Value("${tds.session.remote.url}") final String sessionUrl) {
        this.restTemplate = restTemplate;
        this.sessionUrl = sessionUrl;
    }

    @Override
    public PauseSessionResponse pause(UUID sessionId, PauseSessionRequest request) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(request, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/pause", sessionUrl, sessionId));

        try {
            ResponseEntity<Response<PauseSessionResponse>> response = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                new ParameterizedTypeReference<Response<PauseSessionResponse>>(){});

            if (response.getBody().getData().isPresent()) {
                return response.getBody().getData().get();
            } else {
                throw new ReturnStatusException(response.getBody().getError().toString());
            }
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}

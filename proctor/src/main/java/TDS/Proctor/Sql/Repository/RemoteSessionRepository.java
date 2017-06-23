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

    @Override
    public void updateDateVisited(final UUID sessionId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/extend", sessionUrl, sessionId));

        try {
            restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                Void.class);
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

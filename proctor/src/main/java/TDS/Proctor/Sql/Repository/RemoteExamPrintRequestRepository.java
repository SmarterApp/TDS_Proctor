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

import TDS.Proctor.Sql.Data.Abstractions.ExamPrintRequestRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;

@Repository
public class RemoteExamPrintRequestRepository implements ExamPrintRequestRepository {
    private final RestTemplate restTemplate;
    private final String examUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public RemoteExamPrintRequestRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                            @Value("${tds.exam.remote.url}") final String examUrl,
                                            @Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/print/%s/%s", examUrl, sessionId, examId));

        try {
            return restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<List<ExamPrintRequest>>() {
                }).getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public void denyPrintRequest(final UUID requestId, final String reason) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(reason, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/print/deny/%s", examUrl, requestId));

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

    @Override
    public ExpandableExamPrintRequest findRequestAndApprove(final UUID requestId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/print/approve/%s", examUrl, requestId))
            .queryParam("expandableProperties", ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM);
        ExpandableExamPrintRequest examPrintRequestResponseEntity;

        try {
            examPrintRequestResponseEntity = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                ExpandableExamPrintRequest.class).getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }

        return examPrintRequestResponseEntity;
    }

    @Override
    public List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/print/approved/%s", examUrl, sessionId));

        try {
            return restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<List<ExamPrintRequest>>() {
                }).getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}

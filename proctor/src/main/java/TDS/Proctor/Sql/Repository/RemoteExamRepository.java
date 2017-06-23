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

import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusRequest;
import tds.exam.ExamStatusStage;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;

@Repository
public class RemoteExamRepository implements ExamRepository {
    private final RestTemplate restTemplate;
    private final String examUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public RemoteExamRepository(@Qualifier("integrationRestTemplate") final RestTemplate restTemplate,
                                @Value("${tds.exam.remote.url}") final String examUrl,
                                @Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.examUrl = examUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Exam> findExamsPendingApproval(final UUID sessionId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/pending-approval/%s", examUrl, sessionId));

        try {
            return restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<List<Exam>>() {
                }).getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public List<ExamAccommodation> findAllAccommodations(final UUID examId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/accommodations", examUrl, examId));

        try {
            return restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<List<ExamAccommodation>>() {
                }).getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public void approveAccommodations(final UUID examId, final ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(approveAccommodationsRequest, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/accommodations", examUrl, examId));

        try {
            restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<String>() {
                });
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public Optional<ValidationError> updateStatus(final UUID examId, final String status, final String stage, final String reason) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ExamStatusRequest request = new ExamStatusRequest(new ExamStatusCode(status, ExamStatusStage.fromType(stage)), reason);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(request, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/status", examUrl, examId));

        try {
            restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                new ParameterizedTypeReference<NoContentResponseResource>() {
                });
        } catch (HttpClientErrorException hce) {
            // No need to throw a ReturnStatusException if its a 4xx here - we'll leave it up to the service calling this method
            if (hce.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                NoContentResponseResource responseResource = handleErrorResponseNoContent(hce.getResponseBodyAsString());
                if (responseResource.getErrors().length > 0) {
                    return Optional.of(responseResource.getErrors()[0]);
                } else {
                    throw new ReturnStatusException(hce);
                }
            } else {
                throw new ReturnStatusException(hce);
            }
        }

        return Optional.absent();
    }

    @Override
    public List<ExpandableExam> findExamsForSessionId(final UUID sessionId) throws ReturnStatusException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);
        List<ExpandableExam> exams;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/session/%s", examUrl, sessionId))
            .queryParam("statusNot", ExamStatusCode.STATUS_SUSPENDED)
            .queryParam("statusNot", ExamStatusCode.STATUS_DENIED)
            .queryParam("statusNot", ExamStatusCode.STATUS_PENDING)
            .queryParam("expandableAttribute", ExpandableExamAttributes.EXAM_ACCOMMODATIONS)
            .queryParam("expandableAttribute", ExpandableExamAttributes.ITEM_RESPONSE_COUNT)
            .queryParam("expandableAttribute", ExpandableExamAttributes.UNFULFILLED_REQUEST_COUNT);

        try {
            ResponseEntity<List<ExpandableExam>> response = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                requestHttpEntity,
                new ParameterizedTypeReference<List<ExpandableExam>>() {
                });

            exams = response.getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }

        return exams;
    }

    @Override
    public void pauseAllExamsInSession(final UUID sessionId) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/pause/{sessionId}")
            .buildAndExpand(examUrl, sessionId);

        try {
            restTemplate.put(uriComponents.encode().toUri(), null);
        } catch (final HttpStatusCodeException e) {
            final ReturnStatusException statusException = new ReturnStatusException("Failed to pause the exam: " + e.getResponseBodyAsString());
            statusException.getReturnStatus().setHttpStatusCode(500);
            throw statusException;
        } catch (final RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    @Override
    public void pauseExam(final UUID examId) throws ReturnStatusException {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("{examUrl}/{examId}/pause/")
            .buildAndExpand(examUrl, examId);

        try {
            restTemplate.put(uriComponents.encode().toUri(), null);
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }

    /**
     * Determine if a {@link org.springframework.http.HttpStatus} belongs to the
     * {@link org.springframework.http.HttpStatus.Series} {@code CLIENT_ERROR} series.
     *
     * @param status The {@link org.springframework.http.HttpStatus} to evaluate
     * @return True if the status belongs to the {@code CLIENT_ERROR} series; otherwise false
     */
    private static boolean isClientError(HttpStatus status) {
        return HttpStatus.Series.CLIENT_ERROR.equals(status.series());
    }

    /**
     * Convert the message body from an {@link org.springframework.web.client.HttpClientErrorException} to a
     * {@link tds.common.web.resources.NoContentResponseResource} to extract the {@link tds.common.ValidationError}.
     *
     * @param body The body of the {@link org.springframework.http.ResponseEntity}
     * @return A {@link tds.common.web.resources.NoContentResponseResource} extracted from the body of the
     * {@link org.springframework.http.ResponseEntity}
     * @throws ReturnStatusException If the body cannot be converted
     */
    private NoContentResponseResource handleErrorResponseNoContent(final String body) throws ReturnStatusException {
        try {
            JavaType type = objectMapper.getTypeFactory().constructType(NoContentResponseResource.class);
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ReturnStatusException(e);
        }
    }
}

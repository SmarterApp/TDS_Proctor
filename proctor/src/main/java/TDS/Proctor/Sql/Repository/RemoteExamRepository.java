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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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
import tds.exam.ExpandableExam;

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
    public List<Exam> findExamsPendingApproval(UUID sessionId) throws ReturnStatusException {
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
    public List<ExamAccommodation> findAllAccommodations(UUID examId) throws ReturnStatusException {
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
    public void approveAccommodations(UUID examId, ApproveAccommodationsRequest approveAccommodationsRequest) throws ReturnStatusException {
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
        HttpEntity<?> requestHttpEntity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/status", examUrl, examId))
            .queryParam("status", status)
            .queryParam("stage", stage)
            .queryParam("reason", reason);

        try {
            restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.PUT,
                requestHttpEntity,
                new ParameterizedTypeReference<NoContentResponseResource>() {
                });
        } catch (HttpClientErrorException hce) {
            // No need to throw a ReturnStatusException if its a 4xx here - we'll leave it up to the service calling this method
            if (isClientError(hce.getStatusCode())) {
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
            .queryParam("embed", ExpandableExam.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS)
            .queryParam("embed", ExpandableExam.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT);

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

    private static boolean isClientError(HttpStatus status) {
        return HttpStatus.Series.CLIENT_ERROR.equals(status.series());
    }

    private NoContentResponseResource handleErrorResponseNoContent(String body) throws ReturnStatusException {
        try {
            JavaType type = objectMapper.getTypeFactory().constructType(NoContentResponseResource.class);
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ReturnStatusException(e);
        }
    }
}

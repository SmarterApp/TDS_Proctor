package TDS.Proctor.sql;

import TDS.Proctor.Sql.Repository.RemoteExamPrintRequestRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamPrintRequestRepositoryTest {
    private RemoteExamPrintRequestRepository repository;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JodaModule());
        repository = new RemoteExamPrintRequestRepository(mockRestTemplate, "http://localhost:8080/exam", objectMapper);
    }

    @Test
    public void shouldFindUnfulfilledRequests() throws Exception {
        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID()).build();
        ResponseEntity<List<ExamPrintRequest>> responseEntity = new ResponseEntity<>(Arrays.asList(request), HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        assertThat(repository.findUnfulfilledRequests(UUID.randomUUID(), UUID.randomUUID())).containsExactly(request);
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrown() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        repository.findUnfulfilledRequests(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void shouldDenyPrintRequest() throws Exception {
        repository.denyPrintRequest(UUID.randomUUID(), "A reason");
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), eq(Void.class));
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionForRestClientUnhaldnedExceptionDenyPrintRequest() throws Exception {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), eq(Void.class)))
            .thenThrow(new RestClientException("Fail"));
        repository.denyPrintRequest(UUID.randomUUID(), "reason");
    }

    @Test
    public void shouldFindRequestAndApprove() throws Exception {
        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID()).build();
        ExpandableExamPrintRequest expandableRequest = new ExpandableExamPrintRequest.Builder(request).build();

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(expandableRequest, HttpStatus.OK));
        assertThat(repository.findRequestAndApprove(UUID.randomUUID())).isEqualTo(expandableRequest);
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionForRestClientUnhaldnedExceptionFindAndApproveRequests() throws Exception {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        repository.findRequestAndApprove(UUID.randomUUID());
    }

    @Test
    public void shouldFindApprovedRequests() throws Exception {
        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID()).build();
        ResponseEntity<List<ExamPrintRequest>> responseEntity = new ResponseEntity<>(Arrays.asList(request), HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        assertThat(repository.findApprovedRequests(UUID.randomUUID())).containsExactly(request);
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrownFindApprovedRequests() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        repository.findApprovedRequests(UUID.randomUUID());
    }
}

package TDS.Proctor.performance.sql;

import TDS.Proctor.Sql.Repository.RemoteAssessmentRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
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
import java.util.Collections;
import java.util.List;

import tds.accommodation.Accommodation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAssessmentRepositoryTest {
    private RemoteAssessmentRepository remoteAssessmentRepository;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setup() {
        remoteAssessmentRepository = new RemoteAssessmentRepository(mockRestTemplate, "http://localhost:8080/exam");
    }

    @Test
    public void shouldReturnAccommodationsOnSuccess() throws ReturnStatusException {
        String clientName = "client";
        String assessmentKey = "assessmentKey";

        List<Accommodation> accommodations = Collections.singletonList(new Accommodation.Builder().build());
        ResponseEntity<List<Accommodation>> responseEntity = new ResponseEntity<>(accommodations, HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        assertThat(remoteAssessmentRepository.findAccommodations(clientName, assessmentKey)).isEqualTo(accommodations);
    }

    @Test (expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrown() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        remoteAssessmentRepository.findAccommodations("client", "assessmentKey");
    }
}

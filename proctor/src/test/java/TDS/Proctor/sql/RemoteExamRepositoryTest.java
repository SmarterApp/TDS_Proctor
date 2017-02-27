package TDS.Proctor.sql;

import TDS.Proctor.Sql.Repository.RemoteExamRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExpandableExam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamRepositoryTest {
    private RemoteExamRepository remoteExamRepository;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JodaModule());

        remoteExamRepository = new RemoteExamRepository(mockRestTemplate, "http://localhost:8080/exam", objectMapper);
    }

    @Test
    public void shouldReturnExamsPendingApprovalOnSuccess() throws ReturnStatusException {
        Exam exam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(UUID.randomUUID())
            .build();

        List<Exam> exams = Collections.singletonList(exam);
        ResponseEntity<List<Exam>> responseEntity = new ResponseEntity<>(exams, HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        assertThat(remoteExamRepository.findExamsPendingApproval(exam.getSessionId())).isEqualTo(exams);
    }

    @Test (expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrown() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        remoteExamRepository.findExamsPendingApproval(UUID.randomUUID());
    }

    @Test
    public void shouldReturnAllAccommodationsOnSuccess() throws ReturnStatusException {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID()).build();

        List<ExamAccommodation> accommodations = Collections.singletonList(examAccommodation);
        ResponseEntity<List<ExamAccommodation>> responseEntity = new ResponseEntity<>(accommodations, HttpStatus.OK);

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        assertThat(remoteExamRepository.findAllAccommodations(examAccommodation.getExamId())).isEqualTo(accommodations);
    }

    @Test
    public void shouldReturnExamsForSessionId() throws ReturnStatusException {
        Exam exam1 = new Exam.Builder().withId(UUID.randomUUID()).build();
        Exam exam2 = new Exam.Builder().withId(UUID.randomUUID()).build();
        ExpandableExam expandableExam1 = new ExpandableExam.Builder(exam1).build();
        ExpandableExam expandableExam2 = new ExpandableExam.Builder(exam2).build();

        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(Arrays.asList(expandableExam1, expandableExam2), HttpStatus.OK));

        List<ExpandableExam> expandableExams = remoteExamRepository.findExamsForSessionId(UUID.randomUUID());

        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
        assertThat(expandableExams).hasSize(2);
        assertThat(expandableExams.get(0).getExam().getId()).isEqualTo(exam1.getId());
        assertThat(expandableExams.get(1).getExam().getId()).isEqualTo(exam2.getId());
    }

    @Test
    public void shouldReturnEmptyListWithNoContent() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(new ArrayList<ExpandableExam>(), HttpStatus.NO_CONTENT));

        List<ExpandableExam> expandableExams = remoteExamRepository.findExamsForSessionId(UUID.randomUUID());
        verify(mockRestTemplate).exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class));
        assertThat(expandableExams).isEmpty();
    }

    @Test (expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestClientUnhandledExceptionIsThrownFindExamsForSession() throws ReturnStatusException {
        when(mockRestTemplate.exchange(isA(URI.class), isA(HttpMethod.class), isA(HttpEntity.class), isA(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientException("Fail"));
        remoteExamRepository.findExamsForSessionId(UUID.randomUUID());
    }
}


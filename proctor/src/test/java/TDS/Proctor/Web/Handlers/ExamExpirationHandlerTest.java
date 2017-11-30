package TDS.Proctor.Web.Handlers;

import TDS.Proctor.Services.ExamExpirationService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import tds.exam.ExpiredExamResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamExpirationHandlerTest {
    @Mock
    private ExamExpirationService mockExamExpirationService;

    private ExamExpirationHandler handler;

    @Before
    public void setUp() {
        handler = new ExamExpirationHandler(mockExamExpirationService);
    }

    @Test
    public void shouldExpireExams() throws ReturnStatusException {
        ExpiredExamResponse expiredExamResponse = new ExpiredExamResponse(false, Collections.emptyList());
        when(mockExamExpirationService.expireExams("SBAC")).thenReturn(expiredExamResponse);

        ResponseEntity<ExpiredExamResponse> response = handler.expireExams("SBAC");

        verify(mockExamExpirationService).expireExams("SBAC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expiredExamResponse);
    }
}
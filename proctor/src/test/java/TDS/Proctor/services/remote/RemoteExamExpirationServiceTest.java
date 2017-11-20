package TDS.Proctor.services.remote;

import TDS.Proctor.Services.remote.RemoteExamExpirationService;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamExpirationServiceTest {
    @Mock
    private ExamRepository mockExamRepository;

    private RemoteExamExpirationService examExpirationService;

    @Before
    public void setUp() {
        examExpirationService = new RemoteExamExpirationService(mockExamRepository);
    }

    @Test
    public void shouldExpireExams() throws ReturnStatusException {
        examExpirationService.expireExams("SBAC");
        verify(mockExamRepository).expireExams("SBAC");
    }
}
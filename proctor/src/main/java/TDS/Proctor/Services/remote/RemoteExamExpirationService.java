package TDS.Proctor.Services.remote;

import TDS.Proctor.Services.ExamExpirationService;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import tds.exam.ExpiredExamInformation;

@Service
public class RemoteExamExpirationService implements ExamExpirationService{
    private final ExamRepository examRepository;

    @Autowired
    public RemoteExamExpirationService(final ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public List<ExpiredExamInformation> expireExams(final String clientName) throws ReturnStatusException {
        return examRepository.expireExams(clientName);
    }
}

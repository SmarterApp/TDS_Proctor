package TDS.Proctor.Web.Handlers;

import TDS.Proctor.Services.ExamExpirationService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import tds.exam.ExpiredExamInformation;

/**
 * Handles expiration exam requests
 */
@Controller
@Scope("prototype")
public class ExamExpirationHandler {
    private final ExamExpirationService examExpirationService;

    @Autowired
    public ExamExpirationHandler(final ExamExpirationService examExpirationService) {
        this.examExpirationService = examExpirationService;
    }

    /**
     * Expires exams
     * @param clientName the client name to use when expiring exams
     * @return {@link org.springframework.http.ResponseEntity} containing list of {@link tds.exam.ExpiredExamInformation}
     * @throws ReturnStatusException if there is any issue expiring exams
     */
    @RequestMapping(value = "/exams/expire/{clientName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @Secured({ "ROLE_Assessment Modify" })
    ResponseEntity<List<ExpiredExamInformation>> expireExams(@PathVariable final String clientName) throws ReturnStatusException {
        return ResponseEntity.ok(examExpirationService.expireExams(clientName));
    }
}

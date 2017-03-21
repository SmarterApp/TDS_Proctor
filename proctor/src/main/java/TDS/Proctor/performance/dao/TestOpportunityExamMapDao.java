package TDS.Proctor.performance.dao;

import java.util.UUID;

/**
 * Interacts with the table that stores mapping from test opportunity ID to exam ID
 * This will eventually be removed as it won't be needed.
 */
public interface TestOpportunityExamMapDao {

    /**
     * Maps from the examID used in Proctor to the legacy test opportunity ID when running in dual mode
     * @param examId examID form the exam.exam table
     * @return the test opportunity ID mapped that corresponds to the examID
     */
    UUID getTestOpportunityId(UUID examId);

    /**
     * Maps from the legacy test opportunity id used in Proctor to the exam ID when running in dual mode
     * @param testOpportunityId testOpportunity key from the session.testopportunity table
     * @return the examId mapped that corresponds to the test opportunity ID
     */
    UUID getExamId(UUID testOpportunityId);
}

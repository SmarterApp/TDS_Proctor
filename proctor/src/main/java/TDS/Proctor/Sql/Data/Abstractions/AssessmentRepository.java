package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;

import tds.accommodation.Accommodation;

/**
 * Repository to interact with Assessment data
 */
public interface AssessmentRepository {
    /**
     * @param clientName    the current envrionment's client name
     * @param assessmentKey the key of the {@link tds.assessment.Assessment}
     * @return the list of all assessment {@link tds.accommodation.Accommodation}
     * @throws ReturnStatusException
     */
    List<Accommodation> findAccommodations(final String clientName, final String assessmentKey) throws ReturnStatusException;
}

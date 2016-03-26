package TDS.Proctor.performance.services;


import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.Date;
import java.util.UUID;

public interface TestSessionService {
    SingleDataResultSet createSession(String clientName, UUID browserKey, String sessionName, Long proctorKey, String procId, String procName, Date dateBegin,
                                      Date dateEnd, Integer sessionType) throws ReturnStatusException;
    SingleDataResultSet insertSessionTest(UUID sessionKey, Long proctorKey, UUID browserKey, String testKey, String testId) throws ReturnStatusException;
    SingleDataResultSet getSessionTests(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException;
    SingleDataResultSet approveAccommodations(UUID sessionKey, Long proctorKey, UUID browserKey, UUID opportunityKey, Integer segment, String segmentAccoms)
            throws ReturnStatusException;
    SingleDataResultSet approveOpportunity(UUID sessionKey, Long proctorKey, UUID opportunityKey) throws ReturnStatusException;
}

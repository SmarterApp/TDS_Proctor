package TDS.Proctor.performance.dao;


import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

public interface TestSessionDao {
    String getClientName(UUID sessionKey);
    SingleDataResultSet getCurrentSessionTestees(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException;
}

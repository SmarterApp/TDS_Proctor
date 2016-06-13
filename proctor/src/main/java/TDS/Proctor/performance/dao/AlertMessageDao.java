package TDS.Proctor.performance.dao;

import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface AlertMessageDao {
    SingleDataResultSet getUnacknowledgedAlertMessages(String clientName, Long proctorKey) throws ReturnStatusException;
    SingleDataResultSet getCurrentAlertMessages(String clientName) throws ReturnStatusException;
}

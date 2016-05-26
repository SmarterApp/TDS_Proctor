package TDS.Proctor.performance.services;

import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

public interface ProctorUserService {
    void updateDateVisited(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException;
    SingleDataResultSet getAllTests(String clientname, int sessionType, Long proctorKey) throws ReturnStatusException;
}

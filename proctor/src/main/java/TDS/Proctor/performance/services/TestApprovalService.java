package TDS.Proctor.performance.services;

import AIR.Common.DB.results.MultiDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

public interface TestApprovalService {
    MultiDataResultSet getTestsForApproval(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException;
}

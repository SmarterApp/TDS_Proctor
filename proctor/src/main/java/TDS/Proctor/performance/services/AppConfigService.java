package TDS.Proctor.performance.services;

import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface AppConfigService {
    SingleDataResultSet getConfigs(String clientName) throws ReturnStatusException;
    MultiDataResultSet getGlobalAccommodations(String clientName, String context) throws ReturnStatusException;
}

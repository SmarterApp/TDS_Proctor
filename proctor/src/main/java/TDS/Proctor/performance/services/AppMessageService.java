package TDS.Proctor.performance.services;

import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface AppMessageService {
    SingleDataResultSet getAppMessagesByContext(String systemID, String client, String language, String contextList) throws ReturnStatusException;
    SingleDataResultSet getAppMessagesByContext(String systemID, String client, String language, String contextList, Character delimiter) throws ReturnStatusException;
}

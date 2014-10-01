/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.RepositorySP;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigRepository;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;

public class AppConfigRepository extends AbstractDAO implements IAppConfigRepository
{
private static final Logger _logger = LoggerFactory.getLogger(AppConfigRepository.class);

  public AppConfig getConfigs (String clientName) throws ReturnStatusException {

    ColumnResultSet reader = null;
    AppConfig proctorAppConfig = null;
    final String CMD_GET_CONFIGS = "{call P_GetConfigs(?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_CONFIGS)) {
        callstatement.setString (1, clientName);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        ReturnStatusException.getInstanceIfAvailable (reader, "The SP P_GetConfigs did not return any records");

        reader.setFixNulls (true);

        if (reader.next ()) {
          proctorAppConfig = new AppConfig ();
          proctorAppConfig.setOperational (!reader.getBOOLEAN ("ProctorTraining"));
          String env = AppSettingsHelper.get ("OverrideCLSEnvironment");
          if ((StringUtils.isEmpty (env)) && (reader.hasColumn ("Environment"))) {
            proctorAppConfig.setEnvironment (reader.getString ("Environment"));
          }

          proctorAppConfig.setClientPath (reader.getString ("ClientStylePath"));

          proctorAppConfig.setRefreshValue (30); // default 30 secs
          if (reader.hasColumn ("refreshValue")) {
            int refresh = reader.getINT ("refreshValue");
            if (refresh > 0)
              proctorAppConfig.setRefreshValue (refresh);
          }
          proctorAppConfig.setTimeout (20); // default 20 mins
          if (reader.hasColumn ("TAInterfaceTimeout")) {
            int timeout = reader.getINT ("TAInterfaceTimeout");
            if (timeout > 0)
              proctorAppConfig.setTimeout (timeout);
          }

          proctorAppConfig.setRefreshValue (2);
          if (reader.hasColumn ("refreshValueMultiplier")) {
            proctorAppConfig.setRefreshValueMultiplier (reader.getINT ("refreshValueMultiplier"));
          }

          proctorAppConfig.setTimeZoneOffset (0);
          if (reader.hasColumn ("TimeZoneOffset")) {
            proctorAppConfig.setTimeZoneOffset (reader.getINT ("TimeZoneOffset"));
          }

          if (reader.hasColumn ("checkinURL")) {
            proctorAppConfig.setCheckinSiteURL (reader.getString ("checkinURL"));
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return proctorAppConfig;
  }

  /** 
   * Retrieve BrowserValidation object filled with appropriate BrowserRules
   */
  public BrowserValidation getBrowserValidation(String clientName, String environment, String context)
  {
      BrowserValidation browserValidation = new BrowserValidation();
      return browserValidation;
  }
}

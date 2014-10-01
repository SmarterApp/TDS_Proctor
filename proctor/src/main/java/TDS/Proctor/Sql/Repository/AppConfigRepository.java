/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

/**
 * @author efurman
 * 
 */

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Web.BrowserOS;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigRepository;
import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;
import tds.dll.api.ICommonDLL;

public class AppConfigRepository extends AbstractDAO implements IAppConfigRepository
{
  private static final Logger _logger = LoggerFactory.getLogger(AppConfigRepository.class);
  @Autowired
  IProctorDLL _pdll     = null;
  @Autowired
  ICommonDLL _cdll     = null;
	 
  public AppConfig getConfigs (String clientName) throws ReturnStatusException {

    AppConfig proctorAppConfig = null;

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_GetConfigs_SP (connection, clientName);
      ReturnStatusException.getInstanceIfAvailable (result);

      Iterator<DbResultRecord> records = result.getRecords ();
      result.setFixNulls (true);

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        proctorAppConfig = new AppConfig ();
        // TODO Udaya, discuss with oksana about ISON column in
        // TDSCONFIGS_ClientSystemFlags table. It should be integer not boolean
        proctorAppConfig.setOperational (record.<Integer> get ("ProctorTraining") > 0);
        String env = AppSettingsHelper.get ("OverrideCLSEnvironment");
        if ((StringUtils.isEmpty (env)) && (record.hasColumn ("Environment"))) {
          proctorAppConfig.setEnvironment (record.<String> get ("Environment"));
        }

        proctorAppConfig.setClientPath (record.<String> get ("ClientStylePath"));

        proctorAppConfig.setRefreshValue (30); // default 30 secs
        if (record.hasColumn ("refreshValue")) {
          int refresh = record.<Integer> get ("refreshValue");
          if (refresh > 0)
            proctorAppConfig.setRefreshValue (refresh);
        }
        proctorAppConfig.setTimeout (20); // default 20 mins
        if (record.hasColumn ("TAInterfaceTimeout")) {
          int timeout = record.<Integer> get ("TAInterfaceTimeout");
          if (timeout > 0)
            proctorAppConfig.setTimeout (timeout);
        }

        proctorAppConfig.setRefreshValueMultiplier (2);
        if (record.hasColumn ("refreshValueMultiplier")) {
          proctorAppConfig.setRefreshValueMultiplier (record.<Integer> get ("refreshValueMultiplier"));
        }

        proctorAppConfig.setTimeZoneOffset (0);
        if (record.hasColumn ("TimeZoneOffset")) {
          proctorAppConfig.setTimeZoneOffset (record.<Integer> get ("TimeZoneOffset"));
        }

        if (record.hasColumn ("checkinURL")) {
          proctorAppConfig.setCheckinSiteURL (record.<String> get ("checkinURL"));
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
  public BrowserValidation getBrowserValidation(String clientName, String environment, String context) throws ReturnStatusException {
	  BrowserValidation browserValidation = new BrowserValidation();

	  try (SQLConnection connection = getSQLConnection ()) {
		  SingleDataResultSet result = _cdll.T_GetBrowserWhiteList_SP (connection, clientName, getTdsSettings().getAppName(), context);
		  
		  Iterator<DbResultRecord> records = result.getRecords ();
	      if (records.hasNext ()) {
	        DbResultRecord record = records.next ();
	        BrowserRule browserRule = new BrowserRule();
	        browserRule.setPriority (record.<Integer> get ("Priority"));
	        browserRule.setOsName (BrowserOS.getBrowserOsFromDbString (record.<String> get ("OSName")));
	        browserRule.setOsMinVersion (record.<Float> get ("OSMinVersion").doubleValue ());
	        browserRule.setOsMaxVersion (record.<Float> get ("OSMaxVersion").doubleValue ());
	        browserRule.setArchitecture (record.<String> get ("HW_Arch"));
	        browserRule.setName (record.<String> get ("BrowserName"));
	        browserRule.setMinVersion (record.<Float> get ("BrowserMinVersion").doubleValue ());
	        browserRule.setMaxVersion (record.<Float> get ("BrowserMaxVersion").doubleValue ());
	        browserRule.setAction (BrowserAction.getBrowserActionFromStringCaseInsensitive (record.<String> get ("Action")));

	        if (record.hasColumn ("MessageKey")) {
	          browserRule.setMessageKey (record.<String> get ("MessageKey"));
	        }
	        
	        browserValidation.AddRule(browserRule);
	      }
	  } catch (SQLException e) {
		  _logger.error (e.getMessage ());
		  throw new ReturnStatusException (e);
	  }

	  return browserValidation;
  }
}

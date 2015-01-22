/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Security.AuthenticationType;

public class AppConfig extends TDSSettings
{

  private String  _clientPath;
  private String  _environment;
  private int     _timeZoneOffset;
  private int     _refreshValue;
  private int     _refreshValueMultiplier;
  private int     _timeout;
  private boolean _isOperational;
  private String  _checkinSiteURL;

  @JsonProperty ("ClientPath")
  public String getClientPath () {
    return _clientPath;
  }

  public void setClientPath (String clientPath) {
    _clientPath = clientPath;
  }

  @JsonProperty ("Environment")
  public String getEnvironment () {
    return _environment;
  }

  public void setEnvironment (String environment) {
    _environment = environment;
  }

  @JsonProperty ("TimeZoneOffset")
  public int getTimeZoneOffset () {
    return _timeZoneOffset;
  }

  public void setTimeZoneOffset (int timeZoneOffset) {
    _timeZoneOffset = timeZoneOffset;
  }

  @JsonProperty ("RefreshValue")
  public int getRefreshValue () {
    return _refreshValue;
  }

  public void setRefreshValue (int refreshValue) {
    _refreshValue = refreshValue;
  }

  @JsonProperty ("RefreshVM")
  public int getRefreshValueMultiplier () {
    return _refreshValueMultiplier;
  }

  public void setRefreshValueMultiplier (int refreshValueMultiplier) {
    _refreshValueMultiplier = refreshValueMultiplier;
  }

  @JsonProperty ("Timeout")
  public int getTimeout () {
    return _timeout;
  }

  public void setTimeout (int timeout) {
    _timeout = timeout;
  }

  @JsonProperty ("IsOP")
  public boolean isOperational () {
    return _isOperational;
  }

  public void setOperational (boolean isOperational) {
    _isOperational = isOperational;
  }

  public static boolean isCheckinSite () {
    return AppSettingsHelper.getBoolean ("IsCheckinSite", false);
  }

  @Override
  public String toString () {
    return TDSStringUtils.format ("[ClientName={7}] [ClientPath={0}] [Environment={1}] [TimeZoneOffset={2}] [RefreshValue={3}] [RefreshVM={4}] [Timeout={5}] [IsOP={6}]", _clientPath, _environment,
        _timeZoneOffset, _refreshValue, _refreshValueMultiplier, _timeout, _isOperational, getClientName ());
  }

  public String getSysCookieName (String type) {
    String appName = getAppName ();
    String checkInSys = (isCheckinSite ()) ? "CheckIn-" : "";
    return AIR.Common.Utilities.TDSStringUtils.format ("TDS-{0}-{1}{2}", appName, checkInSys, type);
  }

  public String getUserInfoCookieName () {
    return getSysCookieName ("UserInfo");
  }

  public String getVariablesCookieName () {
    return getSysCookieName ("Variables");
  }

  @JsonProperty ("CheckinSiteURL")
  public String getCheckinSiteURL () {
    return _checkinSiteURL;
  }

  public void setCheckinSiteURL (String checkinSiteURL) {
    _checkinSiteURL = checkinSiteURL;
  }

  public static AuthenticationType getAuthenticationType () {
    return AuthenticationType.getAuthenticationTypeFromStringCaseInsensitive (AppSettingsHelper.get ("AuthenticationType"));
  }

  // SB-286
  public boolean getCLSLogin () {
    return AppSettingsHelper.getBoolean ("isCLSLogin");
  }

}

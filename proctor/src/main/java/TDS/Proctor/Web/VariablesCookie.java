/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.Web.Session.HttpContext;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Shared.Web.UserCookie;

public class VariablesCookie extends UserCookie
{

  private final String _timezoneOffsetKey           = "tz";
  private final String _currentLanguageKey          = "lang";
  private final String _logoutBrowserSessionOnlyKey = "logoutSessOnly";
  private String       _currentLanguage             = null;
  public int           _timezoneOffset;
  public String        _logoutBrowserSessionOnly;

  public VariablesCookie (HttpContext thisContext,String variableCookieName) {
    super (thisContext, variableCookieName);
    if (StringUtils.isEmpty (getCurrentLanguage ()))
      setCurrentLanguage ("ENU");
  }

  public int getTimezoneOffset () {
    try {
      return Integer.parseInt (this.GetValue (_timezoneOffsetKey));
    } catch (NumberFormatException exp) {
      // no need to do anything. just return 0.
    }
    return 0;
  }

  public void setTimezoneOffset (Integer value) {
    this._timezoneOffset = value;
    this.SetValue (_timezoneOffsetKey, value.toString ());
  }

  public String getCurrentLanguage () {
    return this.GetValue (_currentLanguageKey);
  }

  public void setCurrentLanguage (String currentLanguage) {
    this._currentLanguage = currentLanguage;
    this.SetValue (_currentLanguageKey, currentLanguage);
  }

  public String getLogoutBrowserSessionOnly () {
    return this.GetValue (_logoutBrowserSessionOnlyKey);
  }

  public void setLogoutBrowserSessionOnly (String logoutBrowserSessionOnly) {
    this._logoutBrowserSessionOnly = logoutBrowserSessionOnly;
    this.SetValue (_logoutBrowserSessionOnlyKey, logoutBrowserSessionOnly);
  }
}

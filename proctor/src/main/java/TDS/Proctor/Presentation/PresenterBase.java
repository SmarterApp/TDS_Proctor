/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Presentation;

import AIR.Common.Configuration.ConfigurationSection;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Services.ProctorUserService;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.ITestService;
import TDS.Proctor.Sql.Data.Accommodations.AccsDTO;
import TDS.Proctor.Web.VariablesCookie;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;
import TDS.Shared.Web.UserCookie;

abstract class AbstractPresenterBase
{
  private ProctorAppTasks _proctorTasks = null;

  public HttpContext getHttpCurrentContext () {
    return HttpContext.getCurrentContext ();
  }

  protected ProctorAppTasks getProctorTasks () {
    if (_proctorTasks == null) {
      _proctorTasks = getBean ("proctorAppTasks", ProctorAppTasks.class);
    }
    return _proctorTasks;
  }

  private <T> T getBean (String beanName, final Class<T> clazz) {
    return FacesContextHelper.getBean (beanName, clazz);
  }
}

public class PresenterBase extends AbstractPresenterBase
{
//  private static Object   _syncRoot = new Object (); // for locking
  private IPresenterBase  _view;
  private AccsDTO         _globalAccs;
  private AppConfig       _appConfig;
  private ProctorUser     _user;
  private VariablesCookie _variablesCookie;
  private UserCookie      _userInfoCookie;

  public PresenterBase (IPresenterBase view) {
    _view = view;
    _view.setPresenterBase (this); // set presenter base
  }

  public AccsDTO getGlobalAccs () {
    if (_globalAccs == null) {
      try {
        _globalAccs = getTestTasks ().getGlobalAccs ();
      } catch (ReturnStatusException exp) {
        throw new RuntimeReturnStatusException (exp);
      }
    } 
    return _globalAccs;
  }

  public AppConfig getAppConfigDB () // get config from DB
  {
    if (_appConfig == null) {
//      synchronized (_syncRoot) {
        if (_appConfig == null)
          try {
            _appConfig = getProctorTasks ().getAppConfigTasks ().getConfigs ();
            _appConfig.setAppSettings (FacesContextHelper.getBean ("appSettings", ConfigurationSection.class));
          } catch (ReturnStatusException exp) {
            throw new RuntimeReturnStatusException (exp);
          }
//      }
    }
    return _appConfig;
  }

  public ProctorUser getThisUser () {
    if (_user == null) {
      _user = getUser ();
    }
    return _user;
  }

  public UserCookie getUserInfoCookie () {
    if (_userInfoCookie == null) {
      _userInfoCookie = new UserCookie (getHttpCurrentContext (), getAppConfigDB ().getUserInfoCookieName ());
    }
    return _userInfoCookie;
  }

  public VariablesCookie getVariables () {
    if (_variablesCookie == null) {
      _variablesCookie = new VariablesCookie (getHttpCurrentContext (),getAppConfigDB ().getVariablesCookieName ());
    }
    return _variablesCookie;
  }

  // save some config to the cookie for use later
  public void saveVariablesCookie () {
    AppConfig appConfig = getAppConfigDB ();
    getVariables ().setTimezoneOffset (appConfig.getTimeZoneOffset ());
  }

  public void displayMessage (String msg) {
    _view.displayMessage (msg);
  }

  private ITestService getTestTasks () {
    return getProctorTasks ().getTestTasks ();
  }

  private ProctorUser getUser () {
    UserCookie userInfoCookie = new UserCookie (getHttpCurrentContext (), getAppConfigDB ().getUserInfoCookieName ());
    ProctorUser user = ProctorUserService.loadUserFromCookie (userInfoCookie);
    return user;
  }

}

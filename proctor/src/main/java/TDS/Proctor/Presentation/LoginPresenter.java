/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Presentation;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Helpers.Constants;
import AIR.Common.Utilities.SpringApplicationContext;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Utilities.UrlEncoderDecoderUtils;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Proctor.Services.ProctorUserService;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;
import TDS.Shared.Security.IEncryption;
import TDS.Shared.Web.UserCookie;

import static org.opentestsystem.delivery.logging.ProctorEventLogger.eventEntry;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.eventError;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.eventLog;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.LOGOUT;

public class LoginPresenter extends PresenterBase
{

  private static final Logger _logger = LoggerFactory.getLogger(LoginPresenter.class);
  private IProctorUserService _proctorUserService = FacesContextHelper.getBean ("iProctorUserService", IProctorUserService.class);
  private ILoginPresenter     _view               = null;

  public LoginPresenter (ILoginPresenter view) {
    super (view);
    _view = view;
    initView ();
  }

  public void initView () {
  }

  // use for satellite only
  public boolean isDistributed () {
    if (dONOT_Distributed ())
      return false; // for debug only

    AppConfig appConfig = this.getAppConfigDB ();
    return (isCheckinSite () || !StringUtils.isEmpty (appConfig.getCheckinSiteURL ()));
  }

  public boolean isCheckinSite () {
    return AppConfig.isCheckinSite ();
  }

  public boolean dONOT_Distributed () // for debug only
  {
    return AppSettingsHelper.exists ("DONOT_Distributed");
  }

  // both isdistributed and CheckInsite is false
  public boolean isSatelliteSite () {
    return (isDistributed () && !isCheckinSite ());
  }

  public ProctorUser doLoginFromCheckIn (String clientName, UUID browserKey, String userName, String password, boolean ignorePW, long clsSessionKey) {
    try {
      HttpContext currentContext = getHttpCurrentContext ();
      if (!isCheckinSite ())
        return null;
      if (StringUtils.isEmpty (userName)) {
        _view.displayMessage ("User Name Required");
        return null;
      }
      if (StringUtils.isEmpty (password) && !ignorePW) {
        _view.displayMessage ("Password Required");
        return null;
      }
      
//      String entityId = null, entityLevel = null;
      ProctorUser user = _proctorUserService.validate (browserKey, userName, password, true);
//      _proctorUserService.createAndUpdateProctorIsCurrent (entityLevel, entityId, clientName, user.getKey (),TestType.SUMMATIVE);
      if (!ignorePW) // validate password
        validatePassword (password, user.getRTSPassword ());

      if (user == null || !user.isAuth ())
        throw new ReturnStatusException (new ReturnStatus ("failed", "Incorrect ID or password"));

      // check for the URL and then redirect
      if (StringUtils.isEmpty (user.getSatelliteURL ()))
        throw new ReturnStatusException (new ReturnStatus ("failed", "Satellite URL is null or empty"));

      // user.SatelliteURL =
      // @"https://sat1.sbacpt.tds.airast.org/test_testadmin/login.aspx?c=SBAC_PT";
      user.setClsSessionKey (clsSessionKey);

      // collect data to be send to satellite site
      String data =  SpringApplicationContext.getBean ("iEncryption", IEncryption.class).scrambleText (user.getData ());
      data = UrlEncoderDecoderUtils.encode (data);
      String url = TDSStringUtils.format ("{0}&d={1}", user.getSatelliteURL (), data);

      // TDSLogger.Application.Info(url);
      currentContext.getResponse ().sendRedirect (url);
      return user;
    } catch (ReturnStatusException rex) {
      _view.displayMessage (rex.getMessage ());
      _logger.error (rex.getMessage(),rex);
      return null;
    } catch (Exception ex) {
    	_logger.error (ex.getMessage(),ex);
      _view.displayMessage ("Problem authenticating user.");
    }
    return null;
  }

  public ProctorUser doLoginFromSatellite (String clientName, UUID browserKey, String userData) {
    try {
      HttpContext currentContext = getHttpCurrentContext ();
      userData =  SpringApplicationContext.getBean ("iEncryption", IEncryption.class).unScrambleText (userData);
      userData = UrlEncoderDecoderUtils.decode (userData);

      // TDSLogger.Application.Info(userData);

      ProctorUser user = new ProctorUser (clientName, browserKey, null, null);
      user.parseData (userData);

      ProctorUserService.save (user, getUserInfoCookie ()); // save info to a
                                                            // cookie

      // TDSLogger.Application.Info("Cookie saved");

      this.saveVariablesCookie (); // save some config to the cookie for use
                                   // later
      // TODO Shiva
      /*
       * TDS.Shared.Logging.TDSLog.LogSystemClient(user.ID, clientName,
       * TDSSettings.GetAppName(), HttpContext.Current);
       */
      return user;
    } catch (Exception ex) {
      _logger.error (ex.getMessage(),ex);
      _view.displayMessage ("Problem authenticating user: " + ex.getMessage ());
    }
    return null;
  }

  public ProctorUser doLogin (String clientName, UUID browserKey, String userName, String password, boolean ignorePW) {
    if (StringUtils.isEmpty (userName)) {
      _view.displayMessage ("User Name Required");
      return null;
    }
    if (StringUtils.isEmpty (password) && !ignorePW) {
      _view.displayMessage ("Password Required");
      return null;
    }
    try {
//      String entityId = null, entityLevel = null;
      ProctorUser user = _proctorUserService.validate (browserKey, userName, password, true);
//      _proctorUserService.createAndUpdateProctorIsCurrent (entityLevel, entityId, clientName, user.getKey (),TestType.SUMMATIVE);
      if (!ignorePW) // validate password
        validatePassword (password, user.getRTSPassword ());

      ProctorUserService.save (user, getUserInfoCookie ()); // save info to a
                                                            // cookie
      this.saveVariablesCookie (); // save some config to the cookie for use
                                   // later
      // TODO Shiva
      /*
       * _proctorUserService.LogSystemClient(AppSettingsHelper.GetBoolean(
       * "RecordSystemClient", false), clientName, user.ID,
       * TDSSettings.GetAppName(),
       * currentContext.Request.ServerVariables["REMOTE_ADDR"],
       * currentContext.Request.ServerVariables["HTTP_X_FORWARDED_FOR"],
       * currentContext.Request.UserAgent);
       */
      return user;
    } catch (ReturnStatusException rex) {
      _view.displayMessage (rex.getMessage ());
      return null;
    } catch (Exception ex) {
    	_logger.error (ex.getMessage(),ex);
      _view.displayMessage ("Problem authenticating user.");
    }

    return null;
  }

  private void validatePassword (String userPassword, String rtsPassword) {
    boolean isValid = StringUtils.equalsIgnoreCase (userPassword, rtsPassword);
    if (!isValid)
      throw new RuntimeReturnStatusException (new ReturnStatusException (new ReturnStatus ("failed", "Incorrect ID or password")));
  }

  public boolean doLogout () {
    try {
      ProctorUser thisUser = getThisUser ();
      try {
        eventEntry(LOGOUT, thisUser.getId(), thisUser.getSessionKey());
        if (thisUser != null && thisUser.getBrowserKey().compareTo(Constants.UUIDEmpty) != 0)
          _proctorUserService.logout(thisUser.getKey(), thisUser.getBrowserKey());
        clearOutProctorCookieInformationOrRemove(true);
        eventLog(LOGOUT, thisUser.getId(), thisUser.getSessionKey());
      } catch (Exception e) {
        eventError(LOGOUT, thisUser.getId(), thisUser.getSessionKey(), e);
      }

      return true;
    } catch (Exception ex) {
    	_logger.error (ex.getMessage(),ex);
      return false;
    }
  }

  public void clearOutProctorCookieInformationOrRemove (boolean remove) {
    UserCookie userInfoCookie = getUserInfoCookie ();
    if (remove) {
      userInfoCookie.RemoveCookie ();
    } else {
      ProctorUser thisUser = getThisUser ();
      thisUser.setAuth (false);
      thisUser.setBrowserKey (Constants.UUIDEmpty);
      thisUser.setSessionKey (Constants.UUIDEmpty);
      ProctorUserService.save (thisUser, userInfoCookie);
    }
  }

}

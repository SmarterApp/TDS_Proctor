/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.util.UUID;

import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.CookieHelper;
import AIR.Common.Web.taglib.PlaceHolder;
import TDS.Proctor.Presentation.ILoginPresenter;
import TDS.Proctor.Presentation.LoginPresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Web.presentation.taglib.CSSLink;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;

/*
 * backing bean for login.xhtml
 */
public class Login extends BasePage implements ILoginPresenter
{
  private static final Logger _logger          = LoggerFactory.getLogger (Login.class);
  private ProctorAppTasks     _proctorAppTasks = null;
  private LoginPresenter      _presenter       = null;
  private String              _clientName      = null;
  private PlaceHolder         _componentHolder = null;
  // Start page controls
  private GlobalJavascript    _globalJs        = null;
  private CSSLink             _clientCSSLink   = null;
  private HtmlInputText       _loginInput      = null;
  private HtmlInputSecret     _passwordInput   = null;

  // End page controls

  public Login () {
    // TODO Shiva Move Page_Init and Page_Load calls to a Phase event handler.
    page_Init ();
    page_Load ();
  }

  public void setLoginInput (HtmlInputText login) {
    this._loginInput = login;
  }

  public HtmlInputText getLoginInput () {
    return this._loginInput;
  }

  public void setPasswordInput (HtmlInputSecret secret) {
    this._passwordInput = secret;
  }

  public HtmlInputSecret getPasswordInput () {
    return this._passwordInput;
  }

  public void setClientCSSLink (CSSLink link) {
    this._clientCSSLink = link;
    this._clientCSSLink.setPresenter (_presenter);
  }

  public CSSLink getClientCSSLink () {
    return this._clientCSSLink;
  }

  public void setGlobalJs (GlobalJavascript gjs) {
    this._globalJs = gjs;
    this._globalJs.setPresenter (_presenter);
  }

  public GlobalJavascript getGlobalJs () {
    return this._globalJs;
  }

  public LoginPresenter getPresenter () {
    return _presenter;
  }

  public ProctorAppTasks getProctorAppTasks () {
    return _proctorAppTasks;
  }

  public void setProctorAppTasks (ProctorAppTasks proctorTasks) {
    this._proctorAppTasks = proctorTasks;
  }

  public PlaceHolder getComponentHolder () {
    return _componentHolder;
  }

  public void setComponentHolder (PlaceHolder holder) {
    this._componentHolder = holder;
  }

  public void displayMessage (String msg) {
    if (this._componentHolder != null)
    {
      HtmlOutputText text = new HtmlOutputText ();
      text.setValue (msg);
      this._componentHolder.addComponent (text);
    }
  }

  public void setPresenterBase (PresenterBase presenter) {
    // Not required to do anything here as it is already set directly in the JSF
    // / XHTML file.
  }

  protected void page_Init () {
    // TODO Shiva Ignore CLS Login redirect.
	//_presenter.getAppConfigDB ().getAuthenticationType();
    _clientName = getLoginClientName ();
    getTdsSettings ().setClientName (_clientName);// save to the RESPONSE cookie
    setRequestClientName (_clientName);// save to the REQUEST cookie as well.
  }

  protected void page_Load () {
    try {
      _presenter = new LoginPresenter (this);

      if (_presenter.isSatelliteSite ()) // if this is a satallite site
      {
        this.doLoginSatelliteSite ();
        return;
      }
    } catch (Exception ex) {
      // handle the message first
      _logger.error (ex.getMessage (), ex);
      return;
    }
  }

  private void doLoginSatelliteSite () {
    // decrypt the data from the check-in site
    String data = getRequest ().getParameter ("d");
    if (StringUtils.isEmpty (data)) // not coming from Check-in site
    {
      AppConfig appConfig = _presenter.getAppConfigDB ();
      // TODO Shiva
      // getResponse().sendRedirect (appConfig.getCheckinSiteURL());
      return;
    }

    UUID browserKey = UUID.randomUUID ();

    // parse the data and save the user's infor to cookie
    ProctorUser user = _presenter.doLoginFromSatellite (getTdsSettings ().getClientName (), browserKey, data);

    if (user != null) {
      // TODO Shiva
      /*
       * TDS.Shared.Logging.TDSLog.LogSystemClient (user.ID,
       * TDSSettings.GetClientName (), TDSSettings.GetAppName (),
       * HttpContext.Current);
       */
      // FormsAuthentication.SetAuthCookie (user.ID, false);
      // Response.Redirect (FormsAuthentication.DefaultUrl, false);
    } else {
      _logger.info (TDSStringUtils.format ("Info:DoLogin failed from satellite site: {0}", data));
    }
  }

  public String btnLogin_Click () {
    String userName = getLoginInput ().getValue ().toString ();
    String password = getPasswordInput ().getValue ().toString ();

    UUID browserKey = UUID.randomUUID ();
    ProctorUser user;
    if (_presenter.isCheckinSite ()) // login from the check in site
    {
      user = _presenter.doLoginFromCheckIn (_clientName, browserKey, userName, password, false, -1);
      return null;
    } else

      user = _presenter.doLogin (_clientName, browserKey, userName, password, false);
    if (user != null) {
      // TODO Shiva
      // FormsAuthentication.SetAuthCookie (userName, false);
      // FormsAuthentication.DefaultUrl, false);
      // getResponse ().
      return "default";
    }
    return null;
  }

  private void setRequestClientName (String clientName) {
    String cookieName = getTdsSettings ().getCookieName ("Client");
    if (clientName != null) {
      CookieHelper.setValue (cookieName, clientName);
    }
  }

}

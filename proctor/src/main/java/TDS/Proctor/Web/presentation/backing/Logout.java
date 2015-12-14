/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Proctor.Presentation.ICLSLoginPresenter;
import TDS.Proctor.Presentation.ILoginPresenter;
import TDS.Proctor.Presentation.LoginPresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Web.ProctorUrls;

public class Logout extends BasePage implements ILoginPresenter, ICLSLoginPresenter
{
	 private static final Logger _logger = LoggerFactory.getLogger(Logout.class);
  final String            _action          = "Logout";
  private ProctorAppTasks _proctorAppTasks = null;
  private boolean         _explicitLogout  = true;
  private String          _postURL;
  private LoginPresenter  _presenter;

  public Logout () {
    pageLoad ();
  }

  public String getPostURL () {
    return _postURL;
  }

  public void setPostURL (String postURL) {
    this._postURL = postURL;
  }

  /*
   * just to force the page to use this backing bean. we will ignore the
   * setAction call.
   */
  public String getAction () {
    return _action;
  }

  public void setAction (String action) {
    // do nothing.
  }

  public ProctorAppTasks getProctorAppTasks () {
    return _proctorAppTasks;
  }

  public void setProctorAppTasks (ProctorAppTasks proctorAppTasks) {
    this._proctorAppTasks = proctorAppTasks;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * TDS.Proctor.Presentation.IPresenterBase#displayMessage(java.lang.String)
   */
  @Override
  public void displayMessage (String msg) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see TDS.Proctor.Presentation.IPresenterBase#setPresenterBase(TDS.Proctor.
   * Presentation.PresenterBase)
   */
  @Override
  public void setPresenterBase (PresenterBase presenter) {
    // TODO Auto-generated method stub

  }

  protected void pageLoad () {
    _presenter = new LoginPresenter (this);
    String exl = getRequest ().getParameter ("exl");

    if (exl != null)
      _explicitLogout = Boolean.parseBoolean (exl);
    try {
      // logoutBrowserSessionOnly
      // ----------------------------------------------------------------------------
      if (isBrowserSessionLogoutOnly (_presenter)) {
        // save for call back from CLS server since CLS does not return
        // addtional parameters back
        _presenter.getVariables ().setLogoutBrowserSessionOnly ("true");
        tdsLogoutBrowserSessionOnly ();

      } else {
        // logoutBrowserSessionOnly
        // ----------------------------------------------------------------------------
        _presenter.getVariables ().setLogoutBrowserSessionOnly (""); // reset
                                                                     // the
                                                                     // cookie

        tdsLogout ();
      }
      _postURL = ProctorUrls.getLoginUrl ();
      postActionScript ();
    } catch (Exception ex) {
      // handle the message first
    	_logger.error(ex.getMessage(),ex);
      return;
    }
  }

  private void postActionScript () {
    String postAction = "close";
    if (_explicitLogout || _postURL.equalsIgnoreCase ("/login.xhtml"))
      postAction = "redirect";
    // String scripts = String.format
    // ("var postAction='{0}'; var postURL='{1}'", postAction, _postURL);
    String scripts = "var postAction= " + "\"" + postAction + "\"" + "; var postURL=" + "\"" + _postURL + "\"";
    getClientScriptBlock ().registerClientScriptBlock ("postAction", scripts, true);
  }

  private boolean isBrowserSessionLogoutOnly (LoginPresenter presenter) {
    String logoutBrowserSessionOnly = getCurrentContext ().getRequest ().getParameter ("logoutBrowserSessionOnly");
    if (StringUtils.isEmpty (logoutBrowserSessionOnly)) { // check the cookie
      logoutBrowserSessionOnly = presenter.getVariables ().getLogoutBrowserSessionOnly ();
    }
    if (!StringUtils.isEmpty (logoutBrowserSessionOnly) && logoutBrowserSessionOnly == "true") {
      return true;
    }
    return false;
  }

  private void tdsLogoutBrowserSessionOnly () {
    try {
      // TODO Shiva/Ravi We are only going to set isAuth=false and set the
      // browser and session key to empty.
      _presenter.clearOutProctorCookieInformationOrRemove (false);
    } catch (Exception ex) {
    	_logger.error(ex.getMessage(),ex);
    }
  }

  private void tdsLogout () {
    try {
      _presenter.doLogout ();
    } catch (Exception ex) {
    	_logger.error(ex.getMessage(),ex);
    }
  }

}

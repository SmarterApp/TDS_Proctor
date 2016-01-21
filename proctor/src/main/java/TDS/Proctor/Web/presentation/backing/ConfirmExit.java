/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Proctor.Presentation.ConfirmExitPresenter;
import TDS.Proctor.Presentation.IConfirmExitPresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Web.presentation.taglib.CSSLink;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;

/**
 * backing bean for confirmExit.xhtml
 * 
 */
public class ConfirmExit extends BasePage implements IConfirmExitPresenter
{

  private static final Logger _logger = LoggerFactory.getLogger(ConfirmExit.class);
  // Start page controls
  private GlobalJavascript     _globalJs        = null;
  private CSSLink              _clientCSSLink   = null;

  // End page controls

  private ProctorAppTasks      _proctorAppTasks = null;
  private ConfirmExitPresenter _presenter       = null;
  public String                _withCLSText;
  public String                _withoutCLSText;
  public boolean               _isCLSLogin;

  /**
   * Constructor
   */
  public ConfirmExit () {
    // TODO Shiva Move Page_Init and Page_Load calls to a Phase event handler.
    page_Load ();
  }

  /**
   * @return ProctorAppTasks
   */
  public ProctorAppTasks getProctorAppTasks () {
    return _proctorAppTasks;
  }

  /**
   * @param proctorAppTasks
   *          the proctorAppTasks to set
   */
  public void setProctorAppTasks (ProctorAppTasks proctorAppTasks) {
    this._proctorAppTasks = proctorAppTasks;
  }

  /**
   * @return the presenter
   */
  public ConfirmExitPresenter getPresenter () {
    return _presenter;
  }

  /**
   * @param presenter
   *          the presenter to set
   */
  public void setPresenter (ConfirmExitPresenter presenter) {
    this._presenter = presenter;
  }

  /**
   * @return the globalJs
   */
  public GlobalJavascript getGlobalJs () {
    return _globalJs;
  }

  /**
   * @param globalJs
   *          the globalJs to set
   */
  public void setGlobalJs (GlobalJavascript globalJs) {
    this._globalJs = globalJs;
    this._globalJs.setPresenter (_presenter);
  }

  /**
   * @return the clientCSSLink
   */
  public CSSLink getClientCSSLink () {
    return _clientCSSLink;
  }

  /**
   * @param clientCSSLink
   *          the clientCSSLink to set
   */
  public void setClientCSSLink (CSSLink clientCSSLink) {
    this._clientCSSLink = clientCSSLink;
    this._clientCSSLink.setPresenter (_presenter);
  }

  /**
   * @param presenter
   *          to set PresenterBase
   */
  public void setPresenterBase (PresenterBase presenter) {
    // Not required to do anything here as it is already set directly in the JSF
    // / XHTML file.
  }

  /**
   * @return void
   */
  protected void page_Load () {
    try {
      _presenter = new ConfirmExitPresenter (this);
    } catch (Exception ex) {
      // handle the message first
    	_logger.error(ex.getMessage(),ex);
      return;
    }
  }

  /**
   * @return void
   */
  public void displayMessage (String msg) {
    // messagePH.Controls.Add(new LiteralControl(BuildClientMessage(msg)));
  }

  /**
   * @return the withCLSText
   */
  public String getWithCLSText () {
    return _withCLSText;
  }

  /**
   * @param withCLSText
   *          the withCLSText to set
   */
  public void setWithCLSText (String withCLSText) {
    this._withCLSText = withCLSText;
  }

  /**
   * @return the withoutCLSText
   */
  public String getWithoutCLSText () {
    return _withoutCLSText;
  }

  /**
   * @param withoutCLSText
   *          the withoutCLSText to set
   */
  public void setWithoutCLSText (String withoutCLSText) {
    this._withoutCLSText = withoutCLSText;
  }

  /**
   * @return the isCLSLogin
   */
  public boolean isCLSLogin () {
    return _isCLSLogin;
  }

  /**
   * @param isCLSLogin
   *          the isCLSLogin to set
   */
  public void setCLSLogin (boolean isCLSLogin) {
    this._isCLSLogin = isCLSLogin;
  }
}

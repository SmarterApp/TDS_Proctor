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

import TDS.Proctor.Presentation.HomePresenter;
import TDS.Proctor.Presentation.IHomePresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Web.presentation.taglib.CSSLink;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;

public class Default extends BasePage implements IHomePresenter
{ 
	
  private static final Logger _logger = LoggerFactory.getLogger(Default.class);
  private ProctorAppTasks  _proctorAppTasks = null;
  private HomePresenter    _presenter       = null;
  // Start page controls
  private GlobalJavascript _globalJs        = null;
  private CSSLink          _clientCSSLink   = null;
  
  
  
  
  

  public Default () throws Exception{
    _logger.info ("Default.Java >>Load>>> Start >>>>>>>> "+getTime ());
    page_Load ();
    _logger.info ("Default.Java >>Load>>> End >>>>>>>> "+getTime ());
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

  public ProctorAppTasks getProctorAppTasks () {
    return _proctorAppTasks;
  }

  public void setProctorAppTasks (ProctorAppTasks proctorTasks) {
    this._proctorAppTasks = proctorTasks;
  }

  public void displayMessage (String msg) {
    // messagePH.Controls.Add(new LiteralControl(BuildClientMessage(msg)));
  }

  public void setPresenterBase (PresenterBase presenter) {

  }

  public void bindClientSitesInSystem () {
    // TODO Shiva CLS drop downs
    /*
     * if (AIR.CLS.CLSUtil.IsCLSLogin) {
     * AIR.CLS.Presentation.CLSLoginPresenter.BindClientSitesInSystem
     * (ddlUserSitesContainer, ddlUserSites, lblClientName, null,
     * _presenter.GetCLSConfig (), ulUserSites); } else
     * ddlUserSitesContainer.Visible = false;
     */
  }

  // End page controls
  protected void page_Load () throws Exception{
    try {
      _presenter = new HomePresenter (this);
      bindClientSitesInSystem ();
    } catch (Exception ex) {
      // handle the message first
    	_logger.error(ex.getMessage(),ex);
    	throw ex;
    } finally {

    }
  }
  
  
  
}

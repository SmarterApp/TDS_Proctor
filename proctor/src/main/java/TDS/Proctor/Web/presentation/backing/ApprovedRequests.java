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

import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Proctor.Presentation.ApprovedRequestsPresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Web.presentation.taglib.CSSLink;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;

/**
 * backing bean for approvedRequests.xhtml
 * 
 */
public class ApprovedRequests extends AbstractApprovedRequestsPresenter
{
  private static final Logger _logger = LoggerFactory.getLogger(ApprovedRequests.class);
  private ProctorAppTasks           _proctorAppTasks = null;
  private ApprovedRequestsPresenter _presenter       = null;
  // Start page controls
  private GlobalJavascript          _globalJs        = null;
  private CSSLink                   _clientCSSLink   = null;

  // End page controls

  /**
   * Constructor
   */
  public ApprovedRequests () {
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
  public ApprovedRequestsPresenter getPresenter () {
    return _presenter;
  }

  /**
   * @param presenter
   *          the presenter to set
   */
  public void setPresenter (ApprovedRequestsPresenter presenter) {
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
  public void displayMessage (String msg) {
    // messagePH.Controls.Add(new LiteralControl(BuildClientMessage(msg)));
  }

  /**
   * @return void
   */
  private void page_Load () {
    try {
      _presenter = new ApprovedRequestsPresenter (this);
    } catch (Exception ex) {
      // handle the message first
    	_logger.error (ex.getMessage(),ex);
      return;
    }
  }
}

// TODO convert this to the generic one.
class OppKeyPredicate implements Predicate
{
  UUID _oppKey = null;

  public OppKeyPredicate (UUID oppKey) {
    this._oppKey = oppKey;
  }

  @Override
  public boolean evaluate (Object arg0) {
    if (arg0 instanceof TesteeRequest) {
      UUID candidateKey = ((TesteeRequest) arg0).getOppKey ();
      return _oppKey.equals (candidateKey);
    }
    return false;
  }
}

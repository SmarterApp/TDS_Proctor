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

import AIR.Common.Web.Session.HttpContext;
import TDS.Proctor.Presentation.ErrorNotificationPresenter;
import TDS.Proctor.Presentation.IErrorNotificationPresenter;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Services.ProctorAppTasks;

/**
 * backing bean for errorNotification.xhtml
 * 
 */

public class ErrorNotification extends BasePage implements IErrorNotificationPresenter
{
  private static final Logger _logger = LoggerFactory.getLogger(ErrorNotification.class);
  private ProctorAppTasks            _proctorAppTasks = null;
  private ErrorNotificationPresenter _presenter       = null;
  private String                     _message;
  private String                     _activityID;
  private String                     _notificationMessage;
  private String                     _errorMessage;

  /**
   * @return the notificationMessage
   */
  public String getNotificationMessage () {
    return _notificationMessage;
  }

  /**
   * @return the activityID
   */
  public String getActivityID () {
    return _activityID;
  }

  /**
   * @return the notificationMessage
   */
  public String getErrorMessage () {
    return _errorMessage;
  }

  /**
   * Constructor
   */
  public ErrorNotification () {
    // TODO Shiva Move Page_Init and Page_Load calls to a Phase event handler.
    page_Init ();
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
  public ErrorNotificationPresenter getPresenter () {
    return _presenter;
  }

  /**
   * @param presenter
   *          the presenter to set
   */
  public void setPresenter (ErrorNotificationPresenter presenter) {
    this._presenter = presenter;
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
  protected void page_Init () {
    _activityID = HttpContext.getCurrentContext ().getRequest ().getParameter ("activityId");
    _message = HttpContext.getCurrentContext ().getRequest ().getParameter ("message");

    if (StringUtils.isBlank (_message)) {
      _message = "Unknown error occured.";
    }
    _errorMessage = _message;
  }

  /**
   * @return void
   */
  protected void page_Load () {
    try {
      _presenter = new ErrorNotificationPresenter (this);
    } catch (Exception ex) {
      // handle the message first
    	_logger.error (ex.getMessage(),ex);
      return;
    }
  }

  /**
   * @return void
   */
  public void displayMessage (String msg) {
    // messagePH.Controls.Add(new LiteralControl(BuildClientMessage(msg)));
  }

}

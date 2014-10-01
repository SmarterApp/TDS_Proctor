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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Helpers.Constants;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * ApprovedRequestsPresenter Class
 * 
 */

public class ApprovedRequestsPresenter extends PresenterBase
{
  private static final Logger _logger = LoggerFactory.getLogger(ApprovedRequestsPresenter.class);
  private IApprovedRequestsPresenter _view = null;

  /**
   * @param IApprovedRequestsPresenter
   *          view
   */
  public ApprovedRequestsPresenter (IApprovedRequestsPresenter view) {
    super (view);
    _view = view;
    initView ();
  }

  /**
   * @return void
   */
  public void initView () {
    ProctorUser thisUser = getThisUser ();
    ProctorAppTasks proctorAppTasks = getProctorTasks ();
    UUID existingSessionKey = thisUser.getSessionKey ();
    if (thisUser.getSessionKey () == null || Constants.UUIDEmpty.equals (existingSessionKey))
      return;
    try {
      UUID sessionKey = thisUser.getSessionKey ();

      _view.setRequests (proctorAppTasks.getRequestTasks ().getApprovedTesteeRequests (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));
      // TODO Ravi
      // getProctorTasks().getRequestTasks().ConvertDates(_view.getRequests,
      // Variables.TimezoneOffset);
      _view.setOpps (proctorAppTasks.getTestOppTasks ().getCurrentSessionTestees (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));
    } catch (ReturnStatusException rex) {
      displayMessage (rex.getReturnStatus ().getReason ());
      _logger.error (rex.getReturnStatus ().getReason (),rex);
    } catch (Exception ex) {
      displayMessage (ex.getMessage ());
      _logger.error (ex.getMessage (),ex);
    }
  }
}

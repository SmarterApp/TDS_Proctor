/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageRepository;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

public class AlertMessageService implements IAlertMessageService
{

  private final IAlertMessageRepository _alertMsgRepository;
  String                                _clientName;

  public AlertMessageService (TDSSettings settings, IAlertMessageRepository alertMsgRepository) {
    _alertMsgRepository = alertMsgRepository;
    _clientName = settings.getClientName ();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * TDS.Proctor.Sql.Data.Abstractions.IAlertMessageService#GetCurrentMessages
   * (int)
   */
  public AlertMessages getCurrentMessages (int timezoneOffset) throws ReturnStatusException {
    AlertMessages am = null;
    try {
      am = _alertMsgRepository.loadCurrentMessages (_clientName, timezoneOffset);
    } catch (ReturnStatusException se) {
      throw se;
    }
    return am;
  }

  /*
   * (non-Javadoc)
   * 
   * @see TDS.Proctor.Sql.Data.Abstractions.IAlertMessageService#
   * GetUnAcknowledgedMessages(long, int)
   */
  public AlertMessages getUnAcknowledgedMessages (long proctorKey, int timezoneOffset) throws ReturnStatusException {
    AlertMessages am = null;
    try {
      am = _alertMsgRepository.loadUnAcknowledgedMessages (_clientName, proctorKey, timezoneOffset);
    } catch (ReturnStatusException se) {
      throw se;
    }
    return am;
  }
}

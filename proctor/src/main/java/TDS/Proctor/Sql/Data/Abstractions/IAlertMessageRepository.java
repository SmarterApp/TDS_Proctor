/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface IAlertMessageRepository
{

  AlertMessages loadCurrentMessages (String clientName, int timezoneOffset) throws ReturnStatusException;

  AlertMessages loadUnAcknowledgedMessages (String clientName, long proctorKey, int timezoneOffset) throws ReturnStatusException;
}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import java.util.List;

import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.MessageDTO;

public interface IMessageRepository
{
  List<MessageDTO> getMessages (String language, String contextlist) throws ReturnStatusException;

  String getMessage (String contextType, String context, String appKey, String language) throws ReturnStatusException;
}

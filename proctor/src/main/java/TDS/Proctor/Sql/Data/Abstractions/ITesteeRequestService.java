/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import java.util.UUID;

import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface ITesteeRequestService
{

  TesteeRequests getCurrentTesteeRequests (UUID opportunityKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  TesteeRequests getApprovedTesteeRequests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  TesteeRequest getTesteeRequestValues (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, boolean markFulfilled) throws ReturnStatusException;

  boolean denyTesteeRequest (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, String reason) throws ReturnStatusException;

  void convertDates (TesteeRequests testeeRequests, int timeZoneOffset);

}

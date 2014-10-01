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

import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface ITestOpportunityService
{

  TestOpps getCurrentSessionTestees (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  TestOpps getTestsForApproval (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  boolean approveOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  boolean approveAccommodations (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException;

  boolean denyOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException;

  boolean pauseOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

}

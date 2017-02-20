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

  TestOpps getCurrentSessionTestees (final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException;

  TestOpps getTestsForApproval (final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException;

  boolean approveOpportunity (final UUID oppKey, final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException;

  boolean approveAccommodations (final UUID oppKey, final UUID sessionKey, final long proctorKey, final UUID browserKey, final int segment, final String segmentAccs) throws ReturnStatusException;

  void approveAccommodations (final UUID oppKey, final UUID sessionKey, final UUID browserKey, final String segmentAccs) throws ReturnStatusException;

  boolean denyOpportunity (final UUID oppKey, final UUID sessionKey, final long proctorKey, final UUID browserKey, final String reason) throws ReturnStatusException;

  boolean pauseOpportunity (final UUID oppKey, final UUID sessionKey, final long proctorKey, final UUID browserKey) throws ReturnStatusException;

}

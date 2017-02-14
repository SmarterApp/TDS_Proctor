/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.util.UUID;

import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

public class TestOpportunityService implements ITestOpportunityService
{
  private final ITestOpportunityRepository _repository;
  String                                   _clientName;

  public TestOpportunityService (TDSSettings settings, ITestOpportunityRepository repository) {
    _repository = repository;
    _clientName = settings.getClientName ();
  }

  public TestOpps getCurrentSessionTestees (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TestOpps result = null;
    try {
      result = _repository.getCurrentSessionTestees (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public TestOpps getTestsForApproval (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TestOpps result = null;
    try {
      result = _repository.getTestsForApproval (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public boolean approveOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    try {
      returnstatus = _repository.approveOpportunity (oppKey, sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    if (returnstatus != null) {
      throw new ReturnStatusException (returnstatus);
    }
    return true;
  }

  // implemented only in TestOpportunityRestService
  public void approveAccommodations(UUID oppKey, UUID sessionKey, UUID browserKey, String accommodations) throws ReturnStatusException {}

  public boolean approveAccommodations (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    try {
      returnstatus = _repository.approveAccommodations (oppKey, sessionKey, proctorKey, browserKey, segment, segmentAccs);
    } catch (ReturnStatusException re) {
      throw re;
    }
    if (returnstatus != null) {
      throw new ReturnStatusException (returnstatus);
    }
    return true;
  }

  public boolean denyOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    try {
      returnstatus = _repository.denyOpportunity (oppKey, sessionKey, proctorKey, browserKey, reason);
    } catch (ReturnStatusException re) {
      throw re;
    }
    if (returnstatus != null) {
      throw new ReturnStatusException (returnstatus);
    }
    return true;
  }

  public boolean pauseOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    ReturnStatus returnstatus = null;
    try {
      returnstatus = _repository.pauseOpportunity (oppKey, sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    if (returnstatus != null) {
      throw new ReturnStatusException (returnstatus);
    }
    return true;
  }
}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.util.Date;
import java.util.UUID;

import AIR.Common.Utilities.Dates;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

public class TesteeRequestService implements ITesteeRequestService
{
  private final ITesteeRequestRepository _repository;
  String                                 _clientName;

  public TesteeRequestService (TDSSettings settings, ITesteeRequestRepository repository) {
    _repository = repository;
    _clientName = settings.getClientName ();
  }

  public TesteeRequests getCurrentTesteeRequests (UUID opportunityKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TesteeRequests requests = null;
    try {
      requests = _repository.getCurrentTesteeRequests (opportunityKey, sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException se) {
      throw se;
    }

    return requests;
  }

  public TesteeRequests getApprovedTesteeRequests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TesteeRequests requests = null;
    try {
      requests = _repository.getApprovedTesteeRequests (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException se) {
      throw se;
    }
    return requests;
  }

  public TesteeRequest getTesteeRequestValues (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, boolean markFulfilled) throws ReturnStatusException {
    TesteeRequest request = null;
    try {
      request = _repository.getTesteeRequestValues (sessionKey, proctorKey, browserKey, requestKey, markFulfilled);
    } catch (ReturnStatusException e) {
      throw e;
    }
    return request;
  }

  public boolean denyTesteeRequest (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, String reason) throws ReturnStatusException {
    try {
      _repository.denyTesteeRequest (sessionKey, proctorKey, browserKey, requestKey, reason);
    } catch (ReturnStatusException se) {
      throw se;
    }
    return true;
  }

  public void convertDates (TesteeRequests testeeRequests, int timeZoneOffset) {
    for (int i = 0; i < testeeRequests.size (); i++) {
      TesteeRequest testeeRequest = testeeRequests.get (i);
      testeeRequest.setDateSubmitted (Dates.convertEST_XST (testeeRequest.getDateSubmitted (), timeZoneOffset));
      if (testeeRequest.getDateFulfilled () != null)
        testeeRequest.setDateFulfilled (Dates.convertEST_XST ((Date) testeeRequest.getDateFulfilled (), timeZoneOffset));
    }

  }

}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.services.test;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import AIR.test.framework.AbstractTest;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestService;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
public class TesteeRequestServiceTest
{
  private static final Logger _logger              = LoggerFactory.getLogger (TesteeRequestServiceTest.class);
  @Autowired
  @Qualifier ("iTesteeRequestService")
  ITesteeRequestService       testeeRequestService = null;

  // TODO: scripts from Sai
  // #2 --run daily for GetCurrentTesteeRequests
  // update TestOppRequest
  // set datesubmitted = getdate()
  // where _fk_TestOpportunity = '3462D73B-C024-463E-AD6E-D630A40DF69B'
  // and _fk_Session = '502165F2-909C-4FFB-8AB1-0EB2A398C8FF'

  // #3 -- run daily for GetApprovedTesteeREquests
  // update TestOppRequest
  // set datesubmitted = getdate()
  // --, DateFulfilled = getdate()
  // --, datedenied = NULL
  // where _fk_Session = '1558D3D3-5C43-4E6A-9E1E-CD4A0A33D0E5'

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetCurrentTesteeRequests () throws Exception {
    UUID opportunityKey = /* UUID.randomUUID (); */UUID.fromString ("3462D73B-C024-463E-AD6E-D630A40DF69B");

    UUID sessionKey = /* UUID.randomUUID (); */UUID.fromString ("502165F2-909C-4FFB-8AB1-0EB2A398C8FF");
    long proctorKey = 35843;
    UUID browserKey = /* UUID.randomUUID (); */UUID.fromString ("83070BE9-D2C9-457A-B64D-CC2C7D37077D");
    TesteeRequests requests = testeeRequestService.getCurrentTesteeRequests (opportunityKey, sessionKey, proctorKey, browserKey);
    _logger.info (" GetCurrentTesteeRequests SIZE.." + requests.size ());
    assertTrue (requests.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetApprovedTesteeRequests () throws Exception {
    UUID sessionKey = /* UUID.randomUUID (); */UUID.fromString ("1558D3D3-5C43-4E6A-9E1E-CD4A0A33D0E5");
    long proctorKey = 24348;
    UUID browserKey = /* UUID.randomUUID (); */UUID.fromString ("55FF687A-10FE-47E8-93A6-DE922B8FB28D");

    TesteeRequests requests = testeeRequestService.getApprovedTesteeRequests (sessionKey, proctorKey, browserKey);
    _logger.info (" GetApprovedTesteeRequests SIZE.." + requests.size ());
    assertTrue (requests.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetTesteeRequestValues () throws Exception {
    UUID sessionKey = /* UUID.randomUUID (); */UUID.fromString ("3D807FC4-2C46-4789-8F0C-B2D1A42A5573");
    long proctorKey = 24348;
    UUID browserKey = /* UUID.randomUUID (); */UUID.fromString ("69754AE3-2C32-454B-94A8-6A51C1277673");
    UUID requestKey = /* UUID.randomUUID (); */UUID.fromString ("5972AE8F-F0C4-4FF8-A663-06CD6816B2FD");

    boolean markFulfilled = false;
    TesteeRequest request = testeeRequestService.getTesteeRequestValues (sessionKey, proctorKey, browserKey, requestKey, markFulfilled);

    assertTrue (request != null);
    _logger.info (" GetTesteeRequestValues: TesteeName: " + request.getTesteeName ());
  }

  @Test
  public void testDenyTesteeRequest () throws Exception {
    UUID sessionKey = /* UUID.randomUUID (); */UUID.fromString ("45F18366-CB66-469D-89E9-B4F6AEDFEA02");
    long proctorKey = 32;
    UUID browserKey = /* UUID.randomUUID (); */UUID.fromString ("3537FA32-8FFA-4D08-9430-C87F2BB8F512");
    UUID requestKey = /* UUID.randomUUID (); */UUID.fromString ("41D943CF-CAB3-4B73-8FCB-00F938CA1F1C");
    String reason = "Testing";
    testeeRequestService.denyTesteeRequest (sessionKey, proctorKey, browserKey, requestKey, reason);
    _logger.info ("DenyTesteeRequest: successful");
  }
}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.repository.test;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Ignore;
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

import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityRepository;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_ukommineni
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
@Ignore("Requires external resources")
public class TestOppRepositoryTest
{
  @Autowired
  @Qualifier ("iTestOpportunityRepository")
  private ITestOpportunityRepository _testOpportunityRepository = null;
  private static final Logger        _logger                    = LoggerFactory.getLogger (TestOppRepositoryTest.class);

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testApproveAccomodations1 () throws Exception {
    // success case. should not throw exception
    UUID sessionKey = UUID.fromString ("C19F428D-4395-4B65-A567-6D418711A2DC");
    long proctorKey = 24390;
    UUID browserKey = UUID.fromString ("5DCE7D2A-E724-4930-AC9E-B0F2FD4D263D");
    UUID oppKey = UUID.fromString ("FCEC4C40-0556-42D0-BF42-7A64C491AD56");
    int segment = 0;
    String segmentAccs = "";
    _testOpportunityRepository.approveAccommodations (oppKey, sessionKey, proctorKey, browserKey, segment, segmentAccs);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testApproveAccomodations2 () throws Exception {
    // failure case. should throw exception
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    long proctorKey = 24352;
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");
    int segment = 1;
    String segmentAccs = "TEST";

    try {
      _testOpportunityRepository.approveAccommodations (oppKey, sessionKey, proctorKey, browserKey, segment, segmentAccs);

    } catch (ReturnStatusException e) {
      // expected exception
      _logger.info ("Expected exception " + e.getMessage ());

    } catch (Exception e) {
      // any other unexpected exceptions
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  // Run update once daily for the update statement to work
  // update TestOpportunity_ReadOnly
  // set DateChanged = getdate()
  // where _fk_session = '87388C02-92D4-4FF0-A12B-0866DD0BE0E5'

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetCurrentSessionTestees () throws Exception {
    TestOpps testOpps = null;
    UUID sessionKey = UUID.fromString ("87388C02-92D4-4FF0-A12B-0866DD0BE0E5");
    long proctorKey = 26496;
    UUID browserKey = UUID.fromString ("91B088B8-1815-41E3-B16F-857C615626EF");
    try {
      testOpps = _testOpportunityRepository.getCurrentSessionTestees (sessionKey, proctorKey, browserKey);
      assertTrue(testOpps.size () > 0);
      _logger.info ("current session Testees .. " + testOpps.size ());
    } catch (ReturnStatusException re) {
      _logger.error (re.getMessage ());
      throw new Exception (re.getMessage ());
    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  @Test
  public void testGetTestsForApproval () throws Exception {
    TestOpps testOpps = null;
    UUID sessionKey = UUID.fromString ("FEA63E2D-B102-4920-88B0-E6011B8C837E");
    long proctorKey = 20424;
    UUID browserKey = UUID.fromString ("9318C537-EC05-41ED-AB08-39ECE4E8D605");
    testOpps = _testOpportunityRepository.getTestsForApproval (sessionKey, proctorKey, browserKey);
    assertTrue(testOpps.size () > 0);
    _logger.info ("Tests for Approval .. " + testOpps.size ());
  }

  // Run the below update script each time in the SQL server before u run the
  // success
  // test case
  // update TestOpportunity set status = 'pending', prevstatus = NULL,
  // datechanged = '2012-05-29' , dateapproved = NULL where _fk_session =
  // '2F40C9A6-9525-4F6C-A93F-507180297D74'
  // and _key = 'B97A6448-5F59-40F1-9CE6-019A26DD549B'

  @Test
  // success case
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testApproveOpportunity () throws Exception {
    ReturnStatus ret = null;
    UUID sessionKey = UUID.fromString ("2F40C9A6-9525-4F6C-A93F-507180297D74");
    long proctorKey = 1108;
    UUID browserKey = UUID.fromString ("C90FCF70-55DC-4110-BDA1-BE28C6DE615F");
    UUID oppKey = UUID.fromString ("B97A6448-5F59-40F1-9CE6-019A26DD549B");
    ret = _testOpportunityRepository.approveOpportunity (oppKey, sessionKey, proctorKey, browserKey);
    assertTrue(ret == null);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // failure case
  public void testApproveOpportunity1 () throws Exception {
    UUID sessionKey = UUID.fromString ("690B4504-7571-4E04-9E07-28C96B47FA33");
    long proctorKey = 24365;
    UUID browserKey = UUID.fromString ("9B7E9550-72E8-4860-A7AC-FEF1FC132982");
    UUID oppKey = UUID.fromString ("FE0620E9-3530-4FA8-8A0F-0004AEA29F40");
    _testOpportunityRepository.approveOpportunity (oppKey, sessionKey, proctorKey, browserKey);
  }

  // Run the below update script each time in the SQL server before u run the
  // success
  // test case
  // update TestOpportunity set status = 'pending' , prevstatus = 'approved',
  // datechanged = '2012-05-29', comment = NULL
  // where _key = '2FE4069F-383A-4945-BFF1-E88BE3BACADF'

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // success case
  public void testDenyApproval () throws Exception {
    ReturnStatus ret = null;
    UUID sessionKey = UUID.fromString ("79856134-784E-4537-8F0B-99211627FEB6");
    long proctorKey = 24352;
    UUID browserKey = UUID.fromString ("72DEC6C0-F4DE-42FE-911D-2D69233B3FCF");
    UUID oppKey = UUID.fromString ("5741E24B-A9E0-401A-9F45-007E61D69C28");
    String reason = "Test Reason - Case#4";
    ret = _testOpportunityRepository.denyOpportunity (oppKey, sessionKey, proctorKey, browserKey, reason);
    assertTrue(ret == null);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // failure case1
  public void testDenyApproval1 () throws Exception {
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    long proctorKey = 24352;
    UUID browserKey = UUID.fromString ("EEABB7D2-DADA-4DD8-B82F-5CE44AD871CE");
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    String reason = "Test Reason - Case#2";
    _testOpportunityRepository.denyOpportunity (oppKey, sessionKey, proctorKey, browserKey, reason);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // failure case2
  public void testDenyApproval2 () throws Exception {
    UUID sessionKey = UUID.fromString ("690B4504-7571-4E04-9E07-28C96B47FA33");
    long proctorKey = 24365;
    UUID browserKey = UUID.fromString ("9B7E9550-72E8-4860-A7AC-FEF1FC132982");
    UUID oppKey = UUID.fromString ("FE0620E9-3530-4FA8-8A0F-0004AEA29F40");
    String reason = "Test Reason - Case#1";
    _testOpportunityRepository.denyOpportunity (oppKey, sessionKey, proctorKey, browserKey, reason);
  }

  // Run the below update script in the SQL Server each time before u run this
  // test
  // update TestOpportunity set status = 'started', prevstatus = 'approved',
  // datechanged = '2012-05-29', comment = NULL, datepaused = NULL
  // where _key = '2FE4069F-383A-4945-BFF1-E88BE3BACADF'

  @Test
  // success case
  public void testPauseOpportunity () throws Exception {
    ReturnStatus ret = null;
    UUID sessionKey = UUID.fromString ("B3922143-B287-4D5B-9364-0F9BAB10398F");
    long proctorKey = 20628;
    UUID browserKey = UUID.fromString ("062819DE-DDAF-44D0-8977-EBC4C002BDF0");
    UUID oppKey = UUID.fromString ("ABFC402F-07A5-488A-BBFD-FF1AAB7EFAD8");
    ret = _testOpportunityRepository.pauseOpportunity (oppKey, sessionKey, proctorKey, browserKey);
    // repository pass null ReturnStatus in case of success
    assertTrue(ret == null);
  }

  @Test
  // failure case
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testPauseOpportunity1 () throws Exception {
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    long proctorKey = 24352;
    UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    _testOpportunityRepository.pauseOpportunity (oppKey, sessionKey, proctorKey, browserKey);
  }

}

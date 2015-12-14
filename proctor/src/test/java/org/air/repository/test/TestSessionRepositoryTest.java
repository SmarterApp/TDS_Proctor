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

import java.util.Date;
import java.util.List;
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

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionRepository;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_ukommineni
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
public class TestSessionRepositoryTest
{
  @Autowired
  @Qualifier ("iTestSessionRepository")
  private ITestSessionRepository _testSessionRepository = null;
  private static final Logger    _logger                = LoggerFactory.getLogger (TestSessionRepositoryTest.class);

  // --Run below script in the SQL server before running this test everytime
  // delete from SessionTests where _fk_Session =
  // 'C2E01F55-0FA4-4E43-AAD9-B62233F8A490' and _efk_AdminSubject =
  // '(Oregon_PT)ELPA-6-8-Fall-2012-2013'

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // when i run this test in java, it is failing but after running this
  // test when i look in the database, it shows that the test has been inserted.
  public void testInsertSessionTest () throws Exception {
    ReturnStatus ret = null;
    UUID sessionKey = UUID.fromString ("C2E01F55-0FA4-4E43-AAD9-B62233F8A490");
    long proctorKey = 20628;
    UUID browserKey = UUID.fromString ("2BB0568E-ED4D-42AD-B493-994AC51EC398");
    String testKey = "(Oregon_PT)ELPA-6-8-Fall-2012-2013";
    String testID = "ELPA 6-8 ";
    ret = _testSessionRepository.insertSessionTest (sessionKey, proctorKey, browserKey, testKey, testID);
    assertTrue(ret == null);
  }

  // run the below script in the database everytime before we run
  // this test
  // update session
  // set [Status] = 'Open'
  // , datebegin = '2012-05-29'
  // , dateend = '2020-05-29'
  // , datechanged = '2012-05-29'
  // where _key in ( 'DB5AACDA-D724-4F49-98B1-C89E5267AB32'
  // , 'CC0CCD9C-A69D-4C1A-9512-A63F4A840922'
  // , 'FEA63E2D-B102-4920-88B0-E6011B8C837E'
  // , 'E483B532-264C-4F27-A16E-005FF85CA7C7')

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetCurrentSessions () throws Exception {
    List<TestSession> testSession = null;
    String clientname = "Minnesota";
    long proctorKey = 2;
    int sessionType = 0;
    testSession = _testSessionRepository.getCurrentSessions (clientname, proctorKey, sessionType);
    assertTrue(testSession.size () > 0);
    _logger.info ("current sessions .. " + testSession.size ());
  }

  // run the below script in the database everytime before we run this test
  // update session set [Status] = 'Open', datebegin = '2012-05-29', dateend =
  // '2020-05-29', datechanged = '2012-05-29' where _key in (
  // 'DB5AACDA-D724-4F49-98B1-C89E5267AB32'
  // , 'CC0CCD9C-A69D-4C1A-9512-A63F4A840922' ,
  // 'FEA63E2D-B102-4920-88B0-E6011B8C837E',
  // 'E483B532-264C-4F27-A16E-005FF85CA7C7')

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetSessionTests () throws Exception {
    List<String> sessionTests = null;
    UUID sessionKey = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
    long proctorKey = 20424;
    UUID browserKey = UUID.fromString ("3AEF9213-85E6-4346-A202-A381B5334CD5");
    sessionTests = _testSessionRepository.getSessionTests (sessionKey, proctorKey, browserKey);
    assertTrue(sessionTests.size () > 0);
    _logger.info ("session Tests size .." + sessionTests.size ());
  }

  // run the below script in the database everytime before we run this test
  // update session set [Status] = 'Open', datebegin = '2012-05-29', dateend =
  // '2020-05-29', datechanged = '2012-05-29' where _key in (
  // 'DB5AACDA-D724-4F49-98B1-C89E5267AB32'
  // , 'CC0CCD9C-A69D-4C1A-9512-A63F4A840922' ,
  // 'FEA63E2D-B102-4920-88B0-E6011B8C837E',
  // 'E483B532-264C-4F27-A16E-005FF85CA7C7')

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testHasActiveOpps () throws Exception {
    UUID sessionKey = UUID.fromString ("FEA63E2D-B102-4920-88B0-E6011B8C837E");
    long proctorKey = 20424;
    UUID browserKey = UUID.fromString ("9318C537-EC05-41ED-AB08-39ECE4E8D605");
    boolean hao = _testSessionRepository.hasActiveOpps (sessionKey, proctorKey, browserKey);
    assertTrue(hao == true);
  }

  // CASE#1: Success Case
  // Nothing is returned from this procedure when success
  // Column 'DateVisted' in table 'Session' should be updated to date the test
  // was run when success

  @Test
  public void testSetSessionDateVisited () throws Exception {
    UUID sessionKey = UUID.fromString ("E483B532-264C-4F27-A16E-005FF85CA7C7");
    long proctorKey = 1326;
    UUID browserKey = UUID.fromString ("32CACB7B-03C9-40C4-A6B6-F719DD2E9A34");
    ReturnStatus ret = _testSessionRepository.setSessionDateVisited (sessionKey, proctorKey, browserKey);
    assertTrue(ret == null);
  }

  // run the below script in the database everytime before we run this test
  // update session set [Status] = 'Open', datebegin = '2012-05-29', dateend =
  // '2020-05-29', datechanged = '2012-05-29' where _key in (
  // 'DB5AACDA-D724-4F49-98B1-C89E5267AB32'
  // , 'CC0CCD9C-A69D-4C1A-9512-A63F4A840922' ,
  // 'FEA63E2D-B102-4920-88B0-E6011B8C837E',
  // 'E483

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // success case
  public void testPauseSession () throws Exception {
    UUID sessionKey = UUID.fromString ("DB5AACDA-D724-4F49-98B1-C89E5267AB32");
    long proctorKey = 21545;
    UUID browserKey = UUID.fromString ("505497A1-3D19-4231-91B7-C25751D0BC41");
    ReturnStatus ret = _testSessionRepository.pauseSession (sessionKey, proctorKey, browserKey);
    assertTrue(ret == null);
  }

  @Test (expected = ReturnStatusException.class)
  // failure case
  public void testPauseSession1 () throws Exception {
    UUID sessionKey = UUID.fromString ("DE5AACDA-D724-4F49-98B1-C89E5267AB39");
    long proctorKey = 2154;
    UUID browserKey = UUID.fromString ("505497A1-3D19-4231-91B7-C25751D0BC99");
    // expect ReturnStatusException
    _testSessionRepository.pauseSession (sessionKey, proctorKey, browserKey);
  }

  // For success case; a record will be inserted into SessionAudit table for the
  // given date. After running this test, run the below script in the SQL
  // server, to check for the record that is inserted.
  // select * from SessionAudit where _fk_session =
  // 'DF9B19BA-C0EB-4781-9E47-08534CF115A4'
  // and cast(floor(cast(DateAccessed as float)) as smalldatetime) =
  // cast(floor(cast(getdate() as float)) as smalldatetime)
  // and browserkey = '93A42BE2-B8F6-4248-BFA7-12900410D5FC'

  @Test
  // success case
  public void testHandOffSession () throws Exception {
    _Ref<UUID> sessionKey = new _Ref<UUID> ();
    String clientName = "Oregon";
    long proctorKey = 26496;
    String sessionID = "air-50";
    UUID browserKey = UUID.fromString ("93A42BE2-B8F6-4248-BFA7-12900410D5FC");
    _testSessionRepository.handoffSession (clientName, proctorKey, browserKey, sessionID, sessionKey);
    assertTrue(sessionKey.get () != null);
  }

  @Test (expected = ReturnStatusException.class)
  // failure case
  public void testHandOffSession1 () throws Exception {
    _Ref<UUID> sessionKey = new _Ref<UUID> ();
    String clientName = "Oregon";
    long proctorKey = 2649;
    String sessionID = "air-50";
    UUID browserKey = UUID.fromString ("93A42BE2-B8F6-4248-BFA7-12900410D5FC");
    // Expect REturnStatusException
    _testSessionRepository.handoffSession (clientName, proctorKey, browserKey, sessionID, sessionKey);
  }

  // Run the below script everytime before u run the success case test
  // delete
  // from session
  // where clientname = 'Oregon' and _fk_browser =
  // 'D1019F11-7220-42E0-9847-450701BE3CB6'

  @Test (expected = ReturnStatusException.class)
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  // success case
  public void testCreateSession () throws Exception {
    String clientName = "Oregon";
    UUID browserKey = UUID.fromString ("D1019F11-7220-42E0-9847-450701BE3CB6");
    String sessionName = " ";
    long proctorKey = 35837;
    String proctorID = "dtsa1@air.org";
    String proctorName = "DTSA One";
    Date dateBegin = null;
    Date dateEnd = null;
    try {
      TestSession testSession = _testSessionRepository.createSession (clientName, proctorKey, browserKey, sessionName, proctorID, proctorName, dateBegin, dateEnd);
      _logger.info ("Create session Id... " + testSession.getId ());
      _logger.info ("Create session Status... " + testSession.getStatus ());
    } catch (ReturnStatusException e) {
      // Should not throw an error on the first attempt
      throw new AssertionError ("Should not throw ReturnStatusException on first attempt", e);
    }

    // now try to create it again; expect REturnStatusException
    // should not run as two separate test functions because order of execution
    // may be reverse
    _testSessionRepository.createSession (clientName, proctorKey, browserKey, sessionName, proctorID, proctorName, dateBegin, dateEnd);
  }

  @Test (expected = ReturnStatusException.class)
  // Failure case for different unknown clientname
  public void testCreateSession2 () throws Exception {
    String clientName = "XYZ";
    UUID browserKey = UUID.fromString ("D1019F11-7220-42E0-9847-450701BE3CB6");
    String sessionName = " ";
    long proctorKey = 35837;
    String proctorID = "dtsa1@air.org";
    String proctorName = "DTSA One";
    Date dateBegin = null;
    Date dateEnd = null;
    _testSessionRepository.createSession (clientName, proctorKey, browserKey, sessionName, proctorID, proctorName, dateBegin, dateEnd);
  }
}

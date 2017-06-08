/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.repository.test;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.api.IRtsDLL;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
@Ignore("Requires external resources")
public class ProctorRepositoryTest
{
  @SuppressWarnings ("unused")
  private static final Logger _logger   = LoggerFactory.getLogger (ProctorRepositoryTest.class);

  @Autowired
  private IProctorRepository  _proctorRepository;

  @Autowired
  private IRtsDLL             _rtsDll;

  @Rule
  public ExpectedException    exception = ExpectedException.none ();

  @Test
  public void testValidate1 () throws ReturnStatusException {
    String clientName = "Oregon";
    UUID browserKey = UUID.fromString ("28EE3122-3C0C-408E-814E-7D5DF63FFBD7");
    String userId = "TA1@air.org";
    String password = null;
    boolean openSessions = true; /* ignored by DLL */
    boolean ignorePW = true; /* ignored by DLL */
    _proctorRepository.validate (clientName, browserKey, userId, password, openSessions, ignorePW);
  }

  @Test
  public void testValidate2 () throws ReturnStatusException {
    String clientName = "Oregon";
    UUID browserKey = UUID.fromString ("28EE3122-3C0C-408E-814E-7D5DF63FFBD7");
    String userId = "TA1@air.org";
    String password = "9cu+IIEpgw1U0UjHhYwcUrK7FJ4BJ6Ef/pDMauaObb8FmcAiCIE=";
    boolean openSessions = true; /* ignored by DLL */
    boolean ignorePW = true; /* ignored by DLL */

    _proctorRepository.validate (clientName, browserKey, userId, password, openSessions, ignorePW);
  }

  @Test
  public void testValidate3 () throws Exception {

    String clientName = "Oregon";
    UUID browserKey = UUID.fromString ("F7A4F69E-B98D-494E-988F-384DBFB58B5D");
    String userId = "DemoTA11@air.org";
    String password = null;
    boolean openSessions = true; /* ignored by DLL */
    boolean ignorePW = true; /* ignored by DLL */

    exception.expect (ReturnStatusException.class);
    _proctorRepository.validate (clientName, browserKey, userId, password, openSessions, ignorePW);

  }

  @Test
  public void testLogout1 () throws ReturnStatusException {
    // each time run this script from SQL server before running this test !
    // update session
    // set status = 'Open'
    // where clientname = 'SBAC'
    // and _efk_Proctor = 2
    // and _fk_browser = 'EB9C0E65-E667-4AE5-870D-D201448C9442'
    // and sessiontype = 0

    String clientName = "SBAC";
    long userKey = 2;
    UUID browserKey = UUID.fromString ("EB9C0E65-E667-4AE5-870D-D201448C9442");
    _proctorRepository.logout (clientName, userKey, browserKey);
  }

  @Test
  public void testLogout2 () throws ReturnStatusException {
    // invalid params, stored proc still returns status = closed which is NOT
    // treated as
    // failure and exception is NOT fired
    String clientName = "kuku";
    long userKey = 33333;
    UUID browserKey = UUID.fromString ("EB9C0E65-E667-4AE5-870D-000000000000");
    _proctorRepository.logout (clientName, userKey, browserKey);
  }

  @Test
  public void testgetRTSUser () throws ReturnStatusException {

    String clientName = "Oregon";
    String userId = "TA1@air.org";
    ProctorUser user = _proctorRepository.getRTSUser (clientName, userId);
    assertTrue(user != null);
  }
}

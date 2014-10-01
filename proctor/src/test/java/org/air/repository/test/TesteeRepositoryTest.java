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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.Testee;
import TDS.Proctor.Sql.Data.Testees;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context.xml")
public class TesteeRepositoryTest
{
  private static final Logger _logger   = LoggerFactory.getLogger (TesteeRepositoryTest.class);

  @Rule
  public ExpectedException    exception = ExpectedException.none ();

  @Autowired
  @Qualifier ("iTesteeRepository")
  ITesteeRepository           _testeeRepository;

  // @Test
  public void testGetTestee1 () throws Exception {
    String clientName = "kuku"; // non-existent
    String testeeId = "111";

    // TesteeRepository Level converts empty empty array of attrs returned
    // byTesteeDLL level
    // into ReturnStatusException
    exception.expect (ReturnStatusException.class);
    @SuppressWarnings ("unused")
    Testee testee = _testeeRepository.getTestee (clientName, testeeId);

  }

  // @Test
  public void testGetTestee2 () throws Exception {
    String clientName = "Oregon"; // valid
    String testeeId = "111"; // non-existent

    // TesteeDLL level returns array of attt recs with empty attribute values,
    // but
    // TesteeRepository level checks for student key to be zero (because this is
    // not a valid student)
    // and it converts such case into ReturnStatusException
    exception.expect (ReturnStatusException.class);
    @SuppressWarnings ("unused")
    Testee testee = _testeeRepository.getTestee (clientName, testeeId);

  }

  // @Test
  public void testGetTestee3 () throws Exception {
    String clientName = "Oregon"; // valid
    String testeeId = "2941406"; // valid

    try {
      Testee testee = _testeeRepository.getTestee (clientName, testeeId);
      assertTrue(testee != null);
      assertTrue(testee.getTesteeAttributes() != null);

    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  // @Test
  public void testGetSchoolTestees1 () throws Exception {

    String clientName = "Oregon";
    String schoolKey = "22645"; // non-existent, expect testees array of size 0
    String grade = null;
    String firstName = "Michael";
    String lastName = "Nelson";

    try {
      Testees testees = _testeeRepository.getSchoolTestees (clientName, schoolKey, grade, firstName, lastName);
      assertTrue(testees.size () == 0);
    } catch (ReturnStatusException re) {
      _logger.error (re.getMessage ());
      throw re;
    }
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetSchoolTestees2 () throws Exception {

    String clientName = "Oregon";
    String schoolKey = "22654"; // valid schoolKey, expect testees array of non-zero
    String grade = null;
    String firstName = "Michael";
    String lastName = "Nelson";

    try {
      Testees testees = _testeeRepository.getSchoolTestees (clientName, schoolKey, grade, firstName, lastName);
      assertTrue(testees.size () > 0);
    } catch (ReturnStatusException re) {
      _logger.error (re.getMessage ());
      throw re;
    }
  }

  // @Test
  public void testGetSchoolTestees3 () throws Exception {

    String clientName = "Oregon";
    String schoolKey = "4736"; // valid schoolKey, expect testees array of non-zero
    String grade = "8";
    String firstName = "Kyle";
    String lastName = "Tardie";

    try {
      Testees testees = _testeeRepository.getSchoolTestees (clientName, schoolKey, grade, firstName, lastName);
      assertTrue(testees.size () > 0);
    } catch (ReturnStatusException re) {
      _logger.error (re.getMessage ());
      throw re;
    }
  }

  // @Test
  public void testGetSchoolTestees4 () throws Exception {

    String clientName = "abs"; // invalid clientName, expect status=failed
    String schoolKey = "4736";
    String grade = "8";
    String firstName = "Kyle";
    String lastName = "Tardie";

    exception.expect (ReturnStatusException.class);
    @SuppressWarnings ("unused")
    Testees testees = _testeeRepository.getSchoolTestees (clientName, schoolKey, grade, firstName, lastName);
  }

}

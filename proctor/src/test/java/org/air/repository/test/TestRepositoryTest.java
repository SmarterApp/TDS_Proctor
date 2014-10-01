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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.Segments;
import TDS.Proctor.Sql.Data.Abstractions.ITestRepository;
import TDS.Proctor.Sql.Data.Accommodations.Accs;

/**
 * @author temp_ukommineni
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context.xml")
public class TestRepositoryTest
{
  @Autowired
  @Qualifier ("iTestRepository")
  private ITestRepository     testRepository = null;
  private static final Logger _logger        = LoggerFactory.getLogger (TestRepositoryTest.class);

  @Test
  // success case 1
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetAllTests () throws Exception {
    /*
     * ConfigurationSection appSettings = ConfigurationManager.getInstance
     * ().getAppSettings (); appSettings.<Integer>get ("SessionType");
     */
    String clientname = "Oregon";
    int sessionType = 0;
    List<TDS.Proctor.Sql.Data.Test> tests = testRepository.getSelectableTests (clientname, sessionType, -1L);
    _logger.info ("GetAllTests result ...." + tests.size ());
    assertTrue(tests.size () > 1);
  }

  @Test
  // success case 2
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetAllTests1 () throws Exception {
    String clientname = "Oregon";
    int sessionType = 0;
    List<TDS.Proctor.Sql.Data.Test> tests = testRepository.getSelectableTests (clientname, sessionType, -1L);
    _logger.info ("GetAllTests result ...." + tests.size ());
    assertTrue(tests.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetSegments () throws Exception {
    String clientname = "Oregon";
    int sessionType = 0;
    Segments segments = testRepository.getSegments (clientname, sessionType);
    _logger.info ("Segments result SIZE ..  " + segments.size ());
    assertTrue(segments.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetTestAccs () throws Exception {
    String testkey = "(Oregon_PT)OAKS-Math-4-Fall-2012-2013";
    Accs result = testRepository.getTestAccs (testkey);
    _logger.info ("TestAccs result SIZE.." + result.getData ().size ());
    assertTrue(result.getData ().size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetGlobalAccs () throws Exception {
    String clientname = "Hawaii";
    String context = "StudentGlobal";
    Accs result = testRepository.getGlobalAccs (clientname, context);
    _logger.info ("GlobalAccs result SIZE.." + result.getData ().size ());
    assertTrue(result.getData ().size () > 0);
  }
}

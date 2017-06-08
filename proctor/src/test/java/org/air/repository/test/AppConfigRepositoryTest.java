/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.repository.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigRepository;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context.xml")
@Ignore("Requires external resources")
public class AppConfigRepositoryTest
{
  @Autowired
  @Qualifier ("iAppConfigRepository")
  private IAppConfigRepository appConfigRepository = null;
  @SuppressWarnings ("unused")
  private static final Logger  _logger             = LoggerFactory.getLogger (AppConfigRepositoryTest.class);

  @Test
  public void testGetConfigs1 () throws Exception {
    AppConfig appConfig = null;
    String clientName = "Minnesota_PT";
    appConfig = appConfigRepository.getConfigs (clientName);
    assertNotNull (appConfig);
  }

  @Test
  public void testGetConfigs2 () throws Exception {
    AppConfig appConfig = null;
    String clientName = "kuku";
    appConfig = appConfigRepository.getConfigs (clientName);
    assertNull (appConfig);
  }

}

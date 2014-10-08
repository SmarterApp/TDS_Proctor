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
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageRepository;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context.xml")
public class AlertMessageRepositoryTest
{
  @Autowired
  @Qualifier ("iAlertMessageRepository")
  IAlertMessageRepository     alertMessageRepository = null;
  private static final Logger _logger                = LoggerFactory.getLogger (AlertMessageRepositoryTest.class);

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testLoadCurrentMessages () throws Exception {
    AlertMessages msgs = null;
    String clientName = "SBAC";
    int timezoneOffset = 1;
    msgs = alertMessageRepository.loadCurrentMessages (clientName, timezoneOffset);
    assertTrue(msgs.size () > 0);
    _logger.info ("testLoadCurrentMessages SIZE: " + msgs.size ());
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testLoadUnAcknowledgedMessages () throws Exception {
    AlertMessages msgs = null;
    String clientName = "SBAC"; /* per Sai */

    long seed = new Date ().getTime ();
    Random generator = new Random (seed);
    // generator.setSeed (19580427);
    long proctorKey = generator.nextInt ();
    int timezoneOffset = 1; /* whatever ?? */
    msgs = alertMessageRepository.loadUnAcknowledgedMessages (clientName, proctorKey, timezoneOffset);
    assertTrue(msgs.size () > 0);
    _logger.info (" testLoadUnAckowledgedMessages SIZE: " + msgs.size () + " ProctorKey: " + proctorKey);
  }

}

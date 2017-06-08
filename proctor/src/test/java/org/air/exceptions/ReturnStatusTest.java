/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * 
 */
package org.air.exceptions;

// import static org.junit.Assert.assertFalse;
// import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
@Ignore("Requires external resources")
public class ReturnStatusTest
{
  @Autowired
  @Qualifier ("iProctorRepository")
  private IProctorRepository  proctorRepository = null;
  private static final Logger _logger           = LoggerFactory.getLogger (ReturnStatusTest.class);

  // Success test
  @Test
  public void tesGetRTSUser () throws ReturnStatusException {
    ProctorUser user = proctorRepository.getRTSUser ("Oregon", "cswindlehurst@burntriver.k12.or.us");
  }

  // Fail ReturnStatus
  @Test(expected=ReturnStatusException.class)
  public void testGetRTSUserNull () throws ReturnStatusException {
    ProctorUser user = proctorRepository.getRTSUser (null, null);
  }

}

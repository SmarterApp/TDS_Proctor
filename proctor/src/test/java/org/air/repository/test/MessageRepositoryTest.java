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
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.Abstractions.IMessageRepository;
import TDS.Shared.Messages.MessageDTO;

/**
 * @author efurman
 * 
 */
@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context.xml")
public class MessageRepositoryTest
{
  @Autowired
  @Qualifier ("iMessageRepository")
  private IMessageRepository  _messageRepository = null;

  private static final Logger _logger            = LoggerFactory.getLogger (MessageRepositoryTest.class);

  @Test
  public void testGetMessages () throws Exception {
    String language = "ESN"; /* "ENU" */
    String contextList = "Alerts.aspx|Approval.aspx|ApprovedRequests.aspx"; /* "login.aspx|CommonPage" */
    List<MessageDTO> msgs = _messageRepository.getMessages (language, contextList);
    assertTrue(msgs.size () > 0);
    _logger.info ("testGetMessages SIZE: " + msgs.size ());
  }

  @Test
  public void testAllFormatMessage () throws Exception {
    String contextType = "ClientSide";
    String context = "Default.aspx";
    String appKey = "PAUSE_A_TEST";
    String language = "ENU";
    String message = _messageRepository.getMessage (contextType, context, appKey, language);
    assertTrue(message != null);
  }

}

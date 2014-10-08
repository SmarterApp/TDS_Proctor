/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.messages.test;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import TDS.Shared.Messages.MessageDTO;
import TDS.Shared.Messages.MessageXml;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
public class MessageXmlTest
{
  private static final Logger _logger = LoggerFactory.getLogger (MessageXmlTest.class);
  @Autowired
  MessageXml                  messageXml;

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void test () throws Exception {
    Iterable<MessageDTO> myIt = messageXml.load (getClass ().getClassLoader ().getResource ("message.xml").toString ());

    Iterator<MessageDTO> it = myIt.iterator ();
    while (it.hasNext ()) {
      MessageDTO myMsg = it.next ();
      myMsg.getContextType ();
      _logger.info ("contextType" + myMsg.getContextType ());
      myMsg.getContext ();
      _logger.info ("context" + myMsg.getContext ());
      myMsg.getMessageId ();
      _logger.info ("messageId" + myMsg.getMessageId ());
      myMsg.getAppKey ();
      _logger.info ("AppKey" + myMsg.getAppKey ());
      myMsg.getLanguage ();
      _logger.info ("Language" + myMsg.getLanguage ());
    }
    _logger.info (it.next ().toString ());
  }
}

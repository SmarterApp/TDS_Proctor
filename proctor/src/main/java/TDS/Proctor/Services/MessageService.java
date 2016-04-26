/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Sql.Data.Abstractions.IMessageRepository;
import TDS.Proctor.Sql.Data.Abstractions.IMessageService;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.Message;
import TDS.Shared.Messages.MessageContext;
import TDS.Shared.Messages.MessageContextType;
import TDS.Shared.Messages.MessageDTO;
import TDS.Shared.Messages.MessageSystem;
import TDS.Shared.Messages.MessageXml;

public class MessageService implements IMessageService
{

  private static final Logger _logger = LoggerFactory.getLogger(MessageService.class);
  private final IMessageRepository _messageRepository;
  private final MessageSystem      _messageSystem = new MessageSystem ();

  public MessageSystem getMessageSystem () {
    return _messageSystem;
  }

  public MessageService (IMessageRepository messageRepository) {
    _messageRepository = messageRepository;
  }

  // / <summary>
  // / Load the client/system name from SQL.
  // / </summary>

  public MessageSystem load (String language, List<String> contextList) throws ReturnStatusException {
    // get all the messages that match the context
    List<MessageDTO> messageDTOs = null;
    try {
      messageDTOs = _messageRepository.getMessages (language, StringUtils.join (contextList, "|"));
    } catch (ReturnStatusException se) {
      throw se;
    }
    // filter out messages that are just the same
    load (messageDTOs);
    return _messageSystem;
  }

  public String get (String context, String language, String messageKey) throws ReturnStatusException {
    String msgs = null;
    try {
      msgs = _messageRepository.getMessage ("ServerSide", context, messageKey, language);
    } catch (ReturnStatusException se) {
      throw se;
    }
    return msgs;
    // return _messageRepository.GetMessage("ServerSide", context, messageKey,
    // language);
  }

  public MessageSystem loadXml (String xmlPath) throws ReturnStatusException {
    File f = new File (xmlPath);
    if (f.exists ()) {
      MessageXml messageXml = new MessageXml ();
      try {
        messageXml.load (xmlPath);
      } catch (Exception e) {
        ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
        throw new ReturnStatusException (rs);
      }

      load (messageXml.getMessageDTOs ());
    }

    return _messageSystem;
  }

  // / <summary>
  // / Load manually
  // / </summary>
  private void load (Iterable<MessageDTO> messageDTOs) {
    for (MessageDTO messageDTO : messageDTOs) {
      try {

        add (messageDTO);
      } catch (Exception ex) {
        String message = TDSStringUtils.format ("MESSAGES: Error loading translation \"{0}\" ({1}).", messageDTO.getAppKey () != null, messageDTO.getMessageId ());
        _logger.error(message, ex); // todo
      }
    }
  }

  private void add (MessageDTO messageDTO) {
    // get/create context type
    MessageContextType messageContextType = _messageSystem.getMessageContextType (messageDTO.getContextType ());

    if (messageContextType == null) {
      messageContextType = _messageSystem.addMessageContextType (messageDTO.getContextType ());
    }

    // get/create context
    MessageContext messageContext = messageContextType.getContext (messageDTO.getContext ());

    if (messageContext == null) {
      messageContext = messageContextType.AddContext (messageDTO.getContext ());
    }

    // get/create message
    Message message = messageContext.getMessageById (messageDTO.getMessageId ());

    if (message == null) {
      message = messageContext.addMessage (messageDTO.getMessageId (), messageDTO.getAppKey ());
    }

    // get/create translation
    message.addTranslation (messageDTO.getLanguage (), messageDTO.getSubject (), messageDTO.getGrade (), messageDTO.getMessage ());
  }

}

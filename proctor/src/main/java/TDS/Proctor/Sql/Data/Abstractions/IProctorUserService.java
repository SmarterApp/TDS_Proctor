/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import java.util.List;
import java.util.UUID;

import tds.dll.api.TestType;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface IProctorUserService
{

  ProctorUser validate (UUID browserKey, String userID, String password, boolean ignorePW) throws ReturnStatusException;

  boolean logout (long userKey, UUID browserKey) throws ReturnStatusException;

  ProctorUser getRTSUser (String userID) throws ReturnStatusException;

  /**
   * @param recordSystemClient
   * @param clientname
   * @param UserID
   * @param application
   * @param clientIP
   * @param proxyIP
   * @param userAgent
   */
  void logSystemClient (boolean recordSystemClient, String clientname, String UserID, String application, String clientIP, String proxyIP, String userAgent);
  
  void createUser (String userID,String fullName) throws ReturnStatusException;
  
  boolean userAlreadyExists(String userId) throws ReturnStatusException;
  
  public void createAndUpdateProctorIsCurrent(String entityLevel,String entityId, String clientName, Long userKey, List<TestType> testTypeList) throws ReturnStatusException;
  
}

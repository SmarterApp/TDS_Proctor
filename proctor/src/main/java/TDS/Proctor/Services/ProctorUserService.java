/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.dll.api.TestType;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Web.UserCookie;

public class ProctorUserService implements IProctorUserService
{

  private Logger                   logger = LoggerFactory.getLogger (ProctorUserService.class);

  private final IProctorRepository _proctorRepository;
  String                           _clientName;
  String                           _stateCode;

  public ProctorUserService (TDSSettings settings, IProctorRepository proctorRepository) {
    _proctorRepository = proctorRepository;
    _clientName = settings.getClientName ();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * TDS.Proctor.Sql.Data.Abstractions.IProctorUserService#Validate(java.util
   * .UUID, java.lang.String, java.lang.String, boolean)
   */
  public ProctorUser validate (UUID browserKey, String userID, String password, boolean ignorePW) throws ReturnStatusException {
    return validate (browserKey, userID, password, false, ignorePW);
  }
  
  public void createUser (String userID,String fullName) throws ReturnStatusException {
    _proctorRepository.createUser (userID, fullName);
  }
  
  private ProctorUser validate (UUID browserKey, String userID, String password, boolean openSessions, boolean ignorePW) throws ReturnStatusException {
    ProctorUser user = null;
    try {
      user = _proctorRepository.validate (_clientName, browserKey, userID, password, openSessions, ignorePW);
    } catch (ReturnStatusException e) { 
      throw e;
    }
    return user;
  }
  
  public void createAndUpdateProctorIsCurrent(String entityLevel,String entityId, String clientName, Long userKey, TestType testType) throws ReturnStatusException {
    try {
      _proctorRepository.createAndUpdateProctorIsCurrent (entityLevel, entityId, clientName, userKey, testType);
    } catch (ReturnStatusException e) { 
      throw e;
    }
  }
  
  public boolean userAlreadyExists (String userId) throws ReturnStatusException {
    try {
      return _proctorRepository.userAlreadyExists (userId);
    } catch (ReturnStatusException e) { 
      throw e;
    }
  }
  
  
  public boolean logout (long userKey, UUID browserKey) throws ReturnStatusException {
    
    try {
      _proctorRepository.logout (_clientName, userKey, browserKey);
    } catch (ReturnStatusException e) { 
      throw e;
    }
    
    return true;
  }

  public ProctorUser getRTSUser (String userID) throws ReturnStatusException {
    ProctorUser user = null;
    try {
      user = _proctorRepository.getRTSUser (_clientName, userID);
    } catch (ReturnStatusException e) { 
      throw e;
    }
    
    return user;
  }

  public void logSystemClient (boolean recordSystemClient, String clientname, String UserID, String application, String clientIP, String proxyIP, String userAgent) {
    try {
      _proctorRepository.logSystemClient (recordSystemClient, clientname, UserID, application, clientIP, proxyIP, userAgent);
    } catch (ReturnStatusException e) {
      logger.error (e.getMessage ());
      // throw e;
    }

  }

  public static ProctorUser loadUserFromCookie (UserCookie userInfo) // load
  // from
  // cookie
  {
    ProctorUser proctor = new ProctorUser ();
    proctor.setId (userInfo.GetValue ("id"));
    long key;
    try {
      key = Long.parseLong ((String) userInfo.GetValue ("key"));
    } catch (NumberFormatException exp) {
      return null;
    }
    proctor.setKey (key);
    proctor.setFullname (userInfo.GetValue ("fullname"));
    proctor.setPassword (userInfo.GetValue ("pass"));
    boolean bTemp;
    bTemp = Boolean.parseBoolean (userInfo.GetValue ("isAuth"));
    proctor.setAuth (bTemp);
    bTemp = Boolean.parseBoolean (userInfo.GetValue ("isNew"));
    proctor.setNew (bTemp);
    String strSessionKey = userInfo.GetValue ("sKey");
    if (!StringUtils.isEmpty (strSessionKey) || !StringUtils.isBlank (strSessionKey))
      proctor.setSessionKey (UUID.fromString (strSessionKey));
    proctor.setBrowserKey (UUID.fromString (userInfo.GetValue ("bKey")));
    String strRoles = userInfo.GetValue ("tdsRole"); // this is a "|"
    // delimiter list of
    // roles
    String[] roles = StringUtils.split (strRoles, '|');
    proctor.setRoles (new ArrayList<String> (Arrays.asList (roles)));
    proctor.setClientName (userInfo.GetValue ("c"));
    return proctor;
  }

  public static boolean save (ProctorUser proctor, UserCookie userInfo) {
    userInfo.SetValue ("id", proctor.getId ());
    userInfo.SetValue ("key", new Long (proctor.getKey ()).toString ());
    userInfo.SetValue ("fullname", proctor.getFullname ());
    userInfo.SetValue ("password", proctor.getPassword ());
    userInfo.SetValue ("isAuth", new Boolean (proctor.isAuth ()).toString ());
    userInfo.SetValue ("isNew", new Boolean (proctor.isNew ()).toString ());
    userInfo.SetValue ("sKey", proctor.getSessionKey ().toString ());
    userInfo.SetValue ("bKey", proctor.getBrowserKey ().toString ());
    userInfo.SetValue ("tdsRole", StringUtils.join (proctor.getRoles ().toArray (), "|"));
    userInfo.SetValue ("c", proctor.getClientName ());

    return true;
  }

  public static void saveSessionKey (ProctorUser proctor, UserCookie userInfo) {
    userInfo.SetValue ("sKey", proctor.getSessionKey ().toString ());
  }
}

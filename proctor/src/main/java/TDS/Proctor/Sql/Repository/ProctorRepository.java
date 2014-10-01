/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;
import tds.dll.api.IRtsDLL;
import tds.dll.api.TestType;
import tds.dll.mysql.RtsPackageDLL;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorPackageService;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 * 
 */
public class ProctorRepository extends AbstractDAO implements IProctorRepository
{
  private static final Logger  _logger                = LoggerFactory.getLogger (ProctorRepository.class);
  //TODO EF:confirm!
  //@Autowired
  //private ConfigurationManager configurationManager   = null;
  @Autowired
  IProctorDLL                  _pdll                  = null;
  @Autowired
  IRtsDLL                      _rdll                  = null;

  @Autowired
  IProctorPackageService       _proctorPackageService = null;

  public ProctorUser validate (String clientName, UUID browserKey, String userId, String password, boolean openSessions, boolean ignorePW) throws ReturnStatusException {
    ProctorUser user = null;

    // final String sp = "{call P_ValidateProctor(?, ?, ?, ?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      // C# code version did not pass password, openSessions and ignorePW to
      // stored proc

      SingleDataResultSet result = _rdll.P_ValidateProctor_SP (connection, clientName, browserKey, userId);
      ReturnStatusException.getInstanceIfAvailable (result, "The SP P_ValidateProctor did not return any records.");

      Iterator<DbResultRecord> records = result.getRecords ();
      result.setFixNulls (true);
      // get here only if we get valid rows
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        user = new ProctorUser (clientName, browserKey, userId, password);
        // parse proctor data
        // TODO: add logic that sets the following three values to empty strings
        // if they are not in a result set
         user.setKey (record.<Long> get ("userKey"));
        
        if (record.hasColumn ("entityKey")) {
           user.setEntityKey (record.<Long> get ("entityKey"));
        }
        user.setFullname (record.<String> get ("fullName"));
        if (record.hasColumn ("rtspassword"))
          user.setRTSPassword (record.<String> get ("rtspassword")); // get
                                                                     // password
                                                                     // from RTS

        if (record.hasColumn ("URL")) // satellite URL
          user.setSatelliteURL (record.<String> get ("URL"));

        user.setAuth (true);
        user.setNew (true);
        user.addRole ("proctor"); // TDS Role for the proctor

        // Retrieve proctor package from remote service and store for local use
        /*if ( _rdll instanceof RtsPackageDLL ) {
          final String proctorPackage = _proctorPackageService.getProctorPackageString (entityLevel, entityId);
          ( (RtsPackageDLL) _rdll ).createAndUpdateProctorIsCurrent (connection, user.getKey (), clientName, proctorPackage);
        }*/

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return user;
  }
  
  public void createAndUpdateProctorIsCurrent(String entityLevel,String entityId, String clientName, Long userKey, TestType testType) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      // Retrieve proctor package from remote service and store for local use
      if ( _rdll instanceof RtsPackageDLL ) {
        final String proctorPackage = _proctorPackageService.getProctorPackageString (entityLevel, entityId);
       
        if (proctorPackage != null) {
           _rdll.createAndUpdateProctorIsCurrent (connection, userKey, clientName, proctorPackage, testType);
         }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
  
  public int createUser(String userId,String fullName) throws ReturnStatusException{
    try (SQLConnection connection = getSQLConnection ()) {
      // C# code version did not pass password, openSessions and ignorePW to
      // stored proc

      return _rdll.createUser (connection, userId,fullName);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
  
  public boolean userAlreadyExists(String userId) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      // C# code version did not pass password, openSessions and ignorePW to
      // stored proc

      return _rdll.userAlreadyExists (connection, userId);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public void logout (String clientName, long userKey, UUID browserKey) throws ReturnStatusException {
    //String sessionTypeStr = configurationManager.getAppSettings ().get ("SessionType");
    int sessionType = 0;
    sessionType = getTdsSettings ().getSessionType ();
    
    //try {
    //  sessionType = Integer.parseInt (sessionTypeStr);
    //} catch (NumberFormatException ne) {
    //  throw new ReturnStatusException (ne);
    //}
    
    // final String sp = "{call P_LogoutProctor(?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_LogOutProctor_SP (connection, clientName, userKey, browserKey, sessionType);

      // if callstatement is null, everything is ok and no closed sessions
      // found
      // if callstatement is not null, we got back status 'closed' or status
      // 'failed'
      ReturnStatusException.getInstanceIfAvailable (result);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public ProctorUser getRTSUser (String clientName, String userId) throws ReturnStatusException {

    ProctorUser user = null;
    // final String sp = "{call GetRTSUser(?, ?) }";

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.GetRTSUser_SP (connection, clientName, userId);

      ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();

      // get here if status is NOT failed, but we can still get empty reader
      // TODO do we want to throw out exception if user is not found or return
      // user=null?
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        user = new ProctorUser ();
        user.setClientName (clientName);
        user.setId (userId);
        user.setKey (record.<Long> get ("userKey"));
        user.setFullname (record.<String> get ("fullName"));
        user.setPassword (record.<String> get ("password"));
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return user;

  }

  public void logSystemClient (boolean recordSystemClient, String clientName, String UserId, String application, String clientIp, String proxyIp, String userAgent) throws ReturnStatusException {
    // final String sp = "{call _RecordSystemClient(?, ?, ?, ?, ?, ?) }";

    if (!recordSystemClient)
      return;

    try (SQLConnection connection = getSQLConnection ()) {
      _pdll._RecordSystemClient_SP (connection, clientName, application, UserId, clientIp, proxyIp, userAgent);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}

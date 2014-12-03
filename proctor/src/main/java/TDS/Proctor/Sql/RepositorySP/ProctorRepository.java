/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.RepositorySP;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Sql.DbHelper;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;

import tds.dll.api.TestType;

public class ProctorRepository extends AbstractDAO implements IProctorRepository
{

private static final Logger _logger = LoggerFactory.getLogger(ProctorRepository.class);

  public ProctorUser validate (String clientName, UUID browserKey, String userID, String password, boolean openSessions, boolean ignorePW) throws ReturnStatusException {
    ColumnResultSet reader = null;
    // final String sp = "{call P_ValidateProctor(?, ?, ?, ?, ?, ?) }";
    final String sp = "{call P_ValidateProctor(?, ?, ?) }";
    ProctorUser user = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientName);
        callstatement.setNString (2, browserKey.toString ());
        callstatement.setString (3, userID);
        // TODO in .NET code the following three parameters have
        // been commented out. Commenting themout here too
        // callstatement.setString (4, password);
        // callstatement.setBoolean (5, openSessions);
        // callstatement.setBoolean (6, ignorePW);

        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
       
        /*
        ReturnStatus returnStatus = ReturnStatus.parse (reader);
        if (returnStatus != null && "failed".equalsIgnoreCase (returnStatus.getStatus ())) {
          throw new ReturnStatusException (new Exception ("Failed attempt to validate proctor"));
        }
        */
        ReturnStatusException.getInstanceIfAvailable (reader, "The SP P_ValidateProctor did not return any records.");
         
          reader.setFixNulls (true);
          reader.next ();

        user = new ProctorUser (clientName, browserKey, userID, password);
        // parse proctor data
        // TODO: add logic that sets the following three values to empty
        // strings
        // if they are not in a result set
        user.setKey (reader.getLONG ("userKey"));

        user.setFullname (reader.getString ("fullName"));

        if (reader.hasColumn ("URL")) // satellite URL
          user.setSatelliteURL (reader.getString ("URL"));

        user.setAuth (true);
        user.setNew (true);
        user.addRole ("proctor"); // TDS Role for the proctor

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return user;
  }
  
  //TODO Implement when SQL implementation of following method required.
  public void createAndUpdateProctorIsCurrent(String entityLevel,String entityId, String clientName, Long userKey, List<TestType> testTypeList) throws ReturnStatusException {
    
  }

  public void logout (String clientName, long userKey, UUID browserKey) throws ReturnStatusException {
    ColumnResultSet reader = null;
    final String sp = "{call P_LogoutProctor(?, ?, ?) }";

    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientName);
        callstatement.setLong (2, userKey);
        callstatement.setNString (3, browserKey.toString ());

        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to logout proctor.");

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public ProctorUser getRTSUser (String clientName, String userID) throws ReturnStatusException {
    ColumnResultSet reader = null;
    final String sp = "{call GetRTSUser(?, ?) }";

    ProctorUser user = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientName);
        callstatement.setString (2, userID);

        if (callstatement.execute ())
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting  RTS user");
        
        if (reader.next ()) { 

          user = new ProctorUser ();
          user.setClientName (clientName);
          user.setId (userID);
          user.setKey (reader.getLong ("userKey"));
          user.setFullname (reader.getString ("fullName"));
          user.setPassword (reader.getString ("password"));
        } 
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return user;

  }

  public void logSystemClient (boolean recordSystemClient, String clientname, String UserID, String application, String clientIP, String proxyIP, String userAgent) throws ReturnStatusException {
    final String sp = "{call _RecordSystemClient(?, ?, ?, ?, ?, ?) }";

    if (!recordSystemClient)
      return;

    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, (String) DbHelper.isNullifyString (clientname));
        callstatement.setString (2, (String) DbHelper.isNullifyString (application));
        callstatement.setString (3, (String) DbHelper.isNullifyString (UserID));
        callstatement.setString (4, (String) DbHelper.isNullifyString (clientIP));
        callstatement.setString (5, (String) DbHelper.isNullifyString (proxyIP));
        callstatement.setString (6, (String) DbHelper.isNullifyString (userAgent));

        callstatement.execute ();
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
  //TODO Implementation required when integrating with MSSQL
  public int createUser(String userId,String fullName) throws ReturnStatusException{
    return 0;
  }
//TODO Implementation required when integrating with MSSQL
  public boolean userAlreadyExists(String userId) throws ReturnStatusException{
    return false;
  }
  
}

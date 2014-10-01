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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import AIR.Common.Utilities.Dates;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionRepository;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;

public class TestSessionRepository extends AbstractDAO implements ITestSessionRepository
{

private static final Logger _logger = LoggerFactory.getLogger(TestSessionRepository.class);

  public List<TestSession> getCurrentSessions (String clientname, long proctorKey, int sessionType) throws ReturnStatusException {

    ColumnResultSet reader = null;
    List<TestSession> result = null;
    final String CMD_GET_CURRENT_SESSIONS = "{call P_GetCurrentSessions(?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_CURRENT_SESSIONS)) {
        callstatement.setString (1, clientname);
        callstatement.setLong (2, proctorKey);
        callstatement.setInt (3, sessionType);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to get the current sessions");
        result = new ArrayList<TestSession> ();
        while (reader.next ()) {
          TestSession session = new TestSession (proctorKey);
          UUID sessionKey = UUID.fromString (reader.getString ("_Key"));
          session.setKey (sessionKey);
          session.setBrowserKey (UUID.fromString (reader.getString ("browserKey")));
          session.setId (reader.getString ("SessionID"));
          session.setStatus (reader.getString ("Status"));
          session.setName (reader.getString ("sessionName"));
          session.setNeedapproval (reader.getInt ("NeedApproval"));
          result.add (session);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  public ReturnStatus pauseSession (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    ColumnResultSet reader = null;
    ReturnStatus returnStatus = null;
    final String CMD_GET_PAUSE_SESSION = "{call P_PauseSession(?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_PAUSE_SESSION)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to pause the session");

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public TestSession createSession (String clientName, long proctorKey, UUID browserKey, String sessionName, String proctorID, String proctorName, Date dateBegin, Date dateEnd)
      throws ReturnStatusException {
    // NOTE: we need to check again to make sure that no session exists for
    // this proctor???
    // We also need to check for the null or empty user key, ID, and name
    ColumnResultSet reader = null;
    ReturnStatus _returnStatus = null;
    TestSession testSession = new TestSession ();
    if (StringUtils.isEmpty (proctorID) || StringUtils.isEmpty (proctorName)) {
      _returnStatus = new ReturnStatus ("failed", "UserCookieDataDamaged", "CommonPage");
      throw new ReturnStatusException (_returnStatus);
    }

    final String CMD_CREATE_SESSION = "{call P_CreateSession(?, ?, ?, ?, ?, ?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_CREATE_SESSION)) {
        callstatement.setString (1, clientName);
        callstatement.setNString (2, browserKey.toString ());
        callstatement.setString (3, sessionName);
        callstatement.setLong (4, proctorKey);
        callstatement.setString (5, proctorID);
        callstatement.setString (6, proctorName);
        callstatement.setTimestamp (7, Dates.getTimestamp (dateBegin));
        callstatement.setTimestamp (8, Dates.getTimestamp (dateEnd));
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to create the session");
        // check for errors
        reader.next ();
        testSession = new TestSession (proctorKey, browserKey);
        testSession.setKey (UUID.fromString (reader.getString ("sessionKey")));
        testSession.setId (reader.getString ("sessionID"));
        testSession.setStatus (reader.getString ("sessionStatus"));
        // testSession.datebegin = dateBegin;
        // testSession.dateend = dateEnd;
        testSession.setName (sessionName);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testSession;
  }

  public ReturnStatus insertSessionTest (UUID sessionKey, long proctorKey, UUID browserKey, String testKey, String testID) throws ReturnStatusException {
    ColumnResultSet reader = null;
    ReturnStatus returnStatus = null;
    final String CMD_INSERT_SESSION_TEST = "{call P_InsertSessionTest(?, ?, ?, ?, ? ) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_INSERT_SESSION_TEST)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setString (4, testKey);
        callstatement.setString (5, testID);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to insert the session test");

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public List<String> getSessionTests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    ColumnResultSet reader = null;
    List<String> sessionTests = null;
    final String CMD_GET_SESSION_TESTS = "{call P_GetSessionTests(?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_SESSION_TESTS)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed to get the session tests");
        sessionTests = new ArrayList<String> ();
        while (reader.next ()) {
          // load stuff here ...
          sessionTests.add (reader.getString ("TestKey"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return sessionTests;
  }

  public ReturnStatus setSessionDateVisited (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    ColumnResultSet reader = null;
    ReturnStatus returnStatus = null;

    final String CMD_SET_SESSION_DATE_VISITED = "{call P_SetSessionDateVisited(?, ?, ? ) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_SET_SESSION_DATE_VISITED)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());

        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc) {
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed to set the session date");
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public boolean hasActiveOpps (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    ColumnResultSet reader = null;
    final String CMD_GET_ACTIVE_COUNT = "{call P_GetActiveCount(?, ?, ? ) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_ACTIVE_COUNT)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc) 
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "has no active opportunities");                 
          if (!reader.next ())
            return false;
          if (hasColumn (reader, "active"))
            return (reader.getInt ("active") > 0);

          }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return false;
  }

  public ReturnStatus handoffSession (String clientname, long proctorKey, UUID browserKey, String sessionID, _Ref<UUID> sessionKey) throws ReturnStatusException {
    ColumnResultSet reader = null;
    ReturnStatus returnStatus = null;
    UUID newSessionKey = null;

    final String cmd = "BEGIN; SET NOCOUNT ON; exec P_HandoffSession '{0}', '{1}', '{2}', '{3}'; end;";
    String sqlQuery = TDSStringUtils.format (cmd, clientname, "" + proctorKey, sessionID, browserKey.toString ());

    try (SQLConnection connection = getSQLConnection ()) {
      try (Statement callstatement = connection.createStatement ()) {

        if (callstatement.execute (sqlQuery))
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatusException.getInstanceIfAvailable (reader, "has no active opportunities");
        if (reader.next()) {
          if (hasColumn (reader, "sessionKey"))
            newSessionKey = UUID.fromString (reader.getString ("sessionKey"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    
    sessionKey.set (newSessionKey);
    return returnStatus;
  }

}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionRepository;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Proctor.performance.services.ProctorUserService;
import TDS.Proctor.performance.services.TestSessionService;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IProctorDLL;

/**
 * @author temp_ukommineni
 *
 */
public class TestSessionRepository extends AbstractDAO implements ITestSessionRepository
{

  private static final Logger          _logger = LoggerFactory.getLogger (TestSessionRepository.class);
  @Autowired
  ICommonDLL  _cdll  = null;
  @Autowired
  IProctorDLL  _pdll  = null;

  @Autowired
  private ProctorUserService proctorUserService;

  @Autowired
  private TestSessionService testSessionService;

  public List<TestSession> getCurrentSessions (String clientname, long proctorKey, int sessionType) throws ReturnStatusException {
    List<TestSession> testSession = new ArrayList<TestSession> ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_GetCurrentSessions_SP (connection, clientname, proctorKey, sessionType);
      //ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TestSession session = new TestSession (proctorKey);
        UUID sessionKey = record.<UUID> get ("_Key");
        session.setKey (sessionKey);
        session.setBrowserKey (record.<UUID> get ("browserKey"));
        session.setId (record.<String> get ("SessionID"));
        session.setStatus (record.<String> get ("Status"));
        session.setName (record.<String> get ("sessionName"));
        session.setNeedapproval (record.<Integer> get ("NeedApproval"));
        testSession.add (session);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testSession;
  }

  //TODO make void
  public ReturnStatus pauseSession (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _cdll.P_PauseSession_SP (connection, sessionKey, proctorKey, browserKey);
      ReturnStatusException.getInstanceIfAvailable (result);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return null;
  }

  public TestSession createSession (String clientName, long proctorKey, UUID browserKey, String sessionName, String proctorID, String proctorName, Date dateBegin, Date dateEnd)
      throws ReturnStatusException {
    // NOTE: we need to check again to make sure that no session exists for this proctor???
    // We also need to check for the null or empty user key, ID, and name
    TestSession testSession = null;
    if (StringUtils.isEmpty (proctorID) || StringUtils.isEmpty (proctorName)) {
      throw new ReturnStatusException (new ReturnStatus ("failed", "UserCookieDataDamaged", "CommonPage"));
    }
    try {
//      SingleDataResultSet result = _pdll.P_CreateSession_SP (connection, clientName, browserKey, sessionName, proctorKey, proctorID, proctorName, dateBegin, dateEnd, 0);
      SingleDataResultSet result = testSessionService.createSession(clientName, browserKey, sessionName, proctorKey, proctorID, proctorName, dateBegin, dateEnd, 0);
      ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        testSession = new TestSession (proctorKey, browserKey);
        testSession.setKey (record.<UUID> get ("sessionKey"));
        testSession.setId (record.<String> get ("sessionID"));
        testSession.setStatus (record.<String> get ("sessionStatus"));
        // testSession.datebegin = dateBegin;
        // testSession.dateend = dateEnd;
        testSession.setName (sessionName);
      }
    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testSession;
  }

  //TODO make void
  public ReturnStatus insertSessionTest (UUID sessionKey, long proctorKey, UUID browserKey, String testKey, String testID) throws ReturnStatusException {
    try {
//      SingleDataResultSet result = _pdll.P_InsertSessionTest_SP (connection, sessionKey, proctorKey, browserKey, testKey, testID);
      SingleDataResultSet result = testSessionService.insertSessionTest(sessionKey, proctorKey, browserKey, testKey, testID);
      ReturnStatusException.getInstanceIfAvailable (result);
    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return null;
  }

  public List<String> getSessionTests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    List<String> sessionTests = null;
    try {
//      SingleDataResultSet result = _pdll.P_GetSessionTests_SP (connection, sessionKey, proctorKey, browserKey);
      SingleDataResultSet result = testSessionService.getSessionTests(sessionKey, proctorKey, browserKey);
      ReturnStatusException.getInstanceIfAvailable (result);

      sessionTests = new ArrayList<String> ();
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        sessionTests.add (record.<String> get ("TestKey"));
      }
    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return sessionTests;
  }

  //TODO make void
  public ReturnStatus setSessionDateVisited (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    try {
      proctorUserService.updateDateVisited(sessionKey, proctorKey, browserKey);
    } catch (Exception e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return null;

//    try (SQLConnection connection = getSQLConnection ()) {
//      SingleDataResultSet result = _pdll.P_SetSessionDateVisited_SP (connection, sessionKey, proctorKey, browserKey);
//      ReturnStatusException.getInstanceIfAvailable (result);
//    } catch (SQLException e) {
//      _logger.error (e.getMessage ());
//      throw new ReturnStatusException (e);
//    }
//    return null;
  }

  public boolean hasActiveOpps (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_GetActiveCount_SP (connection, sessionKey, proctorKey, browserKey);
      ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.hasColumn ("active")) {
        	Integer nActive = record.<Integer> get ("active");
        	return (nActive > 0);
        }
      }
      // if (hasColumn (reader, "active"))
      // return (reader.getInt ("active") > 0);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return false;
  }

  //TODO make void
  public ReturnStatus handoffSession (String clientname, long proctorKey, UUID browserKey, String sessionID, _Ref<UUID> sessionKey) throws ReturnStatusException {

    UUID newSessionKey = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_HandOffSession_SP (connection, clientname, proctorKey, sessionID, browserKey);
      ReturnStatusException.getInstanceIfAvailable (result);

      Iterator<DbResultRecord> records = result.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record != null) {
          if (record.hasColumn ("sessionKey")) {
            newSessionKey = record.<UUID> get ("sessionKey");
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    sessionKey.set (newSessionKey);
    return null;
  }
}

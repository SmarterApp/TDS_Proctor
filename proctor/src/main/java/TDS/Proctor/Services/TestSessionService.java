/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("legacyTestSessionService")
@Scope("prototype")
public class TestSessionService implements ITestSessionService

{
  private final ITestSessionRepository _repository;
  String                               _clientName;
  int                                  _sessionType = 0;

  @Autowired
  public TestSessionService (TDSSettings settings, ITestSessionRepository repository) {
    _repository = repository;
    _clientName = settings.getClientName ();
    _sessionType = settings.getSessionType ();
  }

  public List<TestSession> getCurrentSessions (long proctorKey) throws ReturnStatusException {
    List<TestSession> result = null;
    try {
      result = _repository.getCurrentSessions (_clientName, proctorKey, _sessionType);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public TestSession getCurrentSession (long proctorKey, UUID curBrowserKey) throws ReturnStatusException {
    List<TestSession> result = null;

    try {
      result = _repository.getCurrentSessions (_clientName, proctorKey, _sessionType);

    } catch (ReturnStatusException re) {
      throw re;
    }

    boolean bProctorHasSession = false;
    for (TestSession session : result) {
      bProctorHasSession = true;
      if (curBrowserKey.equals (session.getBrowserKey ()))
        return session;
    }
    if (bProctorHasSession)// if someone other browser created the session
      // before this browser does
      throw new ReturnStatusException (new ReturnStatus ("failed", "There already is an active session for this user."));
    return null;
  }

  public boolean pauseSession (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    try {
      _repository.pauseSession (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return true;
  }

  public TestSession createSession (long proctorKey, UUID browserKey, String sessionName, String proctorID, String proctorName, Date dateBegin, Date dateEnd) throws ReturnStatusException {
    TestSession result = null;
    try {
      result = _repository.createSession (_clientName, proctorKey, browserKey, sessionName, proctorID, proctorName, dateBegin, dateEnd);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public boolean insertSessionTest (UUID sessionKey, long proctorKey, UUID browserKey, String testKey, String testID) throws ReturnStatusException {
    try {
      _repository.insertSessionTest (sessionKey, proctorKey, browserKey, testKey, testID);
    } catch (ReturnStatusException re) {
      throw re;
    }

    return true;
  }

  public List<String> getSessionTests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    List<String> result = null;

    try {
      result = _repository.getSessionTests (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public boolean setSessionDateVisited (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    try {
      _repository.setSessionDateVisited (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return true;
  }

  public boolean hasActiveOpps (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
	boolean hasActOpps = true;
	try {
     hasActOpps = _repository.hasActiveOpps (sessionKey, proctorKey, browserKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return hasActOpps;
  }

  public boolean handoffSession (long proctorKey, UUID browserKey, String sessionID, _Ref<UUID> sessionKey) throws ReturnStatusException {
    try {
      _repository.handoffSession (_clientName, proctorKey, browserKey, sessionID, sessionKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return true;
  }
}

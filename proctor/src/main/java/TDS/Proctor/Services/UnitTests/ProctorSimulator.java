/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.UnitTests;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Services.UnitTests.student.sql.data.TestSelection;
import TDS.Proctor.Services.UnitTests.student.sql.data.TestSelectionList;
import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.District;
import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grade;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.Institution;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.School;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Proctor.Sql.Data.SessionInstance;
import TDS.Proctor.Sql.Data.Test;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageService;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigService;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionService;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.Sql.Data.Abstractions.ITestService;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;

// simulate a proctor
public class ProctorSimulator extends TimerTask
{

  private final String    _email;
  private final String    _password;
  private final UUID      _browserKey;
  private long            _proctorKey      = -1;
  private SessionInstance _sessionInstance = null;

  private static Timer    _pollTimer;
  
  @Autowired
  ApplicationContext _context;
  @Autowired
  AbstractConnectionManager _connectionManager;

  // Student
  String                  clientname       = "Oregon";
private static final Logger _logger = LoggerFactory.getLogger(ProctorSimulator.class);

  public ProctorSimulator (String email, String password) {
    _email = email;
    _password = password;
    _browserKey = UUID.randomUUID ();
  }

  public void start () throws ReturnStatusException {

	  System.out.println("_context::"+_context);
	  System.out.println("_connectionManager::"+_connectionManager);
    IProctorUserService proctorService = _context.getBean("iProctorUserService", IProctorUserService.class);
    // SP: GetRTSUser
    ProctorUser user = proctorService.getRTSUser (_email);

    _logger.info (TDSStringUtils.format ("GetRTSUser \"{0}\" ({1})", user.getFullname (), user.getId ()));
    // login SP: P_ValidateProctor
    ProctorUser proctor = proctorService.validate (_browserKey, _email, _password, true);
    _logger.info (TDSStringUtils.format ("Login \"{0}\" ({1})", proctor.getFullname (), proctor.getId ()));
    _proctorKey = proctor.getKey ();

    // appConfig: SP: P_GetConfigs
    IAppConfigService appConfigService = _context.getBean("iAppConfigService", IAppConfigService.class);
    AppConfig appConfig = appConfigService.getConfigs ();
    _logger.info (TDSStringUtils.format ("GetConfigs \"{0}\" ", appConfig.toString ()));

    // alert messages SP: P_GetCurrentAlertMessages and
    // P_GetUnAcknowledgedAlertMessages

    IAlertMessageService alertMsgService = _context.getBean("iAlertMessageService", IAlertMessageService.class);
    AlertMessages alertMsgs = alertMsgService.getCurrentMessages (appConfig.getTimeZoneOffset ());
    _logger.info (TDSStringUtils.format ("GetCurrentMessages \"{0}\"", alertMsgs.size ()));
    alertMsgs = alertMsgService.getUnAcknowledgedMessages (proctor.getKey (), appConfig.getTimeZoneOffset ());
    _logger.info (TDSStringUtils.format ("GetUnAcknowledgedMessages \"{0}\"", alertMsgs.size ()));

    // institution
    IInstitutionService instService = _context.getBean("iInstitutionService", IInstitutionService.class);
    Districts districts = instService.getDistricts (); // SP: R_GetDistricts
    if (districts != null && districts.size () > 0) {
      District district = districts.get (0);
      _logger.info (TDSStringUtils.format ("GetDistricts \"{0}\"", district.toString ()));
      Schools schools = instService.getSchools (district.getKey ()); // SP:
      // R_GetDistrictSchools
      if (schools != null && schools.size () > 0) {
        School school = schools.get (0);
        _logger.info (TDSStringUtils.format ("GetSchools \"{0}\"", school.toString ()));
        Grades grades = instService.getGrades (school.getKey ()); // SP:
        // R_GetSchoolGrades
        if (grades != null && grades.size () > 0) {
          Grade grade = grades.get (0);
          _logger.info (TDSStringUtils.format ("GetGrades \"{0}\"", grade.toString ()));
        }
      }
    }
    List<String> userCurrentRoles = new ArrayList<String> ();
    userCurrentRoles.add ("proctor");
    InstitutionList instList = instService.getUserInstitutions (proctor.getKey (), userCurrentRoles); // SP:
                                                                                                      // GetRTSUserRoles
    if (instList != null && instList.size () > 0) {
      Institution inst = instList.get (0);
      _logger.info (TDSStringUtils.format ("GetUserInstitutions \"{0}\"", inst.toString ()));
    }
    instList = instService.getInstitutions (); // SP: R_GetDistricts
    if (instList != null && instList.size () > 0) {
      Institution inst = instList.get (0);
      _logger.info (TDSStringUtils.format ("GetInstitutions \"{0}\"", inst.toString ()));
    }

    // open the session
    ITestSessionService testSessionService = _context.getBean("iTestSessionService", ITestSessionService.class);
    List<TestSession> testSessions = testSessionService.getCurrentSessions (proctor.getKey ());
    if (testSessions != null)
      for (TestSession session : testSessions) {
        _logger.info (TDSStringUtils.format ("Logout [{0}]", session.getId ()));
        proctorService.logout (proctor.getKey (), session.getBrowserKey ()); // SP:
        // P_LogoutProctor logout all existing sessions if any
      }

    // SP: P_CreateSession
    _logger.info (TDSStringUtils.format ("CreateSession \"{0}\"", proctor.getId ()));
    TestSession testSession = testSessionService.createSession (proctor.getKey (), _browserKey, "Proctor Sim", proctor.getId (), proctor.getFullname (), null, null);
    _logger.info (TDSStringUtils.format ("Session ID [{0}][{1}]", testSession.getId (), testSession.getKey ()));

    // add all tests to the session

    ITestService testService = _context.getBean("iTestService", ITestService.class);
    _logger.info (TDSStringUtils.format ("GetSelectableTests \"{0}\"", proctor.getId ()));
    List<Test> tests = testService.getSelectableTests (proctor.getKey ()); // SP:
    // P_GetAllTests
    for (Test test : tests) {
      _logger.info (TDSStringUtils.format ("InsertSessionTest \"{0}\"", test.getId ()));
      // SP: P_InsertSessionTest
      testSessionService.insertSessionTest ((UUID) testSession.getKey (), proctor.getKey (), testSession.getBrowserKey (), test.get_key (), test.getId ());
    }

    // start polling
    _sessionInstance = new SessionInstance ((UUID) testSession.getKey (), testSession.getProctorKey (), testSession.getBrowserKey ());
    _pollTimer = new Timer ();
    _pollTimer.schedule (this, 10000);

    // StudentSimulator studentSimulator = new StudentSimulator(clientname, id,
    // firstName, testSession);
    // boolean studentloginvalue = studentSimulator.studentLogin();
    String keyValues = "ID:9999999001;FirstName:JAMES";
    simulateStudent (clientname, keyValues, testSession);
  }

  /*
   * replacement for Poll method in .NET code.
   * 
   * //private void Poll(Object state) throws ReturnStatusException {
   */
  public void run () {
    try {
      // SessionInstance sessionInstance = state as SessionInstance;
      // _logger.info("Poll Session Key [{0}]", sessionInstance.Key);

      ITestOpportunityService testOpportunityService = _context.getBean("iTestOpportunityService", ITestOpportunityService.class);
      TestOpps testOpps = testOpportunityService.getTestsForApproval (_sessionInstance.getKey (), _sessionInstance.getProctorKey (), _sessionInstance.getBrowserKey ());
      if (testOpps == null || testOpps.size () < 1) {
        _logger.info (TDSStringUtils.format ("GetTestsForApproval, no student waiting[{0}]", _sessionInstance.getKey ()));
        return;
      }

      for (TestOpportunity testOpp : testOpps) {
        _logger.info (TDSStringUtils.format ("ApproveOpportunity [{0}][{1}][{2}]", testOpp.getOppKey (), testOpp.getSsid (), testOpp.getTestID ()));
        testOpportunityService.approveOpportunity (testOpp.getOppKey (), _sessionInstance.getKey (), _sessionInstance.getProctorKey (), _sessionInstance.getBrowserKey ());
      }
    } catch (ReturnStatusException rexp) {
      throw new RuntimeReturnStatusException (rexp);
    }
  }

  public void logout () throws ReturnStatusException {
    _logger.info (TDSStringUtils.format ("Logout [{0}]", _email));
    if (_proctorKey < 0)
      // return;
      _logger.info (TDSStringUtils.format ("Logout [{0}][{1}]", _email, _proctorKey));
    IProctorUserService proctorService = _context.getBean("iProctorUserService", IProctorUserService.class);
    proctorService.logout (_proctorKey, _browserKey); // SP: P_LogoutProctor
  }

  private void simulateStudent (String clientname, String keyValues, TestSession testSession) throws ReturnStatusException {

    StudentSimulator studentSimulator = new StudentSimulator (clientname, testSession, keyValues);
    studentSimulator.studentLogin ();
    /************ Start Proctor Action **************/
    // TODO If we ever movie the simulateStudent method to StudentSimulator then
    // this part needs to be taken out.
    // get all eligible tests and insert them into the session.
    TestSelectionList eligibleTestsListData = studentSimulator.getEligibleTests ();
    for (int i = 0; i < eligibleTestsListData.size (); i++) {
      TestSelection eligibleTestsData = eligibleTestsListData.get (i);
      insertSessionTest (testSession.getKey ().toString (), testSession.getProctorKey (), testSession.getBrowserKey ().toString (), eligibleTestsData.getTestKey (), eligibleTestsData.getTestStatus ());
    }
    /************ End Proctor Action **************/

    boolean testOpenRequested = studentSimulator.openRandomTestOpportunity ();
    if (testOpenRequested) {
      // validate access code in here.
      while (!studentSimulator.validateAccessTask ()) {
        try {
          Thread.sleep (1000);
        } catch (InterruptedException exp) {
          _logger.error ("ERROR:" + exp.getMessage ());
        }
      }
    }
  }

  private boolean insertSessionTest (String Key, Long ProctorKey, String BrowserKey, String testKey, String testID) throws ReturnStatusException {


    ResultSet reader = null;
    boolean studentloginvalue = false;
    String CMD_INSERT_SESSIONTEST = "{call P_InsertSessionTest(?, ?, ?, ?, ?) }";
    try (SQLConnection connection = _connectionManager.getConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_INSERT_SESSIONTEST)) {
        callstatement.setString (1, Key);
        callstatement.setLong (2, ProctorKey);
        callstatement.setNString (3, BrowserKey);
        callstatement.setString (4, testKey);
        callstatement.setString (5, testID);
        callstatement.execute ();
        reader = callstatement.getResultSet ();

        if (reader != null) {
          studentloginvalue = true;
        }
      }

    } catch (SQLException e) {
      _logger.error (TDSStringUtils.format ("ERROR:", e.getMessage ()));
      ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
      throw new ReturnStatusException (rs);
    }
    return studentloginvalue;
  }

}

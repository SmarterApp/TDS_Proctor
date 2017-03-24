/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.Handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import AIR.Common.DB.SQLConnection;
import TDS.Proctor.Sql.Data.*;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Proctor.performance.dao.TestSessionDao;
import org.apache.commons.lang3.StringUtils;
import org.opentestsystem.delivery.logging.ProctorEventLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import AIR.Common.Helpers.Constants;
import AIR.Common.Helpers._Ref;
import AIR.Common.Utilities.UrlEncoderDecoderUtils;
import TDS.Proctor.Services.ProctorAppTasks;
import TDS.Proctor.Services.ProctorUserService;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigService;
import TDS.Proctor.Sql.Data.Accommodations.AccsDTO;
import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserInfo;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.FailedReturnStatusException;
import TDS.Shared.Exceptions.NoDataException;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;
import tds.dll.api.ICommonDLL;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.caching.CachingService;
import tds.dll.common.performance.utils.LegacySqlConnection;

import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.EXAM;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.EXAMS;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.REQUEST_COUNT;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.SEGMENT;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.STATUS;

@Scope ("prototype")
@Controller
public class ActiveSessionXHR extends HttpHandlerBase
{
private static final Logger _logger = LoggerFactory.getLogger(ActiveSessionXHR.class);

  private ProctorAppTasks _proctorAppTasks = null;

  @Autowired
  private ProctorUserDao proctorUserDao;

  @Autowired
  private TestSessionDao testSessionDao;

  @Autowired
  private LegacySqlConnection legacySqlConnection;

  @Autowired
  private ICommonDLL _commonDll;

  @Autowired
  private ProctorEventLogger _eventLogger;

  @Autowired
  private CachingService _cachingService;

  @RequestMapping (value = "XHR.axd/TestController2", method = RequestMethod.GET)
  public @ResponseBody
  String home () {
    return "XHR.axd works.";
  }

  /*
   * /// <summary> /// get messages for selected language /// </summary>
   * 
   * @RequestMapping (value = "XHR.axd/GetMessagesLanguage")
   * 
   * @ResponseBody private void GetMessagesLanguage(@RequestParam (value =
   * "selectedLanguage", required = false) String selectedLanguage,
   * 
   * @RequestParam (value = "contexts", required = false) String contexts) { try
   * { if (!StringUtils.isEmpty(selectedLanguage))
   * getVariablesCookie().setCurrentLanguage(selectedLanguage);
   * 
   * List<String> contextsList = Arrays.<String>asList (StringUtils.split
   * (contexts, ','));
   * 
   * IMessageService messageService = getBean("iMessageService",
   * IMessageService.class); MessageSystem messageSystem =
   * messageService.Load(selectedLanguage, contextsList); MessageJson
   * messageJson = new MessageJson(messageSystem);
   * SendJsonString(messageJson.Create()); } catch (Exception re) {
   * OnDBFailed(re.ReturnStatus); } catch (Exception ex) {
   * OnFailed("UnableToProcessRequest"); TDSLogger.Application.getFatal(ex);
   * HttpContext.clearError(); } }
   */

  // <summary>
  // set current default language for this proctor
  // </summary>
  @RequestMapping (value = "XHR.axd/SetCurrentLanguage")
  @ResponseBody
  public ReturnStatus setCurrentLanguage (@RequestParam (value = "selectedLanguage", required = false) String selectedLanguage) {
    if (!StringUtils.isEmpty (selectedLanguage))
      getVariablesCookie ().setCurrentLanguage (selectedLanguage);
    return new ReturnStatus ("True", "");
  }

  // / <summary>
  // / hand off session to another browser key
  // / [P_HandoffSession]
  // / </summary>
  @RequestMapping (value = "XHR.axd/HandoffSession")
  @ResponseBody
  public ReturnStatus handoffSession (@RequestParam (value = "sessionID", required = false) String strSessionID) throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    try {
      ProctorUser thisUser = getUser ();
      if (StringUtils.isEmpty (strSessionID)) {
        // TODO Shiva
        // OnFailed("InvalidInputSessionKey");
        _logger.error ("InvalidInputSessionKey");
        return new ReturnStatus ("False", "InvalidInputSessionKey");
      }
      _Ref<UUID> sessionKey = new _Ref<UUID> ();

      if (_proctorAppTasks.getTestSessionTasks ().handoffSession (thisUser.getKey (), thisUser.getBrowserKey (), strSessionID, sessionKey)) {
        // success
        // save the new sessionKey to the cookie
        thisUser.setSessionKey (sessionKey.get ());
        ProctorUserService.save (thisUser, getUserInfo ());
        return new ReturnStatus ("True", "");
      }
    } catch (Exception re) {
      _logger.error (re.toString (),re);
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
    return null;
  }

  // / <summary>
  // / check to see if the current session has active students.
  // / P_GetActiveOpportunities_2010
  // / </summary>
  @RequestMapping (value = "XHR.axd/ServerActivity")
  @ResponseBody
  public ReturnStatus serverActivity () throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    try {
      ProctorUser thisUser = getUser ();
      if (thisUser.getSessionKey () == null || Constants.UUIDEmpty.equals (thisUser.getSessionKey ()))
        return new ReturnStatus ("False", "");

      boolean hasActivity = _proctorAppTasks.getTestSessionTasks ().hasActiveOpps (thisUser.getSessionKey (), thisUser.getKey (), thisUser.getBrowserKey ());

      if (hasActivity)
    	  return new ReturnStatus ("True", "");
      return new ReturnStatus ("False", "");
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.);
      _logger.error (re.toString (),re);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }

  }

  // / <summary>
  // / Set the current session date visited for the database to know that the
  // current proctor check-in
  // / P_SetSessionDateVisited_2010
  // / </summary>
  @RequestMapping (value = "XHR.axd/ProctorPing")
  @ResponseBody
  private ReturnStatus proctorPing (@RequestParam (value = "sessionKey", required = false) String strSessionKey) throws TDSSecurityException, ReturnStatusException, SQLException {
    try {
      UUID sessionKey = UUID.fromString (strSessionKey);
      ProctorUser thisUser = checkAuthenticatedAndValidate(sessionKey, "ProctorPing");

      _proctorAppTasks.getTestSessionTasks ().setSessionDateVisited (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ());

      return new ReturnStatus ("True", "");
    } catch (Exception re) {
      _logger.error (re.toString (),re);
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / Auto-refresh by the active session page every x-configurable mins.
  // / //1. Get Current Testee belong to this session
  // / //2. Get a list of students waiting for approval
  // / //3. Get a list of unacknowledged alert messages
  // / </summary>
  @RequestMapping (value = "XHR.axd/AutoRefreshData")
  @ResponseBody
  public SessionDTO autoRefreshData (@RequestParam (value = "sessionKey", required = false) String strSessionKey, @RequestParam (value = "bGetCurTestees", required = false) String strBGetCurTestees)
          throws TDSSecurityException, ReturnStatusException, SQLException {

    try {
      if (StringUtils.isEmpty (strSessionKey)) {
        _logger.error ("InvalidInputSessionKey");
        return null;
      }

      SessionDTO sessionDTO = new SessionDTO ();

      UUID sessionKey = UUID.fromString(strSessionKey);
      ProctorUser thisUser = checkAuthenticatedAndValidate(sessionKey, "AutoRefreshData");

      boolean bGetCurTestees = true; // always get current testees if parameter
                                     // not exists
      if (!StringUtils.isEmpty (strBGetCurTestees)) {
        bGetCurTestees = Boolean.parseBoolean (strBGetCurTestees);
      }

      // 1. Get a list of students waiting for approval
      sessionDTO.setbReplaceApprovalOpps (true);
      sessionDTO.setApprovalOpps (_proctorAppTasks.getTestOppTasks ().getTestsForApproval (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));

      // 1. Get Current Testee belong to this session
      // 2. Get a list of unacknowledged alert messages
      if (bGetCurTestees) // only get this data on demand
      {
        // get list of test opps
        sessionDTO.setTestOpps (new TestOpps ());
        sessionDTO.setbReplaceTestOpps (true);
        sessionDTO.setTestOpps (_proctorAppTasks.getTestOppTasks ().getCurrentSessionTestees (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));

        // get unacknowledged alert messages
        sessionDTO.setbReplaceAlertMsgs (true);
        sessionDTO.setAlertMessages (getUnAcknowledgedMessages ());

        final List<Map<String, String>> examEventInfoList = new ArrayList<>();
        for(TestOpportunity testOpportunity: sessionDTO.getApprovalOpps()) {
          final Map<String, String> examFields = new HashMap<>(4);
          examFields.put(EXAM.name(), testOpportunity.getOppKey().toString());
          examFields.put(STATUS.name(), testOpportunity.getStatus().toString());
          examFields.put(SEGMENT.name(), String.valueOf(testOpportunity.getWaitSegment()));
          examFields.put(REQUEST_COUNT.name(), String.valueOf(testOpportunity.getRequestCount()));
          examEventInfoList.add(examFields);
        }
        _eventLogger.putField(EXAMS.name(), examEventInfoList);
      }

      return sessionDTO;
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      _logger.error (re.toString (),re);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / Get Initialization data:
  // / 1. When just login
  // / 2. When do a browser refresh
  // / Get the following data:
  // / 1. Get current session data if any
  // / 2. Get a list of selectable tests
  // / 3. Get a list of unacknowledged alert messages if any
  // / //1. Get Current Testee belong to this session
  // / //2. Get a list of students waiting for approval
  // / //3. Get a list of unacknowledged alert messages
  // / </summary>
  @RequestMapping (value = "XHR.axd/GetInitData")
  @ResponseBody
  public SessionDTO getInitData () throws TDSSecurityException, ReturnStatusException, RuntimeReturnStatusException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      SessionDTO sessionDTO = new SessionDTO ();
      sessionDTO.replaceAll (true);
      // get session data
      sessionDTO.setSession (_proctorAppTasks.getTestSessionTasks ().getCurrentSession (thisUser.getKey (), thisUser.getBrowserKey ()));

      // get list of tests

      sessionDTO.setTests (_proctorAppTasks.getTestTasks ().getSelectableTests (thisUser.getKey ()));

      // get a list of segments
      sessionDTO.setSegments (_proctorAppTasks.getTestTasks ().getSegments ());

      // get unacknowledged alert messages
      sessionDTO.setbReplaceAlertMsgs (true);
      sessionDTO.setAlertMessages (getUnAcknowledgedMessages ());

      if (sessionDTO.getSession () != null && sessionDTO.getSession ().getKey () != null && !Constants.UUIDEmpty.equals (sessionDTO.getSession ().getKey ())) {
        // get other data as well
        UUID sessionKey = sessionDTO.getSession ().getKey ();
        // this could be diff from the session key in the cookie data

        // get list of session tests
        sessionDTO.setSessionTests (_proctorAppTasks.getTestSessionTasks ().getSessionTests (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));

        // get list of test opps that are waiting for approval only if the
        // session has some ...
        if (sessionDTO.getSession ().getNeedapproval () > 0) {
          sessionDTO.setApprovalOpps (_proctorAppTasks.getTestOppTasks ().getTestsForApproval (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));
        }

        // get list of test opps
        sessionDTO.setTestOpps (_proctorAppTasks.getTestOppTasks ().getCurrentSessionTestees (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));
        sessionDTO.setbReplaceTestOpps (true);
      }

      return sessionDTO;
    } catch (Exception re) {
      // OnDBFailed (re.ReturnStatus);
      _logger.error (re.toString (),re);
      throw re;

    }/*
      * catch (Exception ex) { _logger.error (ex.getMessage ()); //OnFailed
      * ("UnableToProcessRequest"); //TDSLogger.Application.Error (ex);
      * //HttpContext.clearError (); }
      */
  }

  @RequestMapping (value = "XHR.axd/GetSessionTests")
  @ResponseBody
  public List<String> getSessionTests (@RequestParam (value = "sessionKey") String strSessionKey) throws NoDataException, TDSSecurityException, ReturnStatusException {
    try {
      ProctorUser thisUser = checkAuthenticated();
      UUID sessionKey = UUID.fromString (strSessionKey);

      List<String> sessionTests = _proctorAppTasks.getTestSessionTasks ().getSessionTests (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ());

      if (sessionTests.size () > 0)
        return sessionTests;

    } catch (Exception re) {
      _logger.error (re.toString (),re);
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // SetStatus(HttpStatusCode.InternalServerError);
    // Write(ex.Message);
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
    throw new NoDataException ();
  }

  // / <summary>
  // / Create new session or add more tests to the current session
  // / </summary>
  @RequestMapping (value = "XHR.axd/InsertSessionTests")
  @ResponseBody
  public SessionDTO insertSessionTests (@RequestParam (value = "sessionKey", required = false) String strSessionKey, @RequestParam (value = "testKeys", required = false) String testKeys,
      @RequestParam (value = "testIDs", required = false) String testIDs) throws TDSSecurityException, ReturnStatusException {
    try {
      ProctorUser thisUser = checkAuthenticated();
      SessionDTO sessionDTO = new SessionDTO ();
      TestSession testSession;
      if (StringUtils.isEmpty (strSessionKey)) {
        Date begin = null;
        Date end   = null;
//        Date now = new Date ();
//        begin = Dates.getStartOfDayDate (now);
//        // time zone conversion
//        begin = Dates.convertXST_EST (begin, getTimezoneOffset ());
//        end = Dates.getEndOfDayDate (now);
//        // time zone conversion
//        end = Dates.convertXST_EST (end, getTimezoneOffset ());
        
        //TODO Elena: per 11/18/2014 conversation with Hoai-Anh Ngo 
        // we will let ProctorDLL.P_CreateSession method assign begin and end dates
        testSession = _proctorAppTasks.getTestSessionTasks ().createSession (thisUser.getKey (), thisUser.getBrowserKey (), "", thisUser.getId (), thisUser.getFullname (), null, null);

        thisUser.setSessionKey (testSession.getKey ());
        ProctorUserService.save (thisUser, getUserInfo ()); // save new session
                                                            // key to the cookie
        sessionDTO.setSession (testSession);
        sessionDTO.setbReplaceSession (true);
      } else {
        testSession = new TestSession (thisUser.getKey (), thisUser.getBrowserKey ());
        testSession.setKey (UUID.fromString (strSessionKey));
      }
      String[] aryTestKeys = StringUtils.split (testKeys, '|');
      String[] aryTestIDs = StringUtils.split (testIDs, '|');
      UUID sessionKey = testSession.getKey ();



      int len = aryTestKeys.length;
      for (int i = 0; i < len; i++) {
        _proctorAppTasks.getTestSessionTasks ().insertSessionTest (testSession.getKey (), thisUser.getKey (), thisUser.getBrowserKey (), aryTestKeys[i], aryTestIDs[i]);
      }
      sessionDTO.setSessionTests (_proctorAppTasks.getTestSessionTasks ().getSessionTests (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ()));
      sessionDTO.setbReplaceSessionTests (true);
      return sessionDTO;
    } catch (Exception re) {
      _logger.error (re.toString (),re);
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // catch (Exception ex)
    // {
    // SetStatus(HttpStatusCode.InternalServerError);
    // Write(ex.Message);
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/PauseSession")
  @ResponseBody
  public SessionDTO pauseSession (@RequestParam (value = "sessionKey", required = false) String strSessionKey) throws ReturnStatusException, TDSSecurityException {
    ProctorUser thisUser = checkAuthenticated();
    try {
      UUID sessionKey = UUID.fromString (strSessionKey);

      SessionDTO sessionDTO = new SessionDTO ();
      sessionDTO.setbReplaceApprovalOpps (true);
      sessionDTO.setbReplaceTestOpps (true);
      sessionDTO.setbReplaceSession (true);
      sessionDTO.setbReplaceSessionTests (true);

      if (_proctorAppTasks.getTestSessionTasks ().pauseSession (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ())) {
        thisUser.setSessionKey (Constants.UUIDEmpty);
        ProctorUserService.save (thisUser, getUserInfo ());
        return sessionDTO;
      }
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // SetStatus(HttpStatusCode.InternalServerError);
    // Write(ex.Message);
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
    return null;
  }

  @RequestMapping (value = "XHR.axd/GetCurrentRequests")
  @ResponseBody
  public TesteeRequestDTO getCurrentRequests (@RequestParam (value = "oppKey", required = false) String strOppKey,
		  @RequestParam (value = "environment", required = false) String environment,
		  @RequestParam (value = "context", required = false) String context) 
				  throws TDSSecurityException, ReturnStatusException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      UUID oppKey = UUID.fromString (strOppKey);
      TesteeRequests testeeRequests = _proctorAppTasks.getRequestTasks ().getCurrentTesteeRequests (oppKey, thisUser.getSessionKey (), thisUser.getKey (), thisUser.getBrowserKey ());
      
      IAppConfigService appConfigService = _proctorAppTasks.getAppConfigTasks();
      // retrieve BrowserValidation Rule
      BrowserValidation browserValidation = appConfigService.getBrowserValidation(environment, context);
      // validate browser and keep browser action to pass along with request info
      BrowserAction browserAction = browserValidation.Check(BrowserInfo.GetHttpCurrent());
      
      // convert dates to client's time zone
      _proctorAppTasks.getRequestTasks ().convertDates (testeeRequests, getTimezoneOffset ());
      TesteeRequestDTO testeeRequestsDTO = new TesteeRequestDTO ();
      testeeRequestsDTO.setRequests (testeeRequests);
      testeeRequestsDTO.setBrowserAction (browserAction);

      return testeeRequestsDTO;
    } catch (Exception re) {
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed(new ReturnStatus("failed", "UnableToProcessRequest"));
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/DenyTesteeRequest")
  @ResponseBody
  public ReturnStatus denyTesteeRequest (@RequestParam (value = "requestKey", required = false) String strRequestKey, @RequestParam (value = "reason", required = false) String reason)
      throws ReturnStatusException, TDSSecurityException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      UUID requestKey = UUID.fromString (strRequestKey);

      _proctorAppTasks.getRequestTasks ().denyTesteeRequest (thisUser.getSessionKey (), thisUser.getKey (), thisUser.getBrowserKey (), requestKey, reason);

      return new ReturnStatus ("True", "");
    } catch (Exception re) {
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/PauseOpportunity")
  @ResponseBody
  public ReturnStatus pauseOpportunity (@RequestParam (value = "sessionKey", required = false) String strSessionKey, @RequestParam (value = "oppKey", required = false) String strOppKey)
      throws ReturnStatusException, FailedReturnStatusException, TDSSecurityException {

    try {
      ProctorUser thisUser = checkAuthenticated();

      if (StringUtils.isEmpty (strOppKey) || StringUtils.isEmpty (strSessionKey)) {
        throw new FailedReturnStatusException ("InvalidInputTestOpp");
      }
      UUID sessionKey = UUID.fromString (strSessionKey);
      UUID oppKey = UUID.fromString (strOppKey);
      _proctorAppTasks.getTestOppTasks ().pauseOpportunity (oppKey, sessionKey, thisUser.getKey (), thisUser.getBrowserKey ());

      return new ReturnStatus ("SUCCESS", "SUCCESS");
    } catch (Exception re) {
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / inputs: testee, test, opp, sessionkey, proctorkey, accs ("|" delimiter of
  // acc codes)
  // / </summary>
  @RequestMapping (value = "XHR.axd/ApproveOpportunity")
  @ResponseBody
  public ReturnStatus approveOpportunity (@RequestParam (value = "sessionKey", required = false) String strSessionKey, @RequestParam (value = "oppKey", required = false) String strOppKey,
      @RequestParam (value = "accs", required = false) String strAccs) throws TDSSecurityException, ReturnStatusException, FailedReturnStatusException {

    try {
      if (StringUtils.isEmpty (strOppKey) || StringUtils.isEmpty (strSessionKey))
        throw new FailedReturnStatusException ("InvalidInputTestOpp");

      UUID sessionKey = UUID.fromString (strSessionKey);
      ProctorUser thisUser = checkAuthenticatedAndValidate(sessionKey, "ApproveOpportunity");

      String[] accsList = null;
      if (!StringUtils.isEmpty (strAccs))
        accsList = StringUtils.split (strAccs.substring (0, strAccs.length () - 1), ';'); // remove
                                                                                          // the
                                                                                          // last
                                                                                          // ';'
                                                                                          // char


      UUID oppKey = UUID.fromString (strOppKey);

      if (accsList != null) {
        // step 1-legacy: approve all accs first for legacy application if enabled
        int segment = 0;
        for (String accs : accsList) {
          _proctorAppTasks.getTestOppTasks ().approveAccommodations (oppKey, sessionKey, thisUser.getKey (), thisUser.getBrowserKey (), segment, accs);
          segment++;
        }

        // step 1-rest: approve all accommodations for rest enabled application
        _proctorAppTasks.getTestOppTasks().approveAccommodations(oppKey, sessionKey, thisUser.getBrowserKey (), strAccs);
      }
      // step 2: approve opp
      _proctorAppTasks.getTestOppTasks ().approveOpportunity (oppKey, sessionKey, thisUser.getKey (), thisUser.getBrowserKey ());

      return new ReturnStatus ("", "Success");
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / inputs: testee, test, opp, sessionKey, proctorKey, reason for denied
  // / </summary>
  @RequestMapping (value = "XHR.axd/DenyOpportunity")
  @ResponseBody
  public ReturnStatus denyOpportunity (@RequestParam (value = "sessionKey", required = false) String strSessionKey, @RequestParam (value = "oppKey", required = false) String strOppKey,
      @RequestParam (value = "reason", required = false) String strReason) throws ReturnStatusException, FailedReturnStatusException, TDSSecurityException {

    try {
      if (StringUtils.isEmpty (strOppKey))
        throw new FailedReturnStatusException ("InvalidInputTestOpp");

      ProctorUser thisUser = checkAuthenticated();

      UUID sessionKey = UUID.fromString (strSessionKey);
      UUID oppKey = UUID.fromString (strOppKey);

      _proctorAppTasks.getTestOppTasks ().denyOpportunity (oppKey, sessionKey, thisUser.getKey (), thisUser.getBrowserKey (), strReason);

      return new ReturnStatus ("SUCCESS", "Success");
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed (re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex) {
    // OnFailed ("UnableToProcessRequest");
    // TDSLogger.Application.getFatal (ex);
    // HttpContext.clearError ();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetApprovalOpps")
  @ResponseBody
  public SessionDTO getApprovalOpps (@RequestParam (value = "sessionKey", required = false) String strSessionKey) throws ReturnStatusException, TDSSecurityException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      UUID sessionKey = UUID.fromString (strSessionKey);
      TestOpps testOpps = _proctorAppTasks.getTestOppTasks ().getTestsForApproval (sessionKey, thisUser.getKey (), thisUser.getBrowserKey ());
      SessionDTO sessionDTO = new SessionDTO ();
      sessionDTO.setbReplaceApprovalOpps (true);
      sessionDTO.setApprovalOpps (testOpps);
      return sessionDTO;
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetCurrentAlertMessages")
  @ResponseBody
  public AlertMessages getCurrentAlertMessages () throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    try {

      return _proctorAppTasks.getAlertTasks ().getCurrentMessages (getTimezoneOffset ());

    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetUnacknowledgedAlertMessages")
  @ResponseBody
  public AlertMessages getUnacknowledgedAlertMessages () throws ReturnStatusException, TDSSecurityException {
    checkAuthenticated ();
    try {
      return _proctorAppTasks.getAlertTasks ().getUnAcknowledgedMessages (getUser ().getKey (), getTimezoneOffset ());
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetTestee")
  @ResponseBody
  public Testee getTestee (@RequestParam (value = "testeeID", required = false) String testeeID) throws ReturnStatusException, TDSSecurityException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      Testee testee = _proctorAppTasks.getTesteeTasks ().getTestee (testeeID, thisUser.getKey ());
      return testee;
    } catch (Exception re) {
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetSchoolTestees")
  @ResponseBody
  public Testees getSchoolTestees (@RequestParam (value = "districkKey", required = false) String districkKey, @RequestParam (value = "schoolKey", required = false) String schoolKey,
      @RequestParam (value = "grade", required = false) String grade, @RequestParam (value = "firstName", required = false) String paramFirstName,
      @RequestParam (value = "lastName", required = false) String paramLastName) throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    try {

      String firstName, lastName;

      if (grade == "all")
        grade = null;
      firstName = UrlEncoderDecoderUtils.decode (paramFirstName);
      lastName = UrlEncoderDecoderUtils.decode (paramLastName);

      return _proctorAppTasks.getTesteeTasks ().getSchoolTestees (schoolKey, grade, firstName, lastName);

    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetInstitutions")
  @ResponseBody
  public InstitutionList getInstitutions () throws TDSSecurityException, ReturnStatusException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      return _proctorAppTasks.getInstitutionTasks ().getUserInstitutions (thisUser.getKey (), thisUser.getRoles ());
    } catch (Exception re) {
      throw re;
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetDistricts")
  @ResponseBody
  public Districts getDistricts () throws TDSSecurityException, ReturnStatusException {
    checkAuthenticated ();
    try {
      return _proctorAppTasks.getInstitutionTasks ().getDistricts ();
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetSchools")
  @ResponseBody
  public Schools getSchools (@RequestParam (value = "districtKey", required = false) String districtKey) throws TDSSecurityException, ReturnStatusException, FailedReturnStatusException {
    checkAuthenticated ();
    try {

      if (StringUtils.isBlank (districtKey))
        throw new FailedReturnStatusException ("InvalidInputDistrictKey");

      return _proctorAppTasks.getInstitutionTasks ().getSchools (districtKey);
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  @RequestMapping (value = "XHR.axd/GetGrades")
  @ResponseBody
  public Grades getGrades (@RequestParam (value = "schoolKey", required = false) String schoolKey) throws TDSSecurityException, ReturnStatusException, FailedReturnStatusException {
    checkAuthenticated ();
    try {

      if (StringUtils.isBlank (schoolKey))
        throw new FailedReturnStatusException ("InvalidInputSchoolKey");

      return _proctorAppTasks.getInstitutionTasks ().getGrades (schoolKey);
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / get test and all associate segments accommodations
  // / also get the acc dependencies if neccessary
  // / </summary>
  @RequestMapping (value = "XHR.axd/GetAccs")
  @ResponseBody
  public AccsDTO getAccs (@RequestParam (value = "testKey", required = false) String testKey) throws ReturnStatusException, TDSSecurityException, NoDataException {
    checkAuthenticated ();
    try {
      AccsDTO accsDTO = _proctorAppTasks.getTestTasks ().getTestAccs (testKey);
      if (accsDTO != null && accsDTO.size () > 0)
        return accsDTO;
      else
        throw new NoDataException ();
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // OnFailed("UnableToProcessRequest");
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / Get a list of selectable tests
  // / </summary>
  @RequestMapping (value = "XHR.axd/GetTests")
  @ResponseBody
  public List<Test> getTests () throws ReturnStatusException, TDSSecurityException, NoDataException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      List<Test> tests = _proctorAppTasks.getTestTasks ().getSelectableTests (thisUser.getKey ());

      if (tests != null && tests.size () > 0)
        return tests;
      else
        throw new NoDataException ();
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // SetStatus(HttpStatusCode.InternalServerError);
    // Write(ex.Message);
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  // / <summary>
  // / Get the current session data
  // / </summary>
  @RequestMapping (value = "XHR.axd/GetCurrentSession")
  @ResponseBody
  public TestSession getCurrentSession () throws TDSSecurityException, NoDataException, ReturnStatusException {

    try {
      ProctorUser thisUser = checkAuthenticated();
      TestSession testSession = _proctorAppTasks.getTestSessionTasks ().getCurrentSession (thisUser.getKey (), thisUser.getBrowserKey ());
      if (testSession.getKey () != null)
        return testSession;
      else
        throw new NoDataException ();
    } catch (Exception re) {
      // TODO Shiva
      // OnDBFailed(re.ReturnStatus);
      throw re;
    }
    // TODO Shiva
    // catch (Exception ex)
    // {
    // SetStatus(HttpStatusCode.InternalServerError);
    // Write(ex.Message);
    // TDSLogger.Application.getFatal(ex);
    // HttpContext.clearError();
    // }
  }

  protected void onBeanFactoryInitialized () {
    _proctorAppTasks = getBean ("proctorAppTasks", ProctorAppTasks.class);
  }

  private AlertMessages getUnAcknowledgedMessages () throws ReturnStatusException {
    return _proctorAppTasks.getAlertTasks ().getUnAcknowledgedMessages (getUser ().getKey (), getTimezoneOffset ());
  }

  private int getTimezoneOffset () {
    return getVariablesCookie ().getTimezoneOffset ();
  }

  private ProctorUser checkAuthenticatedAndValidate(UUID sessionKey, String methodName) throws TDSSecurityException, ReturnStatusException {
    ProctorUser user = checkAuthenticated();
    Long proctorKey = user.getKey();

    // The AutoRefreshData call for getting the test approvals doesn't do the validation if the Proctor key is null
    //  so we are putting this in place so it is handled the same way
    if (proctorKey != null) {
      String accessDenied = proctorUserDao.validateProctorSession(user.getKey(), sessionKey, user.getBrowserKey());

      if (accessDenied != null) {
        try (SQLConnection connection = legacySqlConnection.get()) {
          String client = testSessionDao.getClientName(sessionKey);

          _commonDll._LogDBError_SP(connection, methodName, accessDenied, proctorKey, null, null, sessionKey);

          // throw the error
          ReturnStatusException.getInstanceIfAvailable(
            _commonDll._ReturnError_SP(connection, client, methodName, accessDenied, null, null, methodName)
          );
        } catch (SQLException e) {
          throw new ReturnStatusException (e);
        }
      }
    }

    return user;
  }

}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionDTO
{
  private TestSession   session;
  private List<Test>    tests;
  private Segments      segments;
  private List<String>  sessionTests;
  private TestOpps      approvalOpps;
  private TestOpps      testOpps;
  private boolean       bReplaceSession;
  private boolean       bReplaceTests;
  private boolean       bReplaceSegments;
  private boolean       bReplaceSessionTests;
  private boolean       bReplaceApprovalOpps;
  private boolean       bReplaceTestOpps;
  private boolean       bReplaceAlertMsgs;
  private AlertMessages alertMessages;
  private MsbAlert      msbAlert;


  @JsonProperty("session")
  public TestSession getSession () {
    return session;
  }

  public void setSession (TestSession session) {
    this.session = session;
  }

  @JsonProperty("tests")
  public List<Test> getTests () {
    return tests;
  }

  public void setTests (List<Test> tests) {
    this.tests = tests;
  }
  
  @JsonProperty("segments")
  public Segments getSegments () {
    return segments;
  }

  public void setSegments (Segments segments) {
    this.segments = segments;
  }

  @JsonProperty("sessionTests")
  public List<String> getSessionTests () {
    return sessionTests;
  }

  public void setSessionTests (List<String> sessionTests) {
    this.sessionTests = sessionTests;
  }

  @JsonProperty("approvalOpps")
  public TestOpps getApprovalOpps () {
    return approvalOpps;
  }

  public void setApprovalOpps (TestOpps approvalOpps) {
    this.approvalOpps = approvalOpps;
  }

  @JsonProperty("testOpps")
  public TestOpps getTestOpps () {
    return testOpps;
  }

  public void setTestOpps (TestOpps testOpps) {
    this.testOpps = testOpps;
  }

  @JsonProperty("bReplaceSession")
  public boolean isbReplaceSession () {
    return bReplaceSession;
  }

  public void setbReplaceSession (boolean bReplaceSession) {
    this.bReplaceSession = bReplaceSession;
  }

  @JsonProperty("bReplaceTests")
  public boolean isbReplaceTests () {
    return bReplaceTests;
  }

  public void setbReplaceTests (boolean bReplaceTests) {
    this.bReplaceTests = bReplaceTests;
  }

  @JsonProperty("bReplaceSegments")
  public boolean isbReplaceSegments () {
    return bReplaceSegments;
  }

  public void setbReplaceSegments (boolean bReplaceSegments) {
    this.bReplaceSegments = bReplaceSegments;
  }

  @JsonProperty("bReplaceSessionTests")
  public boolean isbReplaceSessionTests () {
    return bReplaceSessionTests;
  }

  public void setbReplaceSessionTests (boolean bReplaceSessionTests) {
    this.bReplaceSessionTests = bReplaceSessionTests;
  }

  @JsonProperty("bReplaceApprovalOpps")
  public boolean isbReplaceApprovalOpps () {
    return bReplaceApprovalOpps;
  }

  public void setbReplaceApprovalOpps (boolean bReplaceApprovalOpps) {
    this.bReplaceApprovalOpps = bReplaceApprovalOpps;
  }

  @JsonProperty("bReplaceTestOpps")
  public boolean isbReplaceTestOpps () {
    return bReplaceTestOpps;
  }

  public void setbReplaceTestOpps (boolean bReplaceTestOpps) {
    this.bReplaceTestOpps = bReplaceTestOpps;
  }

  @JsonProperty("bReplaceAlertMsgs")
  public boolean isbReplaceAlertMsgs () {
    return bReplaceAlertMsgs;
  }

  public void setbReplaceAlertMsgs (boolean bReplaceAlertMsgs) {
    this.bReplaceAlertMsgs = bReplaceAlertMsgs;
  }

  @JsonProperty("alertMessages")
  public AlertMessages getAlertMessages () {
    return alertMessages;
  }

  public void setAlertMessages (AlertMessages alertMessages) {
    this.alertMessages = alertMessages;
  }

  public void replaceAll (boolean bReplace) {
    this.bReplaceSession = bReplace;
    this.bReplaceTests = bReplace;
    this.bReplaceSegments = bReplace;
    this.bReplaceSessionTests = bReplace;
    this.bReplaceApprovalOpps = bReplace;
    this.bReplaceTestOpps = bReplace;
    this.bReplaceAlertMsgs = bReplace;
  }

  public void setMsbAlert(MsbAlert msbAlert) {
    this.msbAlert = msbAlert;
  }

  @JsonProperty("msbAlert")
  public MsbAlert getMsbAlert() {
    return msbAlert;
  }
}

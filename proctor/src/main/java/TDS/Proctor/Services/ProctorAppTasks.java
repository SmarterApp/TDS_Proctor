/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageService;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigService;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionService;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityService;
import TDS.Proctor.Sql.Data.Abstractions.ITestService;
import TDS.Proctor.Sql.Data.Abstractions.ITestSessionService;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestService;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

public class ProctorAppTasks
{
  @Autowired
  @Qualifier("remoteSessionService")
  private ITestSessionService     _testSessionTasks;
  private ITestOpportunityService _testOppTasks     = null;
  private ITestService            _testTasks        = null;
  private IAlertMessageService    _alertTasks       = null;
  private ITesteeRequestService   _requestTasks     = null;
  private ITesteeService          _testeeTasks      = null;
  private IInstitutionService     _institutionTasks = null;
  private IProctorUserService     _proctorUserTasks = null;
  private IAppConfigService       _appConfigTasks   = null;

  public ProctorAppTasks() {}

  public IAppConfigService getAppConfigTasks () {
    return _appConfigTasks;
  }

  public void setAppConfigTasks (IAppConfigService service) {
    this._appConfigTasks = service;
  }

  public IProctorUserService getProctorUserTasks () {
    return _proctorUserTasks;
  }

  public void setProctorUserTasks (IProctorUserService service) {
    this._proctorUserTasks = service;
  }

  public ITestSessionService getTestSessionTasks () {
    return _testSessionTasks;
  }

  public void setTestSessionTasks (ITestSessionService service) {
    this._testSessionTasks = service;
  }

  public ITestOpportunityService getTestOppTasks () {
    return _testOppTasks;
  }

  public void setTestOppTasks (ITestOpportunityService service) {
    this._testOppTasks = service;
  }

  public ITestService getTestTasks () {
    return _testTasks;
  }

  public void setTestTasks (ITestService service) {
    this._testTasks = service;
  }

  public IAlertMessageService getAlertTasks () {
    return _alertTasks;
  }

  public void setAlertTasks (IAlertMessageService service) {
    this._alertTasks = service;
  }

  public ITesteeRequestService getRequestTasks () {
    return _requestTasks;
  }

  public void setRequestTasks (ITesteeRequestService service) {
    this._requestTasks = service;
  }

  public ITesteeService getTesteeTasks () {
    return _testeeTasks;
  }

  public void setTesteeTasks (ITesteeService service) {
    this._testeeTasks = service;
  }

  public IInstitutionService getInstitutionTasks () {
    return _institutionTasks;
  }

  public void setInstitutionTasks (IInstitutionService service) {
    this._institutionTasks = service;
  }
}

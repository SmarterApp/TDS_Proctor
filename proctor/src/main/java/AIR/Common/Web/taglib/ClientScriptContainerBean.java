/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.Common.Web.taglib;

import TDS.Proctor.Services.ProctorAppTasks;

public class ClientScriptContainerBean
{
  private ClientScript    _cs              = null;
  private ProctorAppTasks _proctorAppTasks = null;

  public ClientScriptContainerBean ()
  {
    _cs = new ClientScript ();
  }
  
  public void setClientScript (ClientScript cs) {
    this._cs = cs;
  }

  public ClientScript getClientScript () {
    return this._cs;
  }

  public ProctorAppTasks getProctorAppTasks () {
    return _proctorAppTasks;
  }

  public void setProctorAppTasks (ProctorAppTasks proctorAppTasks) {
    this._proctorAppTasks = proctorAppTasks;
  }
}

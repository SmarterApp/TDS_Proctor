/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import TDS.Shared.Browser.BrowserAction;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TesteeRequestDTO
{

  private TesteeRequests _requests;
  private boolean        _bReplaceRequests = true;
  private BrowserAction  _browserAction;

  @JsonProperty("bReplaceRequests")
  public boolean isbReplaceRequests () {
    return _bReplaceRequests;
  }

  public void setbReplaceRequests (boolean bReplaceRequests) {
    this._bReplaceRequests = bReplaceRequests;
  }

  @JsonProperty("requests")
  public TesteeRequests getRequests()
  {
    return _requests;
  }
  
  public void setRequests(TesteeRequests requests)
  {
    this._requests = requests;
  }
  
  @JsonProperty("browserAction")
  public BrowserAction getBrowserAction() {
	  return _browserAction;
  }
  
  public void setBrowserAction(BrowserAction browserAction) {
    this._browserAction = browserAction;
  }
 
}

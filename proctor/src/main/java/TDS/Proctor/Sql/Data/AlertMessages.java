/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlertMessages extends ArrayList<AlertMessage>
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private int _timeZoneOffset;

  public AlertMessages () {

  }

  public AlertMessages (int timeZoneOffset) {
    this._timeZoneOffset = timeZoneOffset;
  }

  @JsonProperty ("TimeZoneOffset")
  public int getTimeZoneOffset () {
    return _timeZoneOffset;
  }

  // not required
  public void setTimeZoneOffset (int timeZoneOffset) {
    this._timeZoneOffset = timeZoneOffset;
  }

}

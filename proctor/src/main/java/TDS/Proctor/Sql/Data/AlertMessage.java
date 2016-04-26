/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.text.DateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlertMessage
{

  public String   _key, _title, _message, _dateStarted, _timeStarted;
  private boolean _isBStarted;

  public AlertMessage (String key, String title, String message, Date dateStarted, boolean bStarted) {
    this._key = key;
    this._title = title;
    this._message = message;
    this._isBStarted = bStarted;
    // outputs "5/16/2001"
    this._dateStarted = DateFormat.getDateInstance (DateFormat.SHORT).format (dateStarted);
    // outputs "3:02 AM"
    this._timeStarted = DateFormat.getTimeInstance (DateFormat.SHORT).format (dateStarted);
  }

  @JsonProperty ("Key")
  public String getKey () {
    return _key;
  }

  public void setKey (String key) {
    this._key = key;
  }

  @JsonProperty ("Title")
  public String getTitle () {
    return _title;
  }

  public void setTitle (String title) {
    this._title = title;
  }

  @JsonProperty ("Message")
  public String getMessage () {
    return _message;
  }

  public void setMessage (String message) {
    this._message = message;
  }

  @JsonProperty ("DateStarted")
  public String getDateStarted () {
    return _dateStarted;
  }

  public void setDateStarted (String dateStarted) {
    this._dateStarted = dateStarted;
  }

  @JsonProperty ("TimeStarted")
  public String getTimeStarted () {
    return _timeStarted;
  }

  public void setTimeStarted (String timeStarted) {
    this._timeStarted = timeStarted;
  }

  @JsonProperty ("bStarted")
  public boolean isBStarted () {
    return _isBStarted;
  }

  public void setBStarted (boolean bstarted) {
    this._isBStarted = bstarted;
  }

}

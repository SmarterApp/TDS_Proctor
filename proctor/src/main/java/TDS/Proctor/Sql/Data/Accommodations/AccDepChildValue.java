/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Accommodations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccDepChildValue
{
  private String  _thenValue;
  private boolean _isDefault;

  public AccDepChildValue (String thenValue, boolean isDefault) {
    this._thenValue = thenValue;
    this._isDefault = isDefault;
  }

  @JsonProperty ("thenValue")
  public String getThenValue () {
    return _thenValue;
  }

  public void setThenValue (String thenValue) {
    this._thenValue = thenValue;
  }

  @JsonProperty ("isDefault")
  public boolean isDefault () {
    return _isDefault;
  }

  public void setDefault (boolean isDefault) {
    this._isDefault = isDefault;
  }

}

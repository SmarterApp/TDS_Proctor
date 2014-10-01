/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Accommodations;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccDepChildType
{
  private String                 _thenType;
  private List<AccDepChildValue> _thenValues;

  public AccDepChildType (String thenType) {
    this._thenType = thenType;
    this._thenValues = new ArrayList<AccDepChildValue> ();
  }

  @JsonProperty ("thenType")
  public String getThenType () {
    return _thenType;
  }

  public void setThenType (String thenType) {
    this._thenType = thenType;
  }

  @JsonProperty ("thenValues")
  public List<AccDepChildValue> getThenValues () {
    return _thenValues;
  }

  public void setThenValues (List<AccDepChildValue> thenValues) {
    this._thenValues = thenValues;
  }

}

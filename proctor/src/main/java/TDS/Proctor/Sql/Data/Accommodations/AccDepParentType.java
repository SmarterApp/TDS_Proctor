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


public class AccDepParentType
{

  private String                _ifType;
  private String                _ifValue;
  private List<AccDepChildType> _accDepChildTypes;

  public AccDepParentType (String ifType, String ifValue) {
    this._ifType = ifType;
    this._ifValue = ifValue;
    this._accDepChildTypes = new ArrayList<AccDepChildType> ();
  }

  @JsonProperty ("ifType")
  public String getIfType () {
    return _ifType;
  }

  public void setIfType (String ifType) {
    this._ifType = ifType;
  }

  @JsonProperty ("ifValue")
  public String getIfValue () {
    return _ifValue;
  }

  public void setIfValue (String ifValue) {
    this._ifValue = ifValue;
  }

  @JsonProperty ("accDepChildTypes")
  public List<AccDepChildType> getAccDepChildTypes () {
    return _accDepChildTypes;
  }

  public void setAccDepChildTypes (List<AccDepChildType> accDepChildTypes) {
    this._accDepChildTypes = accDepChildTypes;
  }

  public AccDepChildType getChildType (String thenType) {

    for (AccDepChildType a : _accDepChildTypes) {
      if (a.getThenType ().equalsIgnoreCase (thenType))
        return a;
    }
    return null;
  }
}

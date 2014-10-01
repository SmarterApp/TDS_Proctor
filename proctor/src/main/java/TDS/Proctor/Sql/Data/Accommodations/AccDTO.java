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

public class AccDTO
{

  public AccDTO (String key, AccTypesDTO value) {
    _key = key;
    _value = value;
  }

  private String      _key;

  private AccTypesDTO _value;

  @JsonProperty ("Key")
  public String getKey () {
    return _key;
  }

  public void setKey (String key) {
    _key = key;
  }

  @JsonProperty ("Value")
  public AccTypesDTO getValue () {
    return _value;
  }

  public void setValue (AccTypesDTO value) {
    _value = value;
  }

}

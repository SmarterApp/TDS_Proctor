/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Utilities.TDSStringUtils;

public class Grade
{

  private String _value;
  private String _text;

  @JsonProperty("Value")
  public String getValue()
  {
    return _value;
  }
  
  public void setValue(String value)
  {
    this._value = value;
  }
  
  @JsonProperty ("Text")
  public String getText () {

    if (StringUtils.isEmpty (_text) || StringUtils.isBlank (_text))
      _text = TDSStringUtils.format ("Grade {0}", this._value);
    return _text;
  }

  public void setText (String text) {
    this._text = text;
  }

  public Grade (String grade) {
    this._value = grade;
  }

}

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

public class AccValue implements Comparable<AccValue>
{

  // code/value/Isdefault/ValueDiscription'
  private String  _value;
  private String  _code;
  private boolean _isSelected;  // isDefault
  private boolean _allowCombine;
  private int     _sortOrder;

  public AccValue (String value, String code, boolean isSelected) {
    this._value = value;
    this._code = code;
    this._isSelected = isSelected;
  }

  public AccValue (String value, String code, boolean isSelected, boolean allowCombine, int sortOrder) {
    this._value = value;
    this._code = code;
    this._isSelected = isSelected;
    this._allowCombine = allowCombine;
    this._sortOrder = sortOrder;
  }

  // / <summary>
  // / The acc value is selected or not
  // / </summary>

  // / <summary>
  // / Does this acc value allow to combine with other values?
  // / </summary>

  @JsonProperty ("AllowCombine")
  public boolean isAllowCombine () {
    return _allowCombine;
  }

  public void setAllowCombine (boolean allowCombine) {
    this._allowCombine = allowCombine;
  }

  public int getSortOrder () {
    return this._sortOrder;
  }

  public void setSortOrder (int sortOrder) {
    this._sortOrder = sortOrder;
  }

  @JsonProperty ("IsSelected")
  public boolean isSelected () {
    return _isSelected;
  }

  public void setSelected (boolean isSelected) {
    this._isSelected = isSelected;
  }

  @JsonProperty ("Value")
  public String getValue () {
    return _value;
  }

  public void setValue (String value) {
    this._value = value;
  }

  @JsonProperty ("Code")
  public String getCode () {
    return _code;
  }

  public void setCode (String code) {
    this._code = code;
  }

  @Override
  public String toString () {
    return this._value;
  }

  public int compareTo (AccValue rhs) {
    return (Integer.compare (this._sortOrder, rhs.getSortOrder ()));
  }

}

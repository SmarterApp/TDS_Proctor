/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * 
 */
package TDS.Proctor.Services.UnitTests.student.sql.data;

/**
 * @author temp_rreddy
 * 
 */
public class TesteeAttribute
{
  private String _tdsId;
  private String _value;
  private String _label;
  private String _sortOrder;
  private String _atLogin;

  public String getTdsId () {
    return _tdsId;
  }

  public void setTdsId (String tdsId) {
    this._tdsId = tdsId;
  }

  public String getValue () {
    return _value;
  }

  public void setValue (String value) {
    this._value = value;
  }

  public String getLabel () {
    return _label;
  }

  public void setLabel (String label) {
    this._label = label;
  }

  public String getSortOrder () {
    return _sortOrder;
  }

  public void setSortOrder (String sortOrder) {
    this._sortOrder = sortOrder;
  }

  public String getAtLogin () {
    return _atLogin;
  }

  public void setAtLogin (String atLogin) {
    this._atLogin = atLogin;
  }

}

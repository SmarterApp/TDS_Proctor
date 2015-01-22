/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Utilities.TDSStringUtils;

public class Institution
{
  private String               _key;
  private String             _name, _id;
  private boolean            _selected   = false;

  @JsonProperty ("isSchool")
  public boolean             isSchool;

  @JsonProperty ("isDistrict")
  public boolean             isDistrict;

  @JsonProperty ("isState")
  public boolean             isState;

  @JsonProperty ("isSchoolGroup")
  public boolean             isSchoolGroup;

  @JsonProperty ("isDistrictGroup")
  public boolean             isDistrictGroup;

  @JsonProperty ("isStateGroup")
  public boolean             isStateGroup;
  
  
  @JsonProperty ("isClient")
  public boolean             isClient;
  
  public static final String INSTITUTION = "INSTITUTION";
  public static final String DISTRICT    = "DISTRICT";
  public static final String STATE       = "STATE";
  public static final String INSTITUTION_GROUP = "INSTITUTION_GROUP";
  public static final String DISTRICT_GROUP    = "DISTRICT_GROUP";
  public static final String STATE_GROUP       = "STATE_GROUP";
  public static final String CLIENT      = "CLIENT";
  
  public Institution (String key, String name, String id, String type) {
    this._key = key;
    this._id = id;
    this._name = name;
    isSchool = isSchool (type);
    isDistrict = isDistrict (type);
    isState = isState (type);
  }

  @JsonProperty ("Key")
  public String getKey () {
    return _key;
  }

  public void setKey (String key) {
    this._key = key;
  }

  @JsonProperty ("Name")
  public String getName () {
    return _name;
  }

  public void setName (String name) {
    this._name = name;
  }

  @JsonProperty ("ID")
  public String getId () {
    return _id;
  }

  public void setId (String id) {
    this._id = id;
  }
 
  @JsonProperty ("Selected")
  public boolean isSelected () {
    return _selected;
  }

  public void setSelected (boolean selected) {
    this._selected = selected;
  }

  public static boolean isSchool (String instType) {
    return (INSTITUTION.equals (instType));
  }

  public static boolean isDistrict (String instType) {
    return (DISTRICT.equals (instType));
  }

  public static boolean isState (String instType) {
    return (STATE.equals (instType));
  }

  public static boolean isSchoolGroup (String instType) {
    return (INSTITUTION_GROUP.equals (instType));
  }

  public static boolean isDistrictGroup (String instType) {
    return (DISTRICT_GROUP.equals (instType));
  }

  public static boolean isStateGroup (String instType) {
    return (STATE_GROUP.equals (instType));
  }

  public static boolean isClient (String instType) {
    return (CLIENT.equals (instType));
  }

  @Override
  public String toString () {
    return TDSStringUtils.format ("[Name={0}] [ID={1}] [Key={2}]", _name, _id, _key);
  }
}

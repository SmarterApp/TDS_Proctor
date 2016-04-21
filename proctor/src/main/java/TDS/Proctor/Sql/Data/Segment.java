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

public class Segment
{

  private String  _key;
  private String  _id;
  private String  _testKey;
  private int     _position;
  private String  _label;
  private int     _isPermeable;
  private int     _entryApproval;
  private int     _exitApproval;
  private boolean _itemReview;

  @JsonProperty ("key")
  public String getKey () {
    return _key;
  }

  public void setKey (String key) {
    this._key = key;
  }

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String id) {
    this._id = id;
  }

  @JsonProperty ("testKey")
  public String getTestKey () {
    return _testKey;
  }

  public void setTestKey (String testKey) {
    this._testKey = testKey;
  }

  @JsonProperty ("position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int position) {
    this._position = position;
  }

  @JsonProperty ("label")
  public String getLabel () {
    return _label;
  }

  public void setLabel (String label) {
    this._label = label;
  }

  @JsonProperty ("isPermeable")
  public int getIsPermeable () {
    return _isPermeable;
  }

  public void setIsPermeable (int isPermeable) {
    this._isPermeable = isPermeable;
  }

  @JsonProperty ("entryApproval")
  public int getEntryApproval () {
    return _entryApproval;
  }

  public void setEntryApproval (int entryApproval) {
    this._entryApproval = entryApproval;
  }

  @JsonProperty ("exitApproval")
  public int getExitApproval () {
    return _exitApproval;
  }

  public void setExitApproval (int exitApproval) {
    this._exitApproval = exitApproval;
  }

  @JsonProperty ("itemReview")
  public boolean isItemReview () {
    return _itemReview;
  }

  public void setItemReview (boolean itemReview) {
    this._itemReview = itemReview;
  }

}

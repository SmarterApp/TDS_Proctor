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
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/*import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;*/

public class AccType
{

  private String            _type;
  private String            _label;                              // get from
                                                                  // internationalization
                                                                  // lib
  private boolean           _allowChange;

  private boolean           _isVisible;
  private boolean           _isSelectable;
  private List<AccValue>    _values = new ArrayList<AccValue> ();

  private int               _sortOrder;
  // depend on
  private String            _dependOnType;
  private int               _allowCombineCount;
  // acc dependencies
  private AccDepParentTypes _accDepParentTypes;

  @JsonProperty ("dependOnType")
  public String getDependOnType () {
    return _dependOnType;
  }

  public void setDependOnType (String dependOnType) {
    this._dependOnType = dependOnType;
  }

  public AccType () {

  }

  public AccType (String type) {
    this._type = type;
  }

  public AccType (String type, String label, boolean allowChange, boolean isVisible, boolean isSelectable, int sortOrder) {
    this._type = type;
    this._label = label;
    this._allowChange = allowChange;
    this._isVisible = isVisible;
    this._isSelectable = isSelectable;
    this._sortOrder = sortOrder;
  }

  @JsonProperty ("Values")
  public List<AccValue> getValues () {
    if (_values == null)
      _values = new ArrayList<AccValue> ();
    return _values;
  }

  public void setValues (List<AccValue> values) {
    this._values = values;
  }

  @JsonProperty ("Type")
  public String getType () {
    return this._type;
  }

  public void setType (String type) {
    this._type = type;
  }

  // / <summary>
  // / Does the acc Type allow change after student started the test?
  // / </summary>

  @JsonProperty ("Label")
  public String getLabel () {
    return this._label;
  }

  public void setLabel (String label) {
    this._label = label;
  }

  @JsonProperty ("AllowChange")
  public boolean isAllowChange () {
    return _allowChange;
  }

  public void setAllowChange (boolean allowChange) {
    this._allowChange = allowChange;
  }

  // / <summary>
  // / Does this acc Type visible by the proctor?
  // / </summary>

  @JsonProperty ("sOrder")
  public int getSortOrder () {
    return this._sortOrder;
  }

  public void setSortOrder (int sortOrder) {
    this._sortOrder = sortOrder;
  }

  // / <summary>
  // / Does this acc Type select able by the proctor?
  // / </summary>

  @JsonProperty ("IsVisible")
  public boolean isVisible () {
    return _isVisible;
  }

  public void setVisible (boolean isVisible) {
    this._isVisible = isVisible;
  }

  // TODO
  // public override String ToString()
  // {
  // return this.type;
  // }

  @JsonProperty ("IsSelectable")
  public boolean isSelectable () {
    return _isSelectable;
  }

  public void setSelectable (boolean isSelectable) {
    this._isSelectable = isSelectable;
  }

  @Override
  public String toString () {
    return this._type;
  }

  public void add (AccValue accValue) {
    this._values.add (accValue);
  }

  @JsonIgnore
  public AccType getDefault () {
    AccType accType = new AccType (this._type);

    for (AccValue value : this._values) {
      if (value.isSelected ()) {
        AccValue accValue = new AccValue (value.getValue (), value.getCode (), true);
        accType.add (accValue);
        break;
      }
    }
    return accType;
  }

  @JsonProperty ("allowCombineCount")
  public int getAllowCombineCount () {
    return _allowCombineCount;
  }

  public void setAllowCombineCount (int allowCombineCount) {
    this._allowCombineCount = allowCombineCount;
  }

  @JsonProperty("accDepParentTypes")
  public AccDepParentTypes getAccDepParentTypes () {
    return _accDepParentTypes;
  }

  public void setAccDepParentTypes (AccDepParentTypes accDepParentTypes) {
    this._accDepParentTypes = accDepParentTypes;
  }

  // sort by sort order
  public void sortValuesBySortOrder () {
    List<AccValue> list = this._values;
    Collections.sort (list);
  }

}

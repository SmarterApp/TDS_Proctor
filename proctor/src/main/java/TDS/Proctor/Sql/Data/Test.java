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

public class Test
{

  private String  _key          = "";  // from tblSetofAdminSubjects._key
  private String  _id;                 // from tblSetofAdminSubjects.TestID
  private int     _minitems;           // from tblSetofAdminSubjects.minitems
  private int     _maxitems;           // from tblSetofAdminSubjects.maxitems
  private int     _maxopportunities;   // from
                                        // tblSetofAdminSubjects.maxopportunities
  private String  _subject;            // from tblSubject.Name
  private String  _gradeText;
  private String  _year;               // from tblTestAdmin.SchoolYear
  private String  _season;             // from tblTestAdmin.Season
  private String  _displayName;        // properly formatted grade/subject
  private int     _sortOrder;
  private boolean _isSelectable = true;
  private String  _accFamily;
  private boolean _isSegmented;
  private int 	  _sortGrade;
  private String _category;            //test category

  @JsonProperty ("Key")
  public String get_key () {
    return _key;
  }

  public void set_key (String _key) {
    this._key = _key;
  }

  @JsonProperty ("Id")
  public String getId () {
    return _id;
  }

  public void setId (String id) {
    this._id = id;
  }

  @JsonProperty ("Minitems")
  public int getMinitems () {
    return _minitems;
  }

  public void setMinitems (int minitems) {
    this._minitems = minitems;
  }

  @JsonProperty ("Maxitems")
  public int getMaxitems () {
    return _maxitems;
  }

  public void setMaxitems (int maxitems) {
    this._maxitems = maxitems;
  }

  @JsonProperty ("Maxopportunities")
  public int getMaxopportunities () {
    return _maxopportunities;
  }

  public void setMaxopportunities (int maxopportunities) {
    this._maxopportunities = maxopportunities;
  }

  @JsonProperty ("Subject")
  public String getSubject () {
    return _subject;
  }

  public void setSubject (String subject) {
    this._subject = subject;
  }

  @JsonProperty ("GradeText")
  public String getGradeText () {
    return _gradeText;
  }

  public void setGradeText (String gradeText) {
    this._gradeText = gradeText;
  }

  @JsonProperty ("Year")
  public String getYear () {
    return _year;
  }

  public void setYear (String year) {
    this._year = year;
  }

  @JsonProperty ("Season")
  public String getSeason () {
    return _season;
  }

  public void setSeason (String season) {
    this._season = season;
  }

  @JsonProperty ("DisplayName")
  public String getDisplayName () {
    return _displayName;
  }

  public void setDisplayName (String displayName) {
    this._displayName = displayName;
  }

  @JsonProperty ("SortOrder")
  public int getSortOrder () {
    return _sortOrder;
  }

  public void setSortOrder (int sortOrder) {
    this._sortOrder = sortOrder;
  }

  @JsonProperty ("Isselectable")
  public boolean isIsselectable () {
    return _isSelectable;
  }

  public void setIsselectable (boolean isselectable) {
    this._isSelectable = isselectable;
  }

  @JsonProperty ("AccFamily")
  public String getAccFamily () {
    return _accFamily;
  }

  public void setAccFamily (String accFamily) {
    _accFamily = accFamily;
  }

  @JsonProperty ("IsSegmented")
  public boolean isSegmented () {
    return _isSegmented;
  }

  public void setSegmented (boolean isSegmented) {
    _isSegmented = isSegmented;
  }
  
  @JsonProperty ("Category")
  public String getCategory () {
    return _category;
  }

  public void setCategory (String category) {
    _category = category;
  }
  
  @JsonProperty ("SortGrade")
  public int getSortGrade () {
    return _sortGrade;
  }

  public void setSortGrade (int sortGrade) {
    _sortGrade = sortGrade;
  }
  
  
}

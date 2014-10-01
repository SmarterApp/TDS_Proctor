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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class TestSelection
{
  private String _testKey;
  private String _testID;
  private int    _opportunity;
  private String _mode;
  private String _displayName;
  private int    _maxOpportunities;
  private String _subject;
  private String _grade;
  private int    _sortOrder;
  private String _testStatus;
  private String _reason;

  @JsonProperty ("key")
  public String getTestKey () {
    return _testKey;
  }

  public void setTestKey (String testKey) {
    this._testKey = testKey;
  }

  @JsonProperty ("id")
  public String getTestID () {
    return _testID;
  }

  public void setTestID (String testID) {
    _testID = testID;
  }

  @JsonProperty ("opportunity")
  public int getOpportunity () {
    return _opportunity;
  }

  public void setOpportunity (int opportunity) {
    this._opportunity = opportunity;
  }

  @JsonProperty ("mode")
  public String getMode () {
    return _mode;
  }

  public void setMode (String mode) {
    this._mode = mode;
  }

  @JsonProperty ("displayName")
  public String getDisplayName () {
    return _displayName;
  }

  public void setDisplayName (String displayName) {
    this._displayName = displayName;
  }

  @JsonProperty ("maxOpportunities")
  public int getMaxOpportunities () {
    return _maxOpportunities;
  }

  public void setMaxOpportunities (int maxOpportunities) {
    _maxOpportunities = maxOpportunities;
  }

  public String getSubject () {
    return _subject;
  }

  public void setSubject (String subject) {
    this._subject = subject;
  }

  public String getGrade () {
    return _grade;
  }

  public void setGrade (String grade) {
    this._grade = grade;
  }

  @JsonProperty ("sortOrder")
  public int getSortOrder () {
    return _sortOrder;
  }

  public void setSortOrder (int sortOrder) {
    this._sortOrder = sortOrder;
  }

  @JsonProperty ("status")
  public String getTestStatus () {
    return _testStatus;
  }

  public void setTestStatus (String testStatus) {
    _testStatus = testStatus;
  }

  @JsonProperty ("reason")
  public String getReason () {
    return _reason;
  }

  public void setReason (String reason) {
    this._reason = reason;
  }

  public enum Status {
    Disabled, // show test with reason
    Hidden, // hide test from student
    Start, // test has never been started
    Resume // test was previously started
  }

}

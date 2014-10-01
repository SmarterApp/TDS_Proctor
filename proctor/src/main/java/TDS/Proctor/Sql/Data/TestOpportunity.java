/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.List;
import java.util.UUID;

import TDS.Proctor.Sql.Data.Accommodations.AccTypes;

public class TestOpportunity implements Comparable<TestOpportunity>
{
  private UUID   _oppKey;
  private String _ssid;                // the ID by which the testee is
                                        // known to the client
  private String _testKey;
  // administered (an
  // aggregate
  // from tblTesteeResponse)
  private int    _responseCount = -1;  // how many items testee has
  // answered
  private int    _requestCount  = 0;

  private String _testID        = null;
  private String _testName      = null;
  private int    _opp           = 0;   // these range from 1 to

  /**
   * @return the _opp
   */
  public int getOpp () {
    return _opp;
  }

  /**
   * @param _opp
   *          the _opp to set
   */
  public void setOpp (int _opp) {
    this._opp = _opp;
  }

  // test.maxopportunities, so
  // zero
  // means 'not instantiated'
  private int    _itemcount = -1; // how many test items
  // segment
  private int    _segment;
  private String _segmentAccoms;
  private int    _waitSegment;
  private String _name;

  /**
   * @return the _itemcount
   */
  public int getItemcount () {
    return _itemcount;
  }

  /**
   * @param _itemcount
   *          the _itemcount to set
   */
  public void setItemcount (int _itemcount) {
    this._itemcount = _itemcount;
  }

  /**
   * @return the _responseCount
   */
  public int getResponseCount () {
    return _responseCount;
  }

  /**
   * @param _responseCount
   *          the _responseCount to set
   */
  public void setResponseCount (int _responseCount) {
    this._responseCount = _responseCount;
  }

  /**
   * @return the _requestCount
   */
  public int getRequestCount () {
    return _requestCount;
  }

  /**
   * @param _requestCount
   *          the _requestCount to set
   */
  public void setRequestCount (int _requestCount) {
    this._requestCount = _requestCount;
  }

  private String _status;
  private String _displayStatus;

  /**
   * @return the _displayStatus
   */
  public String getDisplayStatus () {
    return _displayStatus;
  }

  /**
   * @param _displayStatus
   *          the _displayStatus to set
   */
  public void setDisplayStatus (String _displayStatus) {
    this._displayStatus = _displayStatus;
  }

  private String _accs; // accommodations String

  /**
   * @return the _accs
   */
  public String getAccs () {
    return _accs;
  }

  /**
   * @param _accs
   *          the _accs to set
   */
  public void setAccs (String _accs) {
    this._accs = _accs;
  }

  private Integer _score;

  /**
   * @return the _score
   */
  public Integer getScore () {
    return _score;
  }

  /**
   * @param _score
   *          the _score to set
   */
  public void setScore (Integer _score) {
    this._score = _score;
  }

  private String _lep; // lep flag 'Y', N' or empty

  /**
   * @return the _lep
   */
  public String getLep () {
    return _lep;
  }

  /**
   * @param _lep
   *          the _lep to set
   */
  public void setLep (String _lep) {
    this._lep = _lep;
  }

  private boolean _custAccs; // is this student/test has
                             // custom accs setting?

  /**
   * @return the _custAccs
   */
  public boolean isCustAccs () {
    return _custAccs;
  }

  /**
   * @param _custAccs
   *          the _custAccs to set
   */
  public void setCustAccs (boolean _custAccs) {
    this._custAccs = _custAccs;
  }

  /**
   * @return the _waitSegment
   */
  public int getWaitSegment () {
    return _waitSegment;
  }

  /**
   * @param _waitSegment
   *          the _waitSegment to set
   */
  public void setWaitSegment (int _waitSegment) {
    this._waitSegment = _waitSegment;
  }

  /**
   * @return the _status
   */
  public String getStatus () {
    return _status;
  }

  /**
   * @param _status
   *          the _status to set
   */
  public void setStatus (String _status) {
    this._status = _status;
  }

  /**
   * @return the _testKey
   */
  public String getTestKey () {
    return _testKey;
  }

  /**
   * @param _testKey
   *          the _testKey to set
   */
  public void setTestKey (String _testKey) {
    this._testKey = _testKey;
  }

  /**
   * @return the _name
   */
  public String getName () {
    return _name;
  }

  /**
   * @param _name
   *          the _name to set
   */
  public void setName (String _name) {
    this._name = _name;
  }

  private List<AccTypes> _accTypesList; // 0: test; 1 and so on for
                                        // segments

  /**
   * @return the _accTypesList
   */
  public List<AccTypes> getAccTypesList () {
    return _accTypesList;
  }

  /**
   * @param _accTypesList
   *          the _accTypesList to set
   */
  public void setAccTypesList (List<AccTypes> _accTypesList) {
    this._accTypesList = _accTypesList;
  }

  // public ReturnStatus ReturnedStatus { get; set; }
  private String _reason = null;

  /**
   * @return the _testID
   */
  public String getTestID () {
    return _testID;
  }

  /**
   * @param _testID
   *          the _testID to set
   */
  public void setTestID (String _testID) {
    this._testID = _testID;
  }

  public TestOpportunity () {

  }

  public TestOpportunity (UUID oppKey) {
    this._oppKey = oppKey;
  }

  // sort by testKey/Status/waitSegment
  public int compareTo (TestOpportunity testOpp) {
    int result = this._testKey.compareTo (testOpp._testKey);
    if (result != 0)
      return result;

    result = this._status.compareTo (testOpp._status);
    if (result != 0)
      return result;

    return ((this._waitSegment < testOpp._waitSegment) ? -1 : (this._waitSegment > testOpp._waitSegment) ? 1 : 0);

  }

  public UUID getOppKey () {
    return _oppKey;
  }

  public void setOppKey (UUID oppKey) {
    this._oppKey = oppKey;
  }

  public void setSsid (String ssid) {
    this._ssid = ssid;
  }

  public String getSsid () {
    return this._ssid;
  }

  public String getTestName () {
    return _testName;
  }

  public void setTestName (String testName) {
    this._testName = testName;
  }
}

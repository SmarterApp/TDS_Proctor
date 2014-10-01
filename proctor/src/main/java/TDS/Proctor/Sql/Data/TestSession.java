/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestSession
{

  private UUID   _key          = null; // internal db key which is a
                                       // 'uniqueidentifier'
  private UUID   _browserKey;         // internal db key which is a
                                       // 'uniqueidentifier'
  private String _id           = null; // client-centered id, known to proctors
                                       // and
  // testees
  private String _name         = null;
  private String _status       = null;

  private int    _needapproval = 0;
  private int    _testOppCount = 0;
  private long   _proctorKey;

  /**
   * @return the _needapproval
   */
  public int getNeedapproval () {
    return _needapproval;
  }

  /**
   * @param _needapproval
   *          the _needapproval to set
   */
  public void setNeedapproval (int _needapproval) {
    this._needapproval = _needapproval;
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
   * @return the _proctorKey
   */
  public long getProctorKey () {
    return _proctorKey;
  }

  /**
   * @param _proctorKey
   *          the _proctorKey to set
   */
  public void setProctorKey (long _proctorKey) {
    this._proctorKey = _proctorKey;
  }

  /**
   * @return the _key
   */
  @JsonProperty ("_key")
  public UUID getKey () {
    return _key;
  }

  /**
   * @param _key
   *          the _key to set
   */
  public void setKey (UUID _key) {
    this._key = _key;
  }

  /**
   * @return the _id
   */
  public String getId () {
    return _id;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._id = _id;
  }

  /**
   * @return the _browserKey
   */
  public UUID getBrowserKey () {
    return _browserKey;
  }

  /**
   * @param _browserKey
   *          the _browserKey to set
   */
  public void setBrowserKey (UUID _browserKey) {
    this._browserKey = _browserKey;
  }

  public TestSession () {
  }

  public TestSession (long proctorKey) {
    this._proctorKey = proctorKey;
  }

  public TestSession (long proctorKey, UUID browserKey) {
    this._proctorKey = proctorKey;
    this._browserKey = browserKey;
  }

  public TestSession (UUID sessionKey, long proctorKey, UUID browserKey) {
    this._key = sessionKey;
    this._browserKey = browserKey;
    this._proctorKey = proctorKey;
  }

}

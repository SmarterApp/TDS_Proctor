/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Helpers.Constants;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Shared.Security.TDSUser;

public class ProctorUser extends TDSUser
{

  private UUID   _sessionKey = Constants.UUIDEmpty;
  private UUID   _browserKey = Constants.UUIDEmpty;
  private String _satelliteURL;
  private long   _clsSessionKey;

  public ProctorUser () {

  }

  public ProctorUser (String clientName, UUID browserKey, String userID, String userPass) {
    this.setId (userID);
    this.setPassword (userPass);
    this._browserKey = browserKey;
    this.setClientName (clientName);
  }

  /**
   * @return the _clsSessionKey
   */
  public long getClsSessionKey () {
    return _clsSessionKey;
  }

  /**
   * @param _clsSessionKey
   *          the _clsSessionKey to set
   */
  public void setClsSessionKey (long _clsSessionKey) {
    this._clsSessionKey = _clsSessionKey;
  }

  // data coming from the check in site
  @JsonProperty ("SessionKey")
  public UUID getSessionKey () {
    return _sessionKey;
  }

  public void setSessionKey (UUID sessionKey) {
    this._sessionKey = sessionKey;
  }

  @JsonProperty ("BrowserKey")
  public UUID getBrowserKey () {
    return _browserKey;
  }

  public void setBrowserKey (UUID browserKey) {
    this._browserKey = browserKey;
  }

  @JsonProperty ("SatelliteURL")
  public String getSatelliteURL () {
    return _satelliteURL;
  }

  public void setSatelliteURL (String satelliteURL) {
    this._satelliteURL = satelliteURL;
  }

  public void parseData (String userData) {
    String[] ary;
    if (StringUtils.isEmpty (userData))
      throw new NullPointerException ("Unable to parse user data");
    ary = userData.split ("|");

    if (ary.length != 6)
      throw new NullPointerException ("Unable to parse user data");
    long userKey;
    try {
      userKey = Long.parseLong (ary[0]);
    } catch (NumberFormatException exp) {
      // todo: what was the .NET code doing on exception?
      throw exp;
    }
    this.setKey (userKey);

    long entityKey;
    try {
      entityKey = Long.parseLong (ary[1]);
    } catch (NumberFormatException exp) {
      // todo: what was the .NET code doing on exception?
      throw exp;
    }
    this.setEntityKey (entityKey);

    long clsSessionKey = -1;
    try {
      Long.parseLong (ary[2]);
    } catch (NumberFormatException exp) {
      // todo: what was the .NET code doing on exception?
      throw exp;
    }
    this._clsSessionKey = clsSessionKey;

    this.setId (ary[3]);
    this.setFullname (ary[4]);

    String[] roles = StringUtils.split (ary[5], ",");
    setRoles (Arrays.asList (roles));

  }

  public String getData () {
    return TDSStringUtils.format ("{0}|{1}|{2}|{3}|{4}|{5}", getKey (), getEntityKey (), _clsSessionKey, getId (), getFullname (), getRoles (","));
  }

}

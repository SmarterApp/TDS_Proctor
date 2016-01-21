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

public class SessionInstance
{
  public UUID Key;
  public long ProctorKey;
  public UUID BrowserKey;

  @JsonProperty ("Key")
  public UUID getKey () {
    return Key;
  }

  public void setKey (UUID key) {
    Key = key;
  }

  @JsonProperty ("ProctorKey")
  public long getProctorKey () {
    return ProctorKey;
  }

  public void setProctorKey (long proctorKey) {
    ProctorKey = proctorKey;
  }

  @JsonProperty ("BrowserKey")
  public UUID getBrowserKey () {
    return BrowserKey;
  }

  public void setBrowserKey (UUID browserKey) {
    BrowserKey = browserKey;
  }

  public SessionInstance (UUID key, long proctorKey, UUID browserKey) {
    this.Key = key;
    this.ProctorKey = proctorKey;
    this.BrowserKey = browserKey;
  }
}

/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import AIR.Common.JsonSerializers.JsonFilePathBackSlashToForwardSlashSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TesteeRequest
{

  public enum RequestType {
    PRINTITEM("Item"), PRINTPASSAGE("Passage");

    private String _description = null;

    RequestType (String type)
    {
      this._description = type;
    }

    // TODO Shiva revisit this JsonProperty
    @JsonProperty ("Description")
    public String getDescription ()
    {
      return _description;
    }

  }

  private UUID    _key;
  private UUID    _oppKey;
  private String  _testeeName;
  private String  _testeeID;
  private long    _testeeKey;
  private String  _testID;
  private int     _opportunity;
  private UUID    _sessionKey;
  private String  _requestType;
  private String  _requestValue;     // full path of Item or stimulus file name
  private String  _requestDesc;
  // public PrintRequestInfo PrintRequest;
  private String  _requestParameters; // just get the String and pass to the
                                      // blackbox
  private static final String dateFormat = "yyyy-MM-dd hh:mm:ss a";
  private String  _strDatePrinted;
  private Date    _datePrinted;
  private String  _strDateSubmitted;
  private Date    _dateSubmitted;

  private Date    _dateFulfilled;
  private String  _deniedReason;
  private int     _itemPage;
  private int     _itemPosition;
  private String  _accCode;
  private String  _language;
  private boolean _markFulfilled;
  private String  _itemResponse;
  private String  _reason;

  /**
   * @return the _key
   */
  @JsonProperty ("Key")
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
   * @return the _deniedReason
   */
  @JsonIgnore
  public String getDeniedReason () {
    return _deniedReason;
  }

  /**
   * @param _deniedReason
   *          the _deniedReason to set
   */
  public void setDeniedReason (String _deniedReason) {
    this._deniedReason = _deniedReason;
  }

  /**
   * @return the _itemPage
   */
  @JsonIgnore
  public int getItemPage () {
    return _itemPage;
  }

  /**
   * @param _itemPage
   *          the _itemPage to set
   */
  public void setItemPage (int _itemPage) {
    this._itemPage = _itemPage;
  }

  /**
   * @return the _requestParameters
   */
  @JsonProperty ("RequestParameters")
  public String getRequestParameters () {
    return _requestParameters;
  }

  /**
   * @param _requestParameters
   *          the _requestParameters to set
   */
  public void setRequestParameters (String _requestParameters) {
    this._requestParameters = _requestParameters;
  }

  /**
   * @return the _language
   */
  @JsonProperty ("Language")
  public String getLanguage () {
    return _language;
  }

  /**
   * @param _language
   *          the _language to set
   */
  public void setLanguage (String _language) {
    this._language = _language;
  }

  /**
   * @return the _accCode
   */
  @JsonProperty ("AccCode")
  public String getAccCode () {
    return _accCode;
  }

  /**
   * @param _accCode
   *          the _accCode to set
   */
  public void setAccCode (String _accCode) {
    this._accCode = _accCode;
  }

  /**
   * @return the _itemResponse
   */
  @JsonProperty ("ItemResponse")
  public String getItemResponse () {
    return _itemResponse;
  }

  /**
   * @param _itemResponse
   *          the _itemResponse to set
   */
  public void setItemResponse (String _itemResponse) {
    this._itemResponse = _itemResponse;
  }

  /**
   * @return the _requestValue
   */
  @JsonProperty ("RequestValue")
  @JsonSerialize(using = JsonFilePathBackSlashToForwardSlashSerializer.class)
  public String getRequestValue () {
    return _requestValue;
  }

  /**
   * @param _requestValue
   *          the _requestValue to set
   */
  public void setRequestValue (String _requestValue) {
    this._requestValue = _requestValue;
  }

  /**
   * @return the _requestType
   */
  @JsonProperty ("RequestType")
  public String getRequestType () {
    return _requestType;
  }

  /**
   * @param _requestType
   *          the _requestType to set
   */
  public void setRequestType (String _requestType) {
    this._requestType = _requestType;
  }

  /**
   * @return the _testeeName
   */
  @JsonProperty ("TesteeName")
  public String getTesteeName () {
    return _testeeName;
  }

  /**
   * @param _testeeName
   *          the _testeeName to set
   */
  public void setTesteeName (String _testeeName) {
    this._testeeName = _testeeName;
  }

  /**
   * @return the _testeeID
   */
  @JsonProperty ("TesteeID")
  public String getTesteeID () {
    return _testeeID;
  }

  /**
   * @param _testeeID
   *          the _testeeID to set
   */
  public void setTesteeID (String _testeeID) {
    this._testeeID = _testeeID;
  }

  /**
   * @return the _opportunity
   */
  @JsonProperty ("Opportunity")
  public int getOpportunity () {
    return _opportunity;
  }

  /**
   * @param _opportunity
   *          the _opportunity to set
   */
  public void setOpportunity (int _opportunity) {
    this._opportunity = _opportunity;
  }

  /**
   * @return the _testID
   */
  @JsonProperty ("TestID")
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

  /**
   * @return the _testeeKey
   */
  @JsonProperty ("TesteeKey")
  public long getTesteeKey () {
    return _testeeKey;
  }

  /**
   * @param _testeeKey
   *          the _testeeKey to set
   */
  public void setTesteeKey (long _testeeKey) {
    this._testeeKey = _testeeKey;
  }

  /**
   * @return the _oppKey
   */
  @JsonProperty ("OppKey")
  public UUID getOppKey () {
    return _oppKey;
  }

  /**
   * @param _oppKey
   *          the _oppKey to set
   */
  public void setOppKey (UUID _oppKey) {
    this._oppKey = _oppKey;
  }

  /**
   * @return the _sessionKey
   */
  @JsonProperty ("SessionKey")
  public UUID getSessionKey () {
    return _sessionKey;
  }

  /**
   * @param _sessionKey
   *          the _sessionKey to set
   */
  public void setSessionKey (UUID _sessionKey) {
    this._sessionKey = _sessionKey;
  }

  /**
   * @return the _dateSubmitted
   */
  @JsonIgnore
  public Date getDateSubmitted () {
    return _dateSubmitted;
  }

  /**
   * @param _dateSubmitted
   *          the _dateSubmitted to set
   */
  public void setDateSubmitted (Date _dateSubmitted) {
    this._dateSubmitted = _dateSubmitted;
  }
  
  @JsonIgnore
  public Date getDatePrinted(){
    return _datePrinted;
  }
 
  public void setDatePrinted (Date value) {
    this._datePrinted = value;
  }

  @JsonProperty ("StrDatePrinted")
  public String getStrDatePrinted () {
    // format date as 3/9/2008 4:05:07 PM
    DateFormat df = new SimpleDateFormat ("M/d/yyyy h:mm:ss aa");
    if(_datePrinted==null)
      return null;
    _strDatePrinted = df.format (this._datePrinted);
    return _strDatePrinted;
  }

  public void setStrDatePrinted (String strDatePrinted)
  {
    this._strDatePrinted = strDatePrinted;
  }

  @JsonProperty ("StrDateSubmitted")
  public String getStrDateSubmitted () {
     if (_dateSubmitted == null) {
      return null;
     }
    // format date as 3/9/2008 4:05:07 PM
    DateFormat df = new SimpleDateFormat ("M/d/yyyy h:mm:ss aa");
    _strDateSubmitted = df.format (this._dateSubmitted);
    return _strDateSubmitted;
  }

  public void setStrDateSubmitted (String strDateSubmitted) {
    this._strDateSubmitted = strDateSubmitted;
  }

  public TesteeRequest (UUID requestKey) {
    this._key = requestKey;
  }

  public TesteeRequest () {

  }

  @JsonProperty ("RequestDesc")
  public String getRequestDesc () {
    return this._requestDesc;
  }

  public void setRequestDesc (String desc) {
    this._requestDesc = desc;
  }

  @JsonProperty ("ItemPosition")
  public int getItemPosition () {
    return this._itemPosition;
  }

  public void setItemPosition (int itemPosition) {
    this._itemPosition = itemPosition;
  }

  @JsonIgnore
  public Date getDateFulfilled () {
    return this._dateFulfilled;
  }

  public void setDateFulfilled (Date date) {
    this._dateFulfilled = date;
  }
  @JsonIgnore
  public String getDateFulfilledFormatted () {
    if(_dateFulfilled!=null) {
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
      return sdf.format (_dateFulfilled);
    } else {
      return "";
    }
    
  }

  
}

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

public class TesteeAttribute {

	private String _TDS_ID;
	private String _type;
	private String _atLogin;
	private String _rtsName;
	private String _label;
	private String _value;
	private int _sortOrder;
	private long _entityKey;
	private String _entityID;
	private boolean _showOnProctor;	

	@JsonProperty ("TDS_ID")
	public String getTdsId() {
		return _TDS_ID;
	}

	public void setTdsId(String TDS_ID) {
		_TDS_ID = TDS_ID;
	}

	@JsonProperty ("type")
	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	@JsonProperty ("atLogin")
	public String getAtLogin() {
		return _atLogin;
	}

	public void setAtLogin(String atLogin) {
		_atLogin = atLogin;
	}

	@JsonProperty ("rtsName")
	public String getRtsName() {
		return _rtsName;
	}

	public void setRtsName(String rtsName) {
		_rtsName = rtsName;
	}

	@JsonProperty ("label")
	public String getLabel() {
		return _label;
	}

	public void setLabel(String label) {
		_label = label;
	}

	@JsonProperty ("value")
	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}

	@JsonProperty ("sortOrder")
	public int getSortOrder() {
		return _sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		_sortOrder = sortOrder;
	}

	@JsonProperty ("entityKey")
	public long getEntityKey() {
		return _entityKey;
	}

	public void setEntityKey(long entityKey) {
		_entityKey = entityKey;
	}

	@JsonProperty ("entityID")
	public String getEntityID() {
		return _entityID;
	}

	public void setEntityID(String entityID) {
		_entityID = entityID;
	}

	@JsonProperty ("showOnProctor")
	public boolean getShowOnProctor() {
		return _showOnProctor;
	}

	public void setShowOnProctor(boolean showOnProctor) {
		_showOnProctor = showOnProctor;
	}

	public TesteeAttribute()
	{
	}
}

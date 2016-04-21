/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Accommodations;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import AIR.Common.collections.HashMapDataSerializer;

public class Accs {

	//@JsonSerialize(using = HashMapDataSerializer.class)
	private final HashMap<String, AccTypes> _accs = new HashMap<String, AccTypes>(); // key=Key
																						// of
																						// (TEST/SEGMENT/FAMILY)

	// public ReturnStatus ReturnedStatus { get; set; }
	
	private boolean _isLoaded = false;

	@JsonProperty("IsLoaded")
	public boolean isLoaded() {
		return _isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this._isLoaded = isLoaded;
	}

	@JsonIgnore
	public AccTypes get(String key) {
		AccTypes accTypes = _accs.get(key);
		return accTypes;
	}

	public void add(String key, AccTypes accTypes) {
		_accs.put(key, accTypes);
	}

	public void clear() {
		_accs.clear();
	}

	@JsonProperty("Data")	
	@JsonSerialize(using = HashMapDataSerializer.class)
	public HashMap<String, AccTypes> getData() {
		return _accs;
	}

  public void sortValuesBySortOrder () {
		Iterator<String> keySetIterator = _accs.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String key = keySetIterator.next();
			_accs.get(key).sortValuesBySortOrder();
		}
	}
}

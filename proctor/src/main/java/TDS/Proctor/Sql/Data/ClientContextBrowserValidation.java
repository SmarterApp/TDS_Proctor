/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.HashMap;
import java.util.Map;

import TDS.Shared.Browser.BrowserValidation;

/**
 * Singleton which holds a dictionary of BrowserValidation objects for each client.
 */
public final class ClientContextBrowserValidation {
	private static final Map<String, BrowserValidation> _browserValidations = new HashMap<String,BrowserValidation>();

    private ClientContextBrowserValidation()
    {
    }

    // retrieve the dictionary key
    private String getKey(String clientName, String context)
    {
        return String.format("%s|%s", clientName, context);
    }

    public BrowserValidation get(String clientName, String context)
    {
        return _browserValidations.get(getKey(clientName, context));
    }

    public synchronized void add(String clientName, String context, BrowserValidation browserValidation)
    {
    	if (_browserValidations.get(getKey(clientName, context)) == null)
    		_browserValidations.put(getKey(clientName, context), browserValidation);
    }
}

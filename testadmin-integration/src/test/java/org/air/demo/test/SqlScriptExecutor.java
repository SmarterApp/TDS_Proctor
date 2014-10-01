/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.demo.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import AIR.Common.DB.SQLConnection;

public class SqlScriptExecutor {

	public static void execScript( String scriptResource, SQLConnection connection ) throws IOException, SQLException {
		ClassLoader cl = SqlScriptExecutor.class.getClassLoader();
		try ( InputStream stream = cl.getResourceAsStream( scriptResource );
			  InputStreamReader reader = new InputStreamReader( stream );
			  BufferedReader lineReader = new BufferedReader( reader ) ) {
			
			String line = lineReader.readLine();
			StringBuilder cmd = new StringBuilder();
			while ( line != null ) {
				line = line.trim();
				cmd.append( line );
				cmd.append( '\n' );
				if ( line.endsWith( ";" ) ) {
					connection.createStatement().execute( cmd.toString() );
					cmd.setLength( 0 );
				}
			}
		}
		
	}
}

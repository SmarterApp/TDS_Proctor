/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.demo.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import AIR.Common.DB.AbstractConnectionManager;

public class DatabaseLifecycleManager {

	private final AbstractConnectionManager mgr;
	private final Map<String,String> dbConfig;
	
	
	public DatabaseLifecycleManager( Map<String,String> db_config, AbstractConnectionManager mgr ) {
		this.dbConfig = db_config;
		this.mgr = mgr;
	}
	
	public void dropConstraints() throws IOException, SQLException {
		runScript( "drop_constraints.sql" );
	}
	
	public void dropTables() throws IOException, SQLException {
		runScript( "drop_tables.sql" );
	}
	
	public void createConstraints() throws IOException, SQLException {
		runScript( "create_constraints.sql" );
	}
	
	public void createTables() throws IOException, SQLException {
		runScript( "create_tables.sql" );
	}
	
	public void createIndices() throws IOException, SQLException {
		runScript( "create_indices.sql" );
	}
	
	private void runScript( String scriptName ) throws IOException, SQLException {
		String resourceName = new StringBuilder( "/sql/" )
			.append( dbConfig.get("dialect") ).append('/')
			.append( dbConfig.get("schemaDefinitionName") ).append('/')
			.append( scriptName )
			.toString();
		
		SqlScriptExecutor.execScript( resourceName, mgr.getConnection() );
	}
}

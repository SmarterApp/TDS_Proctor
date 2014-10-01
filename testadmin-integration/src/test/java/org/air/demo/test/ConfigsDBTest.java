/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.demo.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import AIR.Common.DB.AbstractConnectionManager;
import AIR.test.framework.AbstractTest;


/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations="test-context.xml" )
@TestExecutionListeners({
	    DependencyInjectionTestExecutionListener.class,
        // DbUnitTestExecutionListener.class
    })*/
public class ConfigsDBTest{
	
	@Resource( name = "configs-db-info" )
	private Map<String,String> configsDbInfo;
	
	@Autowired
	private AbstractConnectionManager connectionManager;
	
	private DatabaseLifecycleManager mgr = null;

//	@BeforeClass
//	public static void setUpClass() {
//		mgr = new DatabaseLifecycleManager( configsDbInfo, connectionManager );
//	}
	
	@Test
	public void testConfiguration() {
		assertNotNull( "Autowiring didn't work", configsDbInfo );
		
		for ( String k : new String[] { 
				"jdbc.url",
				"jdbc.userName",
				"jdbc.password",
				"jdbc.driver",
				"dialect",
				"schemaDefinitionName"
			}) {
			
			assertTrue( String.format( "DB config is missing key %s", k ), configsDbInfo.containsKey( k ) );
		}
		
	}
	
}

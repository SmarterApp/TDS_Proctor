/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.initializers;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.springframework.web.WebApplicationInitializer;

import AIR.Common.DB.AbstractConnectionManager;
/*import AIR.Common.DB.Resources.ResourceExistsException;
import AIR.Common.DB.Resources.ResourceInitializationException;
import AIR.Common.DB.Resources.ResourceManager;
import AIR.Common.DB.datasource.DataSourceConnectionManager;*/
import AIR.Common.Utilities.TDSStringUtils;

public class DBInitializer implements WebApplicationInitializer
{

  @Override
  public void onStartup (ServletContext container) throws ServletException {
   /* DataSource ds = null;
    try {
      Context initCtx = new InitialContext ();
      Context envCtx = (Context) initCtx.lookup ("java:comp/env");
      ds = (DataSource) envCtx.lookup ("jdbc/sessiondb");
      
      //TODO Shiva get the driver class name here. The problem I have is that I do not want to create a separate parameter and would rather try to extract 
      //that information from the Resource specification for the DataSource. 
      String jdbcDriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      // once the datasource has been obtained. lets register it.
      DataSourceConnectionManager connectionManager = new DataSourceConnectionManager (ds, jdbcDriverClassName);
      try {
        ResourceManager.getInstance ().addToResources (AbstractConnectionManager.class.getName (), connectionManager, true);
      } catch (ResourceExistsException exp) {
        // its ok if it exists. we will not be doing anything more here.
      } catch (ResourceInitializationException initializationExp) {
        throw new ServletException (TDSStringUtils.format ("Exception while adding resource: {0}", initializationExp.toString ()), initializationExp);
      }
    } catch (NamingException namingExp) {
      throw new ServletException (TDSStringUtils.format ("Exception while initializing datasource : {0}", namingExp.toString ()), namingExp);
    }*/

  }
}

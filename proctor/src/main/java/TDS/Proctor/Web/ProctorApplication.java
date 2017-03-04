/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Web.Session.BaseServletContextListener;

public class ProctorApplication extends BaseServletContextListener
{
	  private static final Logger _logger = LoggerFactory.getLogger(ProctorApplication.class);
  // / <summary>
  // / This is fired when the ASP.NET app starts, this will preload all tests.
  // / </summary>
  public void contextInitialized (ServletContextEvent sce) {
    super.contextInitialized (sce);
    // eventLog app start and list out assemblies
    StringBuilder logBuilder = new StringBuilder ("Proctor Application Started: ");

      // TODO Shiva eventLog jar versions / names as we were doing in .NET code.
     _logger.info (logBuilder.toString ());

  }

  public void contextDestroyed (ServletContextEvent sce) {
    super.contextDestroyed (sce);
    // eventLog app shutdown and the reason why
    	 _logger.info ("Proctor application shutdown");
  }
}

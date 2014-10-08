/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.UnitTests;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.DB.AbstractDAO;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;


public class Program extends AbstractDAO {

	private static ApplicationContext _context = new ClassPathXmlApplicationContext(
			new String[] { "root-context.xml" });
	
	
	public static void main(String[] args) {
		try {
			
			for(String beanName:_context.getBeanDefinitionNames()) {
				System.out.println(beanName);
			}
			// register configuration.
//			registerConfiguration();

			// register logger configuration.
//			registerLoggerConfiguration();

			// register database loaders
//			registerDatabaseResourceManager();

			// load client and validate data
			if (!load()) {
				// client failed to load end
				return;
			}
			System.out.println(_context);
			Utils.writeInfo("Press Enter when ready");

			runProctorSimulator();

			Utils.writeInfo("FINISHED");
		} catch (Throwable e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	private static void runProctorSimulator() throws ReturnStatusException {
		Utils.writeInfo("PROCTOR SIMULATOR");

		String proctorEmail = AppSettingsHelper.get("ProctorEmail");
		String proctorPassword = AppSettingsHelper.get("ProctorPassword");

		// TODO the two lines below are hardcoded right now. also passwords are
		// already encrypted in RTS.
		proctorEmail = "zpatel@air.org";
		proctorPassword = "s3RgsZ9JOOQ4ckY0DrPSd46nDQ2BZTZ12ppfDSc+r4npsDXm";

		ProctorSimulator proctorSimulator = new ProctorSimulator(proctorEmail,
				proctorPassword);
		proctorSimulator.start();
	}

	private static boolean load() throws IOException, ConfigurationException {
		Utils.writeInfo("LOADING CLIENT...");

		/*// run validations
		ContextBeans.set(new ClassPathXmlApplicationContext(
				new String[] { "root-context.xml" }));*/

		TDSSettings settingsBean = _context.getBean("tdsSettings",
				TDSSettings.class);
		String clientName = settingsBean.getClientName();

		if (StringUtils.isEmpty(clientName)) {
			Utils.writeError("Could not load the client \"{0}\"", clientName);
			return false;
		}
		return true;
	}

	/*private static void registerLoggerConfiguration() {
		ConfigurationSection appSettings = configurationManager
				.getAppSettings();

		URL logFileName = (new Program()).getClass().getClassLoader()
				.getResource("log4j.xml");
		String logLevel = appSettings.get("logger.debuglevel");
		String path = appSettings.get("logger.proctorDevLogPath");

		LoggerManager.configureLogger(logFileName, logLevel, path);
	}*/

	/*private static void registerConfiguration() throws ConfigurationException {
		
//		 load up configuration. we expect a settings.xml file on the path.
		 
		ConfigurationManager config = configurationManager;
		ClassLoader thisClassLoader = (new Program()).getClass()
				.getClassLoader();
		config.getAppSettings().setURL(
				thisClassLoader.getResource("settings-unitTests.xml"));
		config.getDatabaseSettings().setURL(
				thisClassLoader.getResource("database.xml"));
	}*/

	// TODO Oksana / Shiva One problem we have here is
	
//	 * one problem we have here is that
	 
	/*private static void registerDatabaseResourceManager() {
		ConfigurationSection dbSettings = configurationManager
				.getDatabaseSettings();
		String jdbcURL = dbSettings.get("jdbc.url");
		String jdbcUser = dbSettings.get("jdbc.userName");
		String jdbcPassword = dbSettings.get("jdbc.password");
		String jdbcDriver = dbSettings.get("jdbc.driver");
		StandaloneConnectionManager connectionManager = new StandaloneConnectionManager(
				jdbcURL, jdbcUser, jdbcPassword, jdbcDriver);
		try {
			ResourceManager.getInstance().addToResources(
					AbstractConnectionManager.class.getName(),
					connectionManager, false);
		} catch (ResourceExistsException exp) {
			// we will never throw the exception here as we set the last
			// parameter to false above.
		}
	}*/
}

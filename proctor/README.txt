1) Sample context.xml file. All settings in settings.xml may be overriden in context.xml


<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <!-- Default set of monitored resources -->
	<WatchedResource>WEB-INF/web.xml</WatchedResource>
	<!-- Uncomment this to disable session persistence across Tomcat restarts -->
	<!-- <Manager pathname="" /> -->
	<!-- Uncomment this to enable Comet connection tacking (provides events 
		on session expiration as well as webapp lifecycle) -->
	<!-- <Valve className="org.apache.catalina.valves.CometConnectionManagerValve" 
		/> -->
	<Resource auth="Container" driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
		logAbandoned="true" maxActive="5" maxIdle="2" name="jdbc/sessiondb"
		password="KOJ89238876234rUHJ" removeAbandoned="true" type="javax.sql.DataSource"
		url="jdbc:sqlserver://38.118.82.146;DatabaseName=<database name>" username="dbtds"
		validationQuery="select 1" />
	<Parameter name="logger.proctorDevLogPath" value="C:/WorkSpace/JavaWorkSpace/logs/" override="false" />
	<Parameter name="logger.debuglevel" value="DEBUG" override="false"/>
</Context>


Replace <database name> above with the database that you have been asked to use.

2) Locate the two jars corresponding to these two dependencies 

		<dependency>
			<groupId>org.glassfish</groupId>
			<artifactId>javax.faces</artifactId>
			<version>2.2.0-m14</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
				<groupId>javax.servlet</groupId>
				<artifactId>jstl</artifactId>
				<version>1.2</version>
		</dependency>

and put them in your Tomcat libs folder.

3) Put either the MySQL or the SQLServer jdbc connector jars in your Tomcat libs folder.

4) JSON diff is used for comparing the two json objects by online.
http://tlrobinson.net/projects/javascript-fun/jsondiff/
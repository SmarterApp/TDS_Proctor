################
# Docker file to build a docker servlet container hosting the Proctor WAR.
################

# tomcat 8 based off of the shared secure tomcat image from SS_SharedMultiJar
FROM smarterbalanced/secure-tomcat-image:4.0.0.RELEASE

#Overwrite server.xml
ADD server.xml /usr/local/tomcat/conf/

#Add the WAR file
ADD testadmin-*.war $CATALINA_HOME/webapps/proctor.war

#Embed SAML security files from build machine file-system
#See comments in TDS_Proctor/proctor/pom.xml
RUN mkdir -p /opt/sbtds/resources
ADD security /opt/sbtds/resources/security
ENV PROCTOR_SECURITY_DIR file:///opt/sbtds/resources/security

EXPOSE 8080
CMD ["catalina.sh", "run"]
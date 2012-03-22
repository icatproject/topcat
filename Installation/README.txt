PREREQUISITES
========================================
1) Glassfish v3 or later
2) Maven 2.0.9 or later
3) Oracle
4) Oracle JDBC Driver ojdbc14.jar @ http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html
5) GLASSFISH_HOME is set to point to <glassfish_home_path>


SETUP THE DATABASE
========================================
From within the "Installation" directory
1) update values in <> in initalise_topcat_db.sql
	a) <facility name>, the name of the facility, e.g. ISIS
	b) <icat url with ?wsdl>, the URL of the wsdl to contact, e.g. https://facilities01.esc.rl.ac.uk/ICATService/ICAT?wsdl
	c) <default username>, the username to use when contacting ICAT
	d) <default password>, the password to use when contacting ICAT
	e) <facility search plugin>, possible values include:
		null
		uk.ac.stfc.topcat.gwt.client.facility.ISISFacilityPlugin
		uk.ac.stfc.topcat.gwt.client.facility.DiamondFacilityPlugin
	f) <download plugin name>, possible values include:
		null
		restfulDownload
	g) <ICAT version number>, possible values include:
		v340
		v341
		v400
2) sqlplus
    Enter user-name: system
   SQL>@createuser_topcat_db
    Enter Database Name     : XE
    Enter SYS password              : 
    Enter topcat password       : 
    Enter External tables location : /tmp/extloc
3) sqlplus
    Enter user-name: topcat
   SQL>@generate_topcat_db
   SQL>@initialise_topcat_db 


INSTALLATION INSTRUCTIONS
========================================
From within the "Installation" directory
1) Modify resources.xml database password and connection string
2) Modify passFile with a password to use for the TOPCAT domain
3) Modify the topcat.properties file to select the caching and LOGO URL location and the values that will be used in the links in the footer
4) mvn install:install-file -Dfile=<oracle_lib>/ojdbc14.jar -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0 -Dpackaging=jar
5) mvn install
6) mvn glassfish:create-domain -Dglassfish.home=$GLASSFISH_HOME --non-recursive
7) openssl s_client -showcerts -connect <server>:<port> </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $GLASSFISH_HOME/domains/TOPCAT/config/facility.cert
8) keytool -import -noprompt -alias <alias> -file $GLASSFISH_HOME/domains/TOPCAT/config/facility.cert -keystore $GLASSFISH_HOME/domains/TOPCAT/config/cacerts.jks --storepass changeit 
9) mvn resources:copy-resources -Dglassfish.home=$GLASSFISH_HOME --non-recursive
10) mvn glassfish:deploy -Dglassfish.home=$GLASSFISH_HOME --non-recursive


UNDEPLOY
=======================================
From within the "Installation" directory
1) mvn glassfish:undeploy -Dglassfish.home=$GLASSFISH_HOME --non-recursive


UNINSTALL
=======================================
From within the "Installation" directory
1) mvn glassfish:delete-domain -Dglassfish.home=$GLASSFISH_HOME --non-recursive

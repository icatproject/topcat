TopCAT a Web Based GUI for ICAT
###############################

A setup script has been provided to help configure TopCAT.


PREREQUISITES
#############

Glassfish installed and domain1 running:
> <GLASSFISH_HOME>/glassfish/bin/asadmin start-domain domain1

If you are not using the Derby database ensure the appropriate driver is in 
<GLASSFISH_HOME>glassfish/domains/domain1/lib/. Glassfish will need to be
restarted after the driver is put there. You will also need write access to a
database/schema.


CONFIGURATION FILES
###################

The configuration files are described in more detail below. Configuration data
is stored in the following files:
	glassfish.props
	icats.d/localhost.icat
	topcat.properties


INSTALLATION
############

1) customise glassfish.props
2) create and customise topcat.properties using topcat.properties.example as a
   template
3) add a .icat file to the icats.d directory for every ICAT you wish to be able
   to access via TopCAT, using my.icat.example as a template. The setup script 
   looks for all .icat files in the icats.d directory.
4) create the connection pool, recourses and copy the topcat.properties file 
   into place (only if it does not already exist) using:
> ./topcat_setup.py --create
5) Deploy TopCAT to Glassfish using:
> ./topcat_setup.py --deploy
6) Initialise the database with data about ICAT using:
> ./topcat_setup.py --addICAT
7) If the ICATs you are connecting to are using non standard certificates you
   will need to add them to the Glassfish trust store


UPGRADING
#########

1) Undeploy TopCAT to Glassfish using:
> ./topcat_setup.py --undeploy
2) Deploy TopCAT to Glassfish using:
> ./topcat_setup.py --deploy
3) Manually update the database table ICAT_AUTHENTICATION with entries for the
   authentication plugin


LOG FILE
########

Messages are logged in <GLASSFISH_HOME>glassfish/domains/domain1/log/server.log


CONFIGURATION FILES IN MORE DETAIL
##################################

glassfish.props
~~~~~~~~~~~~~~~

The keys in this file are:
	dbType (optional)
	driver
	topcatProperties
	glassfish
	port (optional)

A) dbType (optional, default:derby) - The type of database to use, i.e.:
	derby
	mysql
	oracle
	
B) driver - The database driver to be used, e.g.
	for Derby:
		org.apache.derby.jdbc.ClientDataSource
	for Oracle:
		oracle.jdbc.pool.OracleDataSource
	for MySQL:
		com.mysql.jdbc.jdbc2.optional.MysqlDataSource

C) topcatProperties - The TopCAT connection properties, e.g.
	for Derby:
		Password=APP:User=APP:serverName=localhost:DatabaseName=topcat: \
			connectionAttributes=";"create"'"="'"true
	for Oracle:
		url="'"jdbc:oracle:thin:@//localhost:1521/XE"'": \
		ImplicitCachingEnabled=true:MaxStatements=200:user=topcat:password=secret
	for MySQL:
		user=topcat:password=secret:databaseName=topcat

D) glassfish - The Glassfish home directory, must contain "glassfish/domains"

E) port (optional, default:4848) - The port for glassfish admin calls (normally 4848)



icats.d/localhost.icat
~~~~~~~~~~~~~~~~~~~~~~

The keys in this file are:
	facilityName
	wdslUrl
	icatVersion
	authenticationProperties

A) facilityName - The name of the facility to be displayed in TopCAT

B) wsdlUrl - The URL of the WSDL for the ICAT

C) icatVersion - The name of the plugin in TopCAT to be used with this ICAT,
   i.e. v420

D) authenticationProperties - The properties for the authentication plugin
   There can be multiple authenticationProperties entries. The components are:
		url -    The url of the athentication service, NOT currently used
		type -   The ICAT authentication type, i.e. 'db' or 'ldap'
		plugin - The TopCAT plugin name, leave blank to use the default 
				 username/password plugin


topcat.properties
~~~~~~~~~~~~~~~~~

The keys in this file are:
	KEYWORDS_CACHED
	LOGO_URL
	ACCESSIBILITY
	PRIVACY_POLICY
	DATA_POLICY
	TERMS_OF_USE
	COMPLAINTS_PROCEDURE
	FEEDBACK

A) KEYWORDS_CACHED - Boolean flag, 'true' or 'false'

B) LOGO_URL - The location of an image to display in the header of TopCAT. The
   value should be a path/file name relative to the 
   <GLASSFISH_HOME>glassfish/domains/domain1/applications/TopCAT/ directory.

C) ACCESSIBILITY - The URL for an accessibility web page, a link to this is
   included in the TopCAT footer

D) PRIVACY_POLICY - The URL for a privacy policy web page, a link to this is
   included in the TopCAT footer

E) DATA_POLICY - The URL for a data policy web page, a link to this is included
   in the TopCAT footer

F) TERMS_OF_USE - The URL for a terms of use web page, a link to this is
   included in the TopCAT footer

G) COMPLAINTS_PROCEDURE - The URL for a complaints web page, a link to this is
   included in the TopCAT footer

H) FEEDBACK - The URL for a mailto link, a link to this is included in the
   TopCAT footer


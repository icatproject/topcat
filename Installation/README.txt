Topcat a Web Based GUI for Icat
###############################

A setup script has been provided to help configure topcat.


PREREQUISITES
#############

Glassfish installed and domain1 running:
> <GLASSFISH_HOME>/glassfish/bin/asadmin start-domain domain1

If you are not using the Derby database ensure the appropriate driver is in 
<GLASSFISH_HOME>/glassfish/domains/domain1/lib/. Glassfish will need to be
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
   
3) add a .icat file to the icats.d directory for every icat you wish to be able
   to access via topcat, using my.icat.example as a template. The setup script 
   looks for all .icat files in the icats.d directory.
   
4) create the connection pool, recourses and copy the topcat.properties file 
   into place (only if it does not already exist) using:
> ./topcat_setup.py --create

5) Deploy topcat to Glassfish using:
> ./topcat_setup.py --deploy

6) Initialise the database with data about icat using:
> ./topcat_setup.py --addICAT

7) If the icats you are connecting to are using non standard certificates you
   will need to add them to the Glassfish trust store:
> openssl s_client -showcerts -connect <HOST>:<PORT> </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > <GLASSFISH_HOME>/glassfish/domains/domain1/config/facility.cert
> keytool -import -noprompt -alias <ALIAS> -file <GLASSFISH_HOME>/glassfish/domains/domain1/config/facility.cert -keystore <GLASSFISH_HOME>/glassfish/domains/domain1/config/cacerts.jks --storepass changeit 

8) If you added certificates to the trust store you must restart glassfish:
> asadmin restart-domain

UPGRADING to 1.9
################

1) Undeploy topcat from Glassfish using:
> ./topcat_setup.py --undeploy

2) Upgrade the database schema
> ./topcat_setup.py --upgrade

3) Deploy topcat to Glassfish using:
> ./topcat_setup.py --deploy

4) If upgrading from 1.6 use your favourite database GUI or CLI to update your 
   database. You will need to add data to the table ICAT_AUTHENTICATION. First
   find the out the server id:
SELECT ID, NAME FROM TOPCAT_ICAT_SERVER;
   Then add one or more rows e.g.:
INSERT INTO ICAT_AUTHENTICATION (SERVER_ID, AUTHENTICATION_TYPE) VALUES (1, 'ldap');


CONNECTING TO AN ICAT DATA SERVICE
##################################

Currently this has to be carried out using your favourite database GUI or CLI
to update your database. You will need to add data to the table 
TOPCAT_ICAT_SERVER. For example:
UPDATE TOPCAT_ICAT_SERVER SET DOWNLOAD_PLUGIN_NAME='IDS', DOWNLOAD_SERVICE_URL='http://examle.com/DownloadManager/' WHERE NAME = 'myFacility';


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
	domain (optional)

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

C) topcatProperties - The topcat connection properties, e.g.
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

F) domain (optional, default:domain1) - The domain within glassfish to use.
    This domain must already exist.



icats.d/localhost.icat
~~~~~~~~~~~~~~~~~~~~~~

The keys in this file are:
	facilityName
	wdslUrl
	icatVersion
	authenticationProperties

A) facilityName - The name of the facility to be displayed in topcat

B) wsdlUrl - The URL of the WSDL for the icat

C) icatVersion - The name of the plugin in topcat to be used with this icat,
   i.e. v420
   N.B. for any 4.2.n icat please use 'v420'. In a future release the '0' will be
   dropped as now it is guaranteed that the icat api will not change between minor
   versions.
D) authenticationProperties - The properties for the authentication plugin
   There can be multiple authenticationProperties entries. The components are:
		url -    The url of the authentication service, NOT currently used
		type -   The ICAT authentication type, i.e. 'db' or 'ldap'
		plugin - The topcat plugin name, leave blank to use the default 
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

B) LOGO_URL - The location of an image to display in the header of topcat. The
   value should be a path/file name relative to the 
   <GLASSFISH_HOME>glassfish/domains/domain1/applications/TopCAT/ directory.

C) ACCESSIBILITY - The URL for an accessibility web page, a link to this is
   included in the topcat footer

D) PRIVACY_POLICY - The URL for a privacy policy web page, a link to this is
   included in the topcat footer

E) DATA_POLICY - The URL for a data policy web page, a link to this is included
   in the topcat footer

F) TERMS_OF_USE - The URL for a terms of use web page, a link to this is
   included in the topcat footer

G) COMPLAINTS_PROCEDURE - The URL for a complaints web page, a link to this is
   included in the topcat footer

H) FEEDBACK - The URL for a mailto link, a link to this is included in the
   topcat footer


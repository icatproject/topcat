Topcat a Web Based GUI for Icat
###############################

A setup script has been provided to help install topcat.


PREREQUISITES
#############

Glassfish installed and <DOMAIN> running:
> <GLASSFISH_HOME>/glassfish/bin/asadmin start-domain <DOMAIN>

If you are not using the Derby database ensure the appropriate driver is in 
<GLASSFISH_HOME>/glassfish/domains/<DOMAIN>/lib/. Glassfish will need to be
restarted after the driver is put there. You will also need write access to a
database/schema.


CONFIGURATION FILES
###################

The configuration files are described in more detail below. Configuration data
is stored in the following files:
	topcat_glassfish.props
	topcat.properties


INSTALLATION
############

1) customise topcat_glassfish.props

2) create and customise topcat.properties using topcat.properties.example as a
   template
   
3) create the jdbc connection pool and resource, and the topcat admin user and
   enable the principal to role manager and deploy the topcat and topcat admin 
   applications to Glassfish using:
> ./topcat_setup.py --install


ADDING ICATS
############

In order for topcat to be useful it needs to know about one or more icats. To
configure topcat to point to icat use the topcat admin console:
	https://localhost.localdomain:8181/TopCATAdmin/

If the icats you are connecting to are using non standard certificates you
will need to add them to the Glassfish trust store:
> openssl s_client -showcerts -connect <HOST>:<PORT> </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > <GLASSFISH_HOME>/glassfish/domains/<DOMAIN>/config/facility.cert
> keytool -import -noprompt -alias <ALIAS> -file <GLASSFISH_HOME>/glassfish/domains/<DOMAIN>/config/facility.cert -keystore <GLASSFISH_HOME>/glassfish/domains/<DOMAIN>/config/cacerts.jks --storepass changeit 

If you added certificates to the trust store you MUST restart glassfish:
> asadmin restart-domain


UN-INSTALL
##########

To delete the jdbc connection pool and resource, and the topcat admin user and
disable the principal to role manager and undeploy the topcat and topcat admin 
applications from Glassfish use:
> ./topcat_setup.py --uninstall


UPGRADING FROM 1.7 TO 1.9
#########################

1) Uninstall topcat using:
> ./topcat_setup.py --uninstall

2) Upgrade the database schema using:
> ./topcat_upgrade.py --upgrade19

3) Install topcat using:
> ./topcat_setup.py --install


LOG FILES
#########

Messages are logged in <GLASSFISH_HOME>glassfish/domains/<DOMAIN>/log/server.log
and <GLASSFISH_HOME>glassfish/domains/<DOMAIN>/log/topcat*


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
	topcatAdminUser
	topcatAdminWar

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

E) port (optional, default:4848) - The port for glassfish admin calls 
    (normally 4848)

F) domain (optional, default:domain1) - The domain within glassfish to use.
    This domain must already exist.

G) topcatAdminUser - The user name to use when setting up the topcat admin 
    user. This will then be the user name to use to log on to the topcat admin
    console.

H) topcatAdminWar - The name of the topcat admin war file.


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
   <GLASSFISH_HOME>glassfish/domains/<DOMAIN>/applications/TopCAT/ directory.
   
C) MESSAGE - This message will be displayed at the top of the web page. It is
   intended for use by sys admins so that they can inform users of up coming
   down times. If you change it then you will need to reload the application.

D) ACCESSIBILITY - The URL for an accessibility web page, a link to this is
   included in the topcat footer

E) PRIVACY_POLICY - The URL for a privacy policy web page, a link to this is
   included in the topcat footer

F) DATA_POLICY - The URL for a data policy web page, a link to this is included
   in the topcat footer

G) TERMS_OF_USE - The URL for a terms of use web page, a link to this is
   included in the topcat footer

H) COMPLAINTS_PROCEDURE - The URL for a complaints web page, a link to this is
   included in the topcat footer

I) FEEDBACK - The URL for a mailto link, a link to this is included in the
   topcat footer

PREREQUISITES
========================================
1) Glassfish v3 or later
2) Empty schema in database with username topcat
3) Maven 2.0.9 or later
4) Oracle JDBC Driver ojdbc14.jar @ http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html
5) GLASSFISH_HOME is set to point to <glassfish_home_path>

SETUP THE DATABASE
==================
From within the "Installation" directory
1) update values in <> in initalise_topcat_db.sql
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
2) mvn install:install-file -Dfile=<oracle_lib>/ojdbc14.jar -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0 -Dpackaging=jar
3) mvn install
4) mvn glassfish:create-domain -Dglassfish.home=$GLASSFISH_HOME --non-recursive
5) openssl s_client -no_tls1 -showcerts -connect <server>:<port> </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $GLASSFISH_HOME/domains/TOPCAT/config/facility.cert
6) keytool -import -noprompt -alias <alias> -file $GLASSFISH_HOME/domains/TOPCAT/config/facility.cert -keystore $GLASSFISH_HOME/domains/TOPCAT/config/cacerts.jks --storepass changeit 
7) mvn glassfish:deploy -Dglassfish.home=$GLASSFISH_HOME --non-recursive


UNDEPLOY
=======================================
From within the "Installation" directory
1) mvn glassfish:undeploy -Dglassfish.home=$GLASSFISH_HOME --non-recursive


UNINSTALL
=======================================
From within the "Installation" directory
1) mvn glassfish:delete-domain -Dglassfish.home=$GLASSFISH_HOME --non-recursive

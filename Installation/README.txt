PREREQUISITES
========================================
1) Glassfish v3 or later
2) Empty schema in database with username topcat
3) Maven 2.0 or later
4) Oracle JDBC Driver ojdbc14.jar @ http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html

INSTALLATION INSTRUCTIONS
========================================
0) Modify resource.xml database password and connection string
1) mvn install
2) mvn glassfish:start-domain -Dglassfish.home=<glassfish_home_path> -Dglassfish.adminPassword=<Admin_Password> --non-recursive
3) mvn glassfish:deploy -Dglassfish.home=<glassfish_home_path> -Dglassfish.adminPassword=<Admin_Password> --non-recursive


UNINSTALL
=======================================
1) mvn glassfish:delete-domain -Dglassfish.home=<glassfish_home_path> -Dglassfish.adminPassword=<Admin_Password> --non-recursive

UNDEPLOY
=======================================
1) mvn glassfish:undeploy -Dglassfish.home=<glassfish_home_path> -Dglassfish.adminPassword=<Admin_Password> --non-recursive

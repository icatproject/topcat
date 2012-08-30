TOPCAT is an companion product for ICAT.  It provides a user friendly way to browse the contents of an ICAT.  

These installation instructions are intended for installing Topcat as a companion to ICAT.

In order to make it simple to install TOPCAT, the following configuration is assumed:

Ports:
The port 8181 is available for the Glassfish Server to deploy Topcat.

Glassfish Server:
topcat.properties - has been added to $GLASSFISH_HOME/glassfish/domains/domain1/lib/classes

ICAT Servers:
The configuration for the icat servers can be found in the .icat files in the icats.d directory. To add additional icat servers, create a file containing the configuration information in the same format, and add the filename to icats.d/icat.list


Configuration is stored in the following files:
deploy.conf
topcat.properties
icats.d/icat.list
icats.d/localhost.icat

Instructions:

Create a environment similar to the one assumed in these instructions. If it is not possible to be identical, then deploy.conf will require changes. 

Ensure the following conditions on the system: 

- deploy.conf and topcat.properties are created using the example files;
- Database server running;
- Glassfish server running;
- the file topcat.properties is available in the appropriate directory for the glassfish server (see above);
- the topcat war file is named TopCAT.war

Do the following from the topcat directory:

# Create schemas and initialise the tables  
./deploy.sh setupDB

# Create the database pools
./deploy.sh create

# Deploy topcat
./deploy.sh deploy

# Add ICAT connection information
./deploy.sh setupICAT

# Logon to topcat 
https://localhost:8181/TOPCATWeb.jsp 

# To stop, undeploy topcat and delete the connection pools, do the following:
./deploy.sh undeploy
./deploy.sh delete

# To delete the ICAT connection information and the topcat schemas, do the following:
./deploy.sh deleteICAT
./deploy.sh deleteDB


The script deploy.sh suppports the following arguments:
----------------

setupDB			Creates the database schemas for Topcat
deleteDB		Complement of setupDB 
create			Creates the jdbc connection pools between the database and glassfish. Please ensure that the parameters in deploy.conf are correctly defined.
delete			Deletes the jdbc connection pools between the database and glassfish.
deploy			Use with topcat to deploy the specified application.
undeploy 		Use with topcat to undeploy the specified application.
setupICAT		Populates the database schemas for Topcat with information about the available ICATs 
deleteICAT		Complement of setupICAT 


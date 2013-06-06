#!/usr/bin/env python
"""
Set up TopCAT
"""

from subprocess import call
from os import path
from os import environ
from tempfile import NamedTemporaryFile
from tempfile import TemporaryFile
from shutil import copyfile
from optparse import OptionParser
from sys import exit
from os import getcwd

# Variables 
ICAT_DIR = "icats.d"
GLASSFISH_PROPS_FILE = "topcat_glassfish.props"
TOPCAT_PROPS_FILE = "topcat.properties"

REQ_VALUES_TOPCAT = ["topcatProperties", "driver", "glassfish",
                     "topcatAdminUser"]

SUPPORTED_DATABASES = {"DERBY":'', "MYSQL":'', "ORACLE":''}

# Do NOT change, this value is required by TopCAT
CONNECTION_POOL_ID = 'TopCATDB'


def get_and_validate_props(file_name, req_values):
    """
    The get_and_validate_props function gets the properties and validate them
    by calling the get_props and check_keys functions.
    """
    props_dict = get_props(file_name)
    check_keys(props_dict, req_values, file_name)
    return props_dict


def get_props(file_name):
    """
    The get_props function checks if the GLASSFISH_PROPS_FILE file exists and then puts it
    into a Dictionary 
    """ 
    props_dict = {}
    if  not  path.exists(file_name):
        print ("There is no file " + file_name)
        exit(1)
    elif VERBOSE > 1:
        print ("Reading props from " + str(file_name))
    try:
        file_handle = open(file_name, 'r')
        for line in file_handle:
            line = line.strip()
            if line.startswith("#") or line == "":
                continue
            try:
                key, value = line.split("=", 1)
            except ValueError:
                print ("WARNING skipping value in " + str(file_name)
                       + " value:" + line)
            props_dict[key] = value
            if VERBOSE > 2:
                print ("prop " + str(key) + "=" + str(value))
    finally:
        file_handle.close()
    return props_dict     
    
    
def check_keys(props_dict, required_keys, file_name):
    """
    The check_keys function checks if the properties have all been configured
    and that none have been left out.
    """
    for key in required_keys:
        if not props_dict.has_key(key):
            print (key + " must be set in the file " + file_name)
            exit(1)
    return

        
def add_optional_props(props_dict):
    """
    The add_optional_props function checks if optional properties have been
    configured, if not then they're set with the default values.
    """   
    if not props_dict.has_key("domain"):
        props_dict["domain"] = 'domain1'
        if VERBOSE > 2:
            print ("Set domain to " + str(props_dict["domain"]))
    if not props_dict.has_key("port"):
        props_dict["port"] = 4848
        if VERBOSE > 2:
            print ("Set port to " + str(props_dict["port"]))
    if not props_dict.has_key("dbType"):
        props_dict["dbType"] = 'DERBY'
        if VERBOSE > 2:
            print ("Set dbType to " + str(props_dict["dbType"]))
    if not SUPPORTED_DATABASES.has_key(props_dict["dbType"].upper()):
        print ("ERROR " + props_dict["dbType"] + 
               " not supported. Supported databases are: ")
        for key in SUPPORTED_DATABASES.keys():
            print "    " + key
        exit(1)
    return props_dict


def extract_db_props(topcat_properties):
    """
    Extract the database properties from the topcat_properties.
    """
    bits = topcat_properties.split(":")
    props_dict = {}
    for prop in bits:
        try:
            key, value = prop.split("=", 1)
            props_dict[key] = value
            if VERBOSE > 2:
                print ("prop " + str(key) + "=" + str(value))
        except ValueError:
            if prop.startswith('@//'):
                props_dict['hostname'] = prop.split('@//', 1)[1]
                if VERBOSE > 2:
                    print ("prop hostname=" + str(prop.split('@//', 1)[1]))
            elif prop.startswith('@'):
                props_dict['hostname'] = prop.split('@', 1)[1]
                if VERBOSE > 2:
                    print ("prop hostname=" + str(prop.split('@', 1)[1]))
    return props_dict
 
                
def create(conf_props):
    """
    Create the database connection pool and resource
    """
    if VERBOSE > 0:
        print "Create the database connection pool and resource"
    install_props_file(conf_props)
    if conf_props['dbType'].upper() == "DERBY":
        start_derby()
    create_connection_pool(conf_props)     
    create_jdbc_resource()
    create_topcat_admin(conf_props)
    
    
def create_connection_pool(conf_props):
    """
    Set up connection pool
    """
    command = (ASADMIN + " create-jdbc-connection-pool" + 
                 " --datasourceclassname " + conf_props["driver"] + 
                 " --restype javax.sql.DataSource --failconnection=true"
                 " --steadypoolsize 2 --maxpoolsize 8 --ping" + 
                 " --property " + conf_props["topcatProperties"] + " " + 
                 CONNECTION_POOL_ID)          
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR creating database connection pool"
        exit(1)


def create_jdbc_resource():
    """
    Set up jdbc resource
    """
    command = (ASADMIN + " create-jdbc-resource --connectionpoolid " + 
                 CONNECTION_POOL_ID + " " + "jdbc/" + 
                 CONNECTION_POOL_ID)
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR creating jdbc resource"
        exit(1)        


def create_topcat_admin(conf_props):
    """
    Set up topcat admin user
    """
    user = conf_props['topcatAdminUser']
    print "\nCreating TopCAT Admin User. Please enter new password:"
    command = (ASADMIN + " create-file-user --groups topcatAdmin " + user)
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)
    if retcode > 0:
        print "ERROR creating user " + user
        exit(1)

def install_props_file(conf_props):
    """
    Copy the TOPCAT_PROPS_FILE file
    """
    dest_dir = path.join(conf_props["glassfish"], "glassfish", "domains",
                     conf_props["domain"], "lib", "classes")
    if not path.exists(dest_dir):
        print "ERROR Cannot find the directory " + dest_dir
        exit(1)
    dest = path.join(dest_dir, TOPCAT_PROPS_FILE)
        
    if path.exists(dest):
        print ("Found existing " + TOPCAT_PROPS_FILE + " in " + str(dest_dir) 
               + " new file not copied")
    else:
        if not path.exists(TOPCAT_PROPS_FILE):
            print ("ERROR Cannot find " + TOPCAT_PROPS_FILE + 
                   " in the current directory")
            exit(1)
        copyfile(TOPCAT_PROPS_FILE, dest)
        if VERBOSE > 0:
            print "copied " + TOPCAT_PROPS_FILE + " to " + str(dest)
        
        
def start_derby():
    """
    Ensure the derby database is running
    """
    if VERBOSE > 0:
        print "Ensure the derby database is running"
    command = ASADMIN + " start-database --dbhost 127.0.0.1"
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR starting Derby database"
        exit(1)


def delete():
    """
    Delete the database connection pool and resource
    """
    if VERBOSE > 0:
        print "Delete the database connection pool and resource"
    error = False
    command = (ASADMIN + " " + 
    "delete-jdbc-resource jdbc/" + CONNECTION_POOL_ID)
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR deleting jdbc resource"
        error = True

    command = (ASADMIN + " " + 
    "delete-jdbc-connection-pool " + CONNECTION_POOL_ID) 
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR deleting database connection pool"
        error = True
        
    if error:
        exit(1)


def deploy():
    """
    Deploy the TopCAT application
    """
    if VERBOSE > 0:
        print "Deploy the TopCAT application"
    command = ASADMIN + " deploy TopCAT.war"
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR deploying TopCAT"
        exit(1)


def undeploy():
    """
    Un-deploy the TopCAT application
    """
    if VERBOSE > 0:
        print "Undeploy the TopCAT application"
    command = ASADMIN + " undeploy TopCAT"
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())    
    if retcode > 0:
        print "ERROR un-deploying TopCAT"
        exit(1)


def status():
    """
    display the status as reported by asadmin
    """
    print "\nStatus"
    print "\nDomains"
    command = ASADMIN + " list-domains"
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR listing domains"
    print "\nComponents"
    command = ASADMIN + " list-components"
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR listing components"
    print "\nJDBC connection pools"
    command = ASADMIN + " list-jdbc-connection-pools"
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR listing jdbc connection pools"
    print "\nJDBC resources"
    command = ASADMIN + " list-jdbc-resources"
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR listing jdbc resources"
        

def upgrade(conf_props):
    """
    Upgrade the database
    """
    if VERBOSE > 0:
        print "Upgrade database"
    elif VERBOSE > 1:
        print ("Reading props from " + str(conf_props))
    db_props = extract_db_props(conf_props['topcatProperties'])
    sql_command = get_sql_command(conf_props, db_props)
    upgrade_db(conf_props, sql_command, db_props)


def get_sql_command(conf_props, db_props):
    """
    Get the sql command to use based on the type of database
    """
    if conf_props['dbType'].upper() == "DERBY":
        sql_command = IJ
        start_derby()
    elif conf_props['dbType'].upper() == "ORACLE":
        try:
            db_props['oracleHome'] = environ['ORACLE_HOME']
        except KeyError:
            print "ERROR - Please set ORACLE_HOME"
            exit(1)
        sqlplus = path.join(db_props['oracleHome'], "bin", "sqlplus")
        try:
            db_props['password']
        except KeyError:
            print ("ERROR - Unable to extract DB password from" 
                   + " topcatProperties in " + GLASSFISH_PROPS_FILE)
            exit(1)
        try:
            db_props['hostname']
        except KeyError:
            print ("ERROR - Unable to extract DB hostname from"
                   + " topcatProperties in " + GLASSFISH_PROPS_FILE)
            exit(1)
        sql_command = (sqlplus + " " + db_props['user'] + "/" + 
                       db_props['password'] + "@" + 
                       db_props['hostname'] + " @")
    elif conf_props['dbType'].upper() == "MYSQL":
        try:
            db_props['password']
        except KeyError:
            print ("ERROR - Unable to extract DB password from" 
                   + " topcatProperties in " + GLASSFISH_PROPS_FILE)
            exit(1)
        try:
            db_props['databaseName']
        except KeyError:
            print ("ERROR - Unable to extract DB databaseName from" 
                   + " topcatProperties in " + GLASSFISH_PROPS_FILE)
            exit(1)        
        sql_command = (MYSQL + " -u " + db_props['user'] + " -p" + 
                       db_props['password'] + " " + db_props['databaseName'] + 
                       "<")
    return sql_command


def upgrade_db(conf_props, sql_command, db_props):
    """
    Drop the columns AUTHENTICATION_SERVICE_URL and AUTHENTICATION_SERVICE_TYPE from TOPCAT_ICAT_SERVER
    Add the column DOWNLOAD_SERVICE_URL to the table TOPCAT_ICAT_SERVER
    Add the column PREPARED_ID to the table TOPCAT_USER_DOWNLOAD
    Add the column DISPLAY_NAME to the table ICAT_AUTHENTICATION
    Rename the table ICAT_AUTHENTICATION to TOPCAT_ICAT_AUTHENTICATION
    """
    if conf_props['dbType'].upper() == "ORACLE":
        sql_file = NamedTemporaryFile(dir=getcwd(), suffix='.sql')
    else:
        sql_file = NamedTemporaryFile()
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("connect 'jdbc:derby://" + db_props['serverName'] + 
                       ":1527/" + db_props['DatabaseName'] + "';")
        
    sql_file.write("ALTER TABLE TOPCAT_ICAT_SERVER DROP COLUMN "
                   "AUTHENTICATION_SERVICE_URL\n;\n")
    sql_file.write("ALTER TABLE TOPCAT_ICAT_SERVER DROP COLUMN "
                   "AUTHENTICATION_SERVICE_TYPE\n;\n")

    sql_file.write("ALTER TABLE TOPCAT_ICAT_SERVER ")
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("ADD DOWNLOAD_SERVICE_URL VARCHAR(255)\n;\n")
    else:
        sql_file.write("ADD DOWNLOAD_SERVICE_URL VARCHAR2(255)\n;\n")
        
    sql_file.write("ALTER TABLE TOPCAT_USER_DOWNLOAD ")
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("ADD PREPARED_ID VARCHAR(255)\n;\n")
    else:
        sql_file.write("ADD PREPARED_ID VARCHAR2(255)\n;\n")
    
    sql_file.write("ALTER TABLE ICAT_AUTHENTICATION ")
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("ADD DISPLAY_NAME VARCHAR(255)\n;\n")
    else:
        sql_file.write("ADD DISPLAY_NAME VARCHAR2(255)\n;\n")

    if conf_props['dbType'].upper() == "ORACLE":
        sql_file.write("RENAME ICAT_AUTHENTICATION TO "
                       "TOPCAT_ICAT_AUTHENTICATION\n;\n")
    else:
        sql_file.write("RENAME TABLE ICAT_AUTHENTICATION TO "
                       "TOPCAT_ICAT_AUTHENTICATION\n;\n")
        
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("disconnect;")
    sql_file.write("exit\n;")
    command = sql_command + " " + sql_file.name
    if VERBOSE > 1:
        print sql_command
        sql_file.seek(0)
        print sql_file.readlines()
    sql_file.seek(0)
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR updating table"
        exit(1)
    out_file.seek(0)
    lines = out_file.readlines()
    if VERBOSE > 2:
        for line in lines:
            print line
    for line in lines:
        if line.find("ERROR") > -1:
            print "ERROR updating table"
            for lin in lines:
                print lin
            exit(1)
    out_file.close()
    sql_file.close() 
    print ("updated tables TOPCAT_ICAT_SERVER, TOPCAT_USER_DOWNLOAD and " 
           "ICAT_AUTHENTICATION")
    return


PARSER = OptionParser()
PARSER.add_option("--create", dest="create",
                  help="Creates the database connection pool",
                  action="store_true")
PARSER.add_option("--delete", dest="delete",
                  help="Deletes the database connection pool",
                  action="store_true")
PARSER.add_option("--deploy", dest="deploy",
                  help="Deploys the TOPCat application to Glassfish",
                  action="store_true")
PARSER.add_option("--undeploy", dest="undeploy",
                  help="Undeploys the TOPCat application from Glassfish",
                  action="store_true")
PARSER.add_option("--status", dest="status",
                  help="Display status information",
                  action="store_true")
PARSER.add_option("--upgrade", dest="upgrade",
                  help=("Upgrade the database for the migration between 1.7"
                        + " and 1.9"), action="store_true")
PARSER.add_option("-v", "--verbose", action="count", default=0,
                    help="increase output verbosity")

(OPTIONS, ARGS) = PARSER.parse_args()
VERBOSE = OPTIONS.verbose

CONF_PROPS = get_and_validate_props(GLASSFISH_PROPS_FILE, REQ_VALUES_TOPCAT)
CONF_PROPS = add_optional_props(CONF_PROPS)
 
ASADMIN = path.join(CONF_PROPS["glassfish"], "bin", "asadmin")
# if windows:
#    ASADMIN = ASADMIN + ".bat"
ASADMIN = ASADMIN + " --port " + CONF_PROPS["port"]
 
IJ = path.join(CONF_PROPS["glassfish"], "javadb", "bin", "ij")
MYSQL = "mysql"

if OPTIONS.create:
    create(CONF_PROPS)
elif OPTIONS.delete:
    delete()
elif OPTIONS.deploy:
    deploy()
elif OPTIONS.undeploy:
    undeploy()
elif OPTIONS.status:
    status()
elif OPTIONS.upgrade:
    upgrade(CONF_PROPS)
else:
    print ("\nYou must provide an option\n")
    print PARSER.print_help()
    exit(1)
    
print ('All done')
exit(0)


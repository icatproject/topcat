#!/usr/bin/env python
"""
Set up TopCAT
"""

from subprocess import call
from os import path
from os import listdir
from os import environ
from tempfile import NamedTemporaryFile
from tempfile import TemporaryFile
from shutil import copyfile
from optparse import OptionParser
from sys import exit
import re
from os import getcwd

#Variables 
ICAT_DIR = "icats.d"
TOPCAT_PROPS_FILE = "glassfish.props"

REQ_VALUES_TOPCAT = ["topcatProperties", "driver", "glassfish"]

REQ_VALUES_ICAT = ["facilityName", "wsdlUrl", "icatVersion",
                   "authenticationProperties"]
OPT_VALUES_ICAT = ["defaultUser", "defaultPassword", "pluginName",
                   "downloadPluginName"]

SUPPORTED_DATABASES = {"DERBY":'', "MYSQL":'', "ORACLE":''}

# Do NOT change, this value is required by TopCAT
CONNECTION_POOL_ID = 'TopCATDB'

def get_props(file_name):
    """
    The get_props function checks if the TOPCAT_PROPS_FILE file exists and then puts it
    into a Dictionary 
    """ 
    props_dict = {}
    if  not  path.exists(file_name):
        print ("There is no file " + file_name)
        exit(1)
    try:
        file_handle = open(file_name, 'r')
        for line in file_handle:
            line = line.strip() 
            if line.startswith("#") or line == "":
                continue
            key, value = line.split("=", 1)
            props_dict[key] = value
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

     
def get_and_validate_props(file_name, req_values):
    """
    The get_and_validate_props function gets the properties and validate them
    by calling the get_props and check_keys functions.
    """
    props_dict = get_props(file_name)
    check_keys(props_dict, req_values, file_name)
    return props_dict
        
        
def add_optional_props(props_dict):
    """
    The add_optional_props function checks if optional properties have been
    configured, if not then they're set with the default values.
    """   
    if not props_dict.has_key("domain"):
        props_dict["domain"] = 'domain1'
    if not props_dict.has_key("port"):
        props_dict["port"] = 4848
    if not props_dict.has_key("dbType"):
        props_dict["dbType"] = 'DERBY'
    if not SUPPORTED_DATABASES.has_key(props_dict["dbType"].upper()):
        print ("ERROR " + props_dict["dbType"] + 
               " not supported. Supported databases are: ")
        for key in SUPPORTED_DATABASES.keys():
            print "    " + key
        exit(1)
    return props_dict


def add_icat_optional_props(props_dict):
    """
    The add_optional_props function checks if optional properties have been
    configured, if not then they're set with the default values.
    """   
    for key in OPT_VALUES_ICAT:
        if not props_dict.has_key(key):
            props_dict[key] = ''
    return props_dict

     
def get_authentication_props(file_name):
    """
    Get the components of the authentication_props from the .icat file. There
    can be zero or more authentication_props parameters.
    """
    props_list = []
    if  not  path.exists(file_name):
        print ("There is no file " + file_name)
        exit(1)
    try:
        file_handle = open(file_name)
        for line in file_handle:
            line = line.strip() 
            if not line.startswith("authenticationProperties="):
                continue
            bits = line.split("=", 1)[1].split(":")
            props_dict = {}
            for prop in bits:
                key, value = prop.split("=", 1)
                props_dict[key] = value
            props_list.append(props_dict)
    finally:
        file_handle.close()
    return props_list     


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
        except ValueError:
            if prop.startswith('@//'):
                props_dict['hostname'] = prop.split('@//', 1)[1]
    return props_dict
 
                
def create(conf_props):
    """
    Create the database connection pool and resource
    """
    install_props_file()
    if conf_props['dbType'].upper() == "DERBY":
        start_derby()
    # Set up connection pool
    command = (ASADMIN + " create-jdbc-connection-pool" + 
                 " --datasourceclassname " + conf_props["driver"] + 
                 " --restype javax.sql.DataSource --failconnection=true"
                 " --steadypoolsize 2 --maxpoolsize 8 --ping" + 
                 " --property " + conf_props["topcatProperties"] + " " + 
                 CONNECTION_POOL_ID)          
    retcode = call(command, shell=True)
    if retcode > 0:
        print "ERROR creating database connection pool"
        exit(1)
            
    # Set up jdbc resource
    command = (ASADMIN + " create-jdbc-resource --connectionpoolid " + 
                 CONNECTION_POOL_ID + " " + "jdbc/" + 
                 CONNECTION_POOL_ID)
    retcode =  call(command, shell=True)
    if retcode > 0:
        print "ERROR creating jdbc resource"
        exit(1)


def install_props_file():
    """
    Copy the topcat.properties file
    """
    dest_dir = path.join(CONF_PROPS_TOPCAT["glassfish"], "glassfish", "domains",
                     CONF_PROPS_TOPCAT["domain"], "lib", "classes")
    if not path.exists(dest_dir):
        print "ERROR Cannot find the directory" + dest_dir
        exit(1)
    dest = path.join(dest_dir, "topcat.properties")
        
    if not path.exists(dest):
        if not  path.exists('topcat.properties'):
            print "ERROR Cannot find topcat.properties in the current directory"
            exit(1)
        copyfile('topcat.properties', dest)
        
        
def start_derby():
    """
    Ensure the dearby database is running
    """
    command = ASADMIN + " start-database --dbhost 127.0.0.1"
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR starting Derby database"
        exit(1)
    out_file.close()


def delete():
    """
    Delete the database connection pool and resource
    """
    error = False
    command = (ASADMIN + " " + 
    "delete-jdbc-resource jdbc/" + CONNECTION_POOL_ID)
    retcode = call(command, shell=True)     
    if retcode > 0:
        print "ERROR deleting jdbc resource"
        error = True

    command = (ASADMIN + " " + 
    "delete-jdbc-connection-pool " + CONNECTION_POOL_ID) 
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR deleting database connection pool"
        error = True
        
    if error:
        exit(1)


def deploy():
    """
    Deploy the TopCAT application
    """
    command = ASADMIN + " deploy TopCAT.war"
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR deploying TopCAT"
        exit(1)


def undeploy():
    """
    Un-deploy the TopCAT application
    """
    command = ASADMIN + " undeploy TopCAT"
    retcode = call(command, shell=True)    
    if retcode > 0:
        print "ERROR un-deploying TopCAT"
        exit(1)


def add_icat(conf_props):
    """
    Set up TopCAT to point to one or more ICATs
    """
    if  not path.exists(ICAT_DIR):    
        print ('There is no ' + ICAT_DIR + " directory")
        exit(1)
    db_props = extract_db_props(conf_props['topcatProperties'])
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
        sql_command = (sqlplus + " " + db_props['user'] + "/" + 
                       db_props['password'] + "@" + 
                       db_props['hostname'] + " @")
    elif conf_props['dbType'].upper() == "MYSQL":
        sql_command = (MYSQL + " -u " + db_props['user'] + " -p" + 
                       db_props['password'] + " " + db_props['databaseName'] + 
                       "<")
    dir_list = listdir(ICAT_DIR)
    pattern = re.compile("\.icat$")
    icat_id = get_next_server_id(conf_props, sql_command, db_props)
    auth_id = get_next_auth_id(conf_props, sql_command, db_props)
    for fname in dir_list:
        if pattern.search(fname) != None:
            auth_id = add_icat_entry(conf_props, ICAT_DIR + "/" + fname,
                                      icat_id, auth_id, sql_command, db_props)
            icat_id = icat_id + 1      


def get_next_server_id(conf_props, sql_command, db_props):
    """
    Get the next server id to use
    """
    select = "SELECT MAX(ID) FROM TOPCAT_ICAT_SERVER\n;\n"
    current_id = (get_value_from_database(conf_props, sql_command, db_props,
                                          select))
    try:
        return int(current_id) + 1
    except ValueError:
        return 1


def get_next_auth_id(conf_props, sql_command, db_props):  
    """
    Get the next auth id to use
    """
    select = "SELECT MAX(ID) FROM  ICAT_AUTHENTICATION\n;\n"
    current_id = (get_value_from_database(conf_props, sql_command, db_props,
                                          select))
    try:
        return int(current_id) + 1
    except ValueError:
        return 1


def check_icat_name_exists(conf_props, sql_command, db_props, name):  
    """
    Check the database to see if there is already an enter for this ICAT
    """
    select = ("SELECT COUNT(*) FROM  TOPCAT_ICAT_SERVER WHERE NAME='" + name + 
              "'\n;\n")
    count = get_value_from_database(conf_props, sql_command, db_props, select)
    try:
        count = int(count)
    except ValueError:
        print "ERROR getting data from database"
        exit(1)
    if count > 0:
        return True
    else:
        return False
       

def get_value_from_database(conf_props, sql_command, db_props, select):
    """
    Get an int from the database in response to the given query
    """

    if conf_props['dbType'].upper() == "ORACLE":
        sql_file = NamedTemporaryFile(dir=getcwd(), suffix='.sql')
    else:
        sql_file = NamedTemporaryFile()
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("connect 'jdbc:derby://" + db_props['serverName'] + 
                       ":1527/" + db_props['DatabaseName'] + "';")
    sql_file.write(select)
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("disconnect;")
    sql_file.write("exit\n;")
    command = sql_command + " " + sql_file.name
    sql_file.seek(0)
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR getting data from database"
        exit(1)
    out_file.seek(0)
    if conf_props['dbType'].upper() == "DERBY":
        ret_value = out_file.readlines()[6].strip()
    elif conf_props['dbType'].upper() == "ORACLE":
        ret_value = out_file.readlines()[12].strip()
    elif conf_props['dbType'].upper() == "MYSQL":
        ret_value = out_file.readlines()[1].strip()
    out_file.close()
    sql_file.close()
    return ret_value



def add_icat_entry(conf_props, fname, icat_id, auth_id, sql_command, db_props):
    """
    Add an icat entry to the database
    """
    icat_props = get_and_validate_props(fname, REQ_VALUES_ICAT)
    icat_props = add_icat_optional_props(icat_props)
    authentication_props = get_authentication_props(fname)
    if check_icat_name_exists(conf_props, sql_command, db_props,
                              icat_props['facilityName']):
        print ("WARNING The database already contains an entry for " + 
               icat_props['facilityName'] + ", data NOT added for " + 
               icat_props['facilityName'])
        return
    if conf_props['dbType'].upper() == "ORACLE":
        sql_file = NamedTemporaryFile(dir=getcwd(), suffix='.sql')
    else:
        sql_file = NamedTemporaryFile()
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("connect 'jdbc:derby://" + db_props['serverName'] + 
                       ":1527/" + db_props['DatabaseName'] + "';")
    sql_file.write("INSERT INTO TOPCAT_ICAT_SERVER ")
    sql_file.write("(ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD," + 
                   " PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES (")
    sql_file.write(str(icat_id) + ", ")
    sql_file.write("'" + icat_props['facilityName'] + "', ")
    sql_file.write("'" + icat_props['wsdlUrl'] + "', ")
    sql_file.write("'" + icat_props['defaultUser'] + "', ")
    sql_file.write("'" + icat_props['defaultPassword'] + "', ")
    sql_file.write("'" + icat_props['pluginName'] + "', ")
    sql_file.write("'" + icat_props['downloadPluginName'] + "', ")
    sql_file.write("'" + icat_props['icatVersion'] + "')\n;\n")
    
    for auth in authentication_props:
        sql_file.write("INSERT INTO ICAT_AUTHENTICATION ")
        sql_file.write("(ID, AUTHENTICATION_SERVICE_URL, ")
        sql_file.write("AUTHENTICATION_TYPE, PLUGIN_NAME, SERVER_ID) VALUES (")
        sql_file.write(str(auth_id) + ", ")
        sql_file.write("'" + auth['url'] + "', ")
        sql_file.write("'" + auth['type'] + "', ")
        sql_file.write("'" + auth['plugin'] + "', ")
        sql_file.write("" + str(icat_id) + ")\n;\n")
        auth_id = auth_id + 1
        
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("disconnect;")
    sql_file.write("exit\n;")
    command = sql_command + " " + sql_file.name
    sql_file.seek(0)
    retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR writing icat data to database"
        exit(1)
    sql_file.close()
    return auth_id


CONF_PROPS_TOPCAT = get_and_validate_props(TOPCAT_PROPS_FILE, REQ_VALUES_TOPCAT)
CONF_PROPS_TOPCAT = add_optional_props(CONF_PROPS_TOPCAT)

ASADMIN = path.join(CONF_PROPS_TOPCAT["glassfish"], "bin", "asadmin")
#if windows:
#    ASADMIN = ASADMIN + ".bat"
ASADMIN = ASADMIN + " --port " + CONF_PROPS_TOPCAT["port"]

IJ = path.join(CONF_PROPS_TOPCAT["glassfish"], "javadb", "bin", "ij")
MYSQL = "mysql"

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
PARSER.add_option("--addICAT", dest="addICAT",
                  help="Adds the ICATs described in the .icat files to TopCAT",
                  action="store_true")

(OPTIONS, ARGS) = PARSER.parse_args()

if OPTIONS.create:
    create(CONF_PROPS_TOPCAT)
elif OPTIONS.delete:
    delete()
elif OPTIONS.deploy:
    deploy()
elif OPTIONS.undeploy:
    undeploy()
elif OPTIONS.addICAT:
    add_icat(CONF_PROPS_TOPCAT)
else:
    print ("You must provide an arg")
    exit(1)
    
print ('All done')
exit(0)


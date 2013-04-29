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

# Variables 
ICAT_DIR = "icats.d"
TOPCAT_PROPS_FILE = "glassfish.props"

REQ_VALUES_TOPCAT = ["topcatProperties", "driver", "glassfish"]

REQ_VALUES_ICAT = ["facilityName", "wsdlUrl", "icatVersion",
                   "authenticationProperties"]
OPT_VALUES_ICAT = ["defaultUser", "defaultPassword", "pluginName",
                   "downloadPluginName"]

SUPPORTED_DATABASES = {"DERBY":'', "MYSQL":'', "ORACLE":''}
SUPPORTED_ICATS = {"v420":''}

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
    The get_props function checks if the TOPCAT_PROPS_FILE file exists and then puts it
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
    if not path.exists(file_name):
        print ("There is no file " + file_name)
        exit(1)
    elif VERBOSE > 1:
        print ("Reading props from " + str(file_name))
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
                if VERBOSE > 2:
                    print ("prop " + str(key) + "=" + str(value))
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
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        print "ERROR creating database connection pool"
        exit(1)
            
    # Set up jdbc resource
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


def install_props_file():
    """
    Copy the topcat.properties file
    """
    dest_dir = path.join(CONF_PROPS_TOPCAT["glassfish"], "glassfish", "domains",
                     CONF_PROPS_TOPCAT["domain"], "lib", "classes")
    if not path.exists(dest_dir):
        print "ERROR Cannot find the directory " + dest_dir
        exit(1)
    dest = path.join(dest_dir, "topcat.properties")
        
    if path.exists(dest):
        print ("Found existing topcat.properties in " + str(dest_dir) 
               + " new file not copied")
    else:
        if not path.exists('topcat.properties'):
            print "ERROR Cannot find topcat.properties in the current directory"
            exit(1)
        copyfile('topcat.properties', dest)
        if VERBOSE > 0:
            print "copied topcat.properties to " + str(dest)
        
        
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
#    out_file.close()


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


def status(conf_props):
    """
    display the status as reported by asadmin
    """
    print ("\nStatus")
    list_asadmin_bits()
    list_icat_servers(conf_props)


def list_asadmin_bits():  
    """
    display the status as reported by asadmin
    """
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
        
        
def list_icat_servers(conf_props):  
    """
    List the ICAT servers in the database
    """
    print "\nUsing " + str(conf_props['dbType'].upper()) + " database\n"
    db_props = extract_db_props(conf_props['topcatProperties'])
    sql_command = get_sql_command(conf_props, db_props)
    select = ("SELECT SERVER_URL FROM  TOPCAT_ICAT_SERVER \n;\n")
    urls = get_value_from_database(conf_props, sql_command, db_props, select)   
    if conf_props['dbType'].upper() == "DERBY":
        index = 6
        stop = len(urls) - 5
    elif conf_props['dbType'].upper() == "ORACLE":
        index = 12
        stop = len(urls) - 4
    elif conf_props['dbType'].upper() == "MYSQL":
        index = 1
        stop = len(urls)  
    print "ICAT WSDL URLS: "
    while index < stop:
        print " " + str(urls[index]).strip()
        index = index + 1
    print

            
def add_icat(conf_props):
    """
    Set up TopCAT to point to one or more ICATs
    """
    if VERBOSE > 0:
        print "Set up TopCAT to point to one or more ICATs"
    if  not path.exists(ICAT_DIR):    
        print ('There is no ' + ICAT_DIR + " directory")
        exit(1)
    elif VERBOSE > 1:
        print ("Reading props from " + str(conf_props))
    db_props = extract_db_props(conf_props['topcatProperties'])
    sql_command = get_sql_command(conf_props, db_props)
    dir_list = listdir(ICAT_DIR)
    pattern = re.compile("\.icat$")
    icat_id = get_next_server_id(conf_props, sql_command, db_props)
    auth_id = get_next_auth_id(conf_props, sql_command, db_props)
    for fname in dir_list:
        if pattern.search(fname) != None:
            auth_id = add_icat_entry(conf_props, ICAT_DIR + "/" + fname,
                                      icat_id, auth_id, sql_command, db_props)
            icat_id = icat_id + 1      


def upgrade(conf_props):
    """
    Upgrade the database
    """
    if VERBOSE > 0:
        print "Upgrade database"
    if  not path.exists(ICAT_DIR):    
        print ('There is no ' + ICAT_DIR + " directory")
        exit(1)
    elif VERBOSE > 1:
        print ("Reading props from " + str(conf_props))
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
                   + " topcatProperties in glassfish.props")
            exit(1)
        try:
            db_props['hostname']
        except KeyError:
            print ("ERROR - Unable to extract DB hostname from"
                   + " topcatProperties in glassfish.props")
            exit(1)
        sql_command = (sqlplus + " " + db_props['user'] + "/" + 
                       db_props['password'] + "@" + 
                       db_props['hostname'] + " @")
    elif conf_props['dbType'].upper() == "MYSQL":
        try:
            db_props['password']
        except KeyError:
            print ("ERROR - Unable to extract DB password from" 
                   + " topcatProperties in glassfish.props")
            exit(1)
        try:
            db_props['databaseName']
        except KeyError:
            print ("ERROR - Unable to extract DB databaseName from" 
                   + " topcatProperties in glassfish.props")
            exit(1)        
        sql_command = (MYSQL + " -u " + db_props['user'] + " -p" + 
                       db_props['password'] + " " + db_props['databaseName'] + 
                       "<")
    return sql_command


def get_next_server_id(conf_props, sql_command, db_props):
    """
    Get the next server id to use
    """
    select = "SELECT MAX(ID) FROM TOPCAT_ICAT_SERVER\n;\n"
    current_id = (get_single_value_from_database(conf_props, sql_command,
                                           db_props, select))
    try:
        next_id = int(current_id) + 1
        if VERBOSE > 2:
            print ("Next server id:" + str(next_id))
        return next_id
    except ValueError:
        if VERBOSE > 2:
            print ("Next server id:1")
        return 1


def get_next_auth_id(conf_props, sql_command, db_props):  
    """
    Get the next auth id to use
    """
    select = "SELECT MAX(ID) FROM  ICAT_AUTHENTICATION\n;\n"
    current_id = (get_single_value_from_database(conf_props, sql_command,
                                          db_props, select))
    try:
        next_id = int(current_id) + 1
        if VERBOSE > 2:
            print ("Next auth id:" + str(next_id))
        return next_id
    except ValueError:
        if VERBOSE > 2:
            print ("Next auth id:1")
        return 1


def check_icat_name_exists(conf_props, sql_command, db_props, name):  
    """
    Check the database to see if there is already an enter for this ICAT
    """
    select = ("SELECT COUNT(*) FROM  TOPCAT_ICAT_SERVER WHERE NAME='" + name + 
              "'\n;\n")
    count = get_single_value_from_database(conf_props, sql_command, db_props,
                                            select)
    try:
        count = int(count)
    except ValueError:
        print "ERROR getting data from database"
        exit(1)
    if count > 0:
        if VERBOSE > 2:
            print (str(name) + " already in database")
        return True
    else:
        if VERBOSE > 2:
            print (str(name) + " not in database")
        return False
       

def get_single_value_from_database(conf_props, sql_command, db_props, select):
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
    if VERBOSE > 1:
        print sql_command
        sql_file.seek(0)
        print sql_file.readlines()
    sql_file.seek(0)
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR getting data from database"
        exit(1)
    out_file.seek(0)
    lines = out_file.readlines()
    if VERBOSE > 2:
        for line in lines:
            print line
    for line in lines:
        if line.find("ERROR") > -1:
            print line
            print "ERROR getting data from database"
            exit(1)
    if conf_props['dbType'].upper() == "DERBY":
        ret_value = lines[6].strip()
    elif conf_props['dbType'].upper() == "ORACLE":
        ret_value = lines[12].strip()
    elif conf_props['dbType'].upper() == "MYSQL":
        ret_value = lines[1].strip()
    out_file.close()
    sql_file.close()
    return ret_value


def get_value_from_database(conf_props, sql_command, db_props, select):
    """
    Get the results from the database in response to the given query
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
    if VERBOSE > 1:
        print sql_command
        sql_file.seek(0)
        print sql_file.readlines()
    sql_file.seek(0)
    out_file = TemporaryFile()
    retcode = call(command, shell=True, stdout=out_file) 
    if retcode > 0:
        print "ERROR getting data from database"
        exit(1)
    print "rt " + str(retcode)
    out_file.seek(0)
    lines = out_file.readlines()
    if VERBOSE > 2:
        for line in lines:
            print line
    for line in lines:
        if line.find("ERROR") > -1:
            print line
            print "ERROR getting data from database"
            exit(1)
    out_file.close()
    sql_file.close()
    return lines


def add_icat_entry(conf_props, fname, icat_id, auth_id, sql_command, db_props):
    """
    Add an icat entry to the database
    """
    icat_props = get_and_validate_props(fname, REQ_VALUES_ICAT)
    icat_props = add_icat_optional_props(icat_props)
    validate_version(icat_props)
    authentication_props = get_authentication_props(fname)
    if check_icat_name_exists(conf_props, sql_command, db_props,
                              icat_props['facilityName']):
        print ("WARNING The database already contains an entry for " + 
               icat_props['facilityName'] + ", data NOT added for " + 
               icat_props['facilityName'])
        return auth_id
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
    if VERBOSE > 1:
        print command
    sql_file.seek(0)
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:    
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR writing icat data to database"
        exit(1)
    sql_file.close()
    return auth_id


def validate_version(icat_props):
    """
    Check that the version number supplied is one that is know to Topcat
    """
    if VERBOSE > 1:
        print "Checking icat version"
    if VERBOSE > 1:
        print "Supported versions are: "
        for key in SUPPORTED_ICATS.keys():
            print "    " + key
    if not SUPPORTED_ICATS.has_key(icat_props["icatVersion"]):
        print ("ERROR " + icat_props["icatVersion"] + 
               " not supported. Supported versions are: ")
        for key in SUPPORTED_ICATS.keys():
            print "    " + key
        print ("N.B. Please use v420 for all 4.2.n icats")
        exit(1)


def upgrade_db(conf_props, sql_command, db_props):
    """
    Add the column DOWNLOAD_SERVICE_URL to the table TOPCAT_ICAT_SERVER
    Add the column PREPARED_ID to the table TOPCAT_USER_DOWNLOAD
    """
    if conf_props['dbType'].upper() == "ORACLE":
        sql_file = NamedTemporaryFile(dir=getcwd(), suffix='.sql')
    else:
        sql_file = NamedTemporaryFile()
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("connect 'jdbc:derby://" + db_props['serverName'] + 
                       ":1527/" + db_props['DatabaseName'] + "';")
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
    if conf_props['dbType'].upper() == "DERBY":
        sql_file.write("disconnect;")
    sql_file.write("exit\n;")
    command = sql_command + " " + sql_file.name
    if VERBOSE > 1:
        print command
    sql_file.seek(0)
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR updating table"
        exit(1)
    sql_file.close()
    print "updated tables TOPCAT_ICAT_SERVER and TOPCAT_USER_DOWNLOAD"
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
PARSER.add_option("--addICAT", dest="addICAT",
                  help="Adds the ICATs described in the .icat files to TopCAT",
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

CONF_PROPS_TOPCAT = get_and_validate_props(TOPCAT_PROPS_FILE, REQ_VALUES_TOPCAT)
CONF_PROPS_TOPCAT = add_optional_props(CONF_PROPS_TOPCAT)
 
ASADMIN = path.join(CONF_PROPS_TOPCAT["glassfish"], "bin", "asadmin")
# if windows:
#    ASADMIN = ASADMIN + ".bat"
ASADMIN = ASADMIN + " --port " + CONF_PROPS_TOPCAT["port"]
 
IJ = path.join(CONF_PROPS_TOPCAT["glassfish"], "javadb", "bin", "ij")
MYSQL = "mysql"

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
elif OPTIONS.status:
    status(CONF_PROPS_TOPCAT)
elif OPTIONS.upgrade:
    upgrade(CONF_PROPS_TOPCAT)
else:
    print ("You must provide an arg")
    exit(1)
    
print ('All done')
exit(0)


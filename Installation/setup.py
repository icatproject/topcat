#!/usr/bin/env python
"""
Set up TopCAT
"""

from subprocess import call
from os import path
from tempfile import TemporaryFile
from shutil import copyfile
from optparse import OptionParser
import sys

# Variables
GLASSFISH_PROPS_FILE = "topcat-setup.properties"
TOPCAT_PROPS_FILE = "topcat.properties"

REQ_VALUES_TOPCAT = ["topcatProperties", "driver", "glassfish", "topcatWar",
                     "topcatAdminWar", "topcatAdminUser"]

SUPPORTED_DATABASES = {"DERBY":'', "MYSQL":'', "ORACLE":''}

# Do NOT change, this value is required by TopCAT
CONNECTION_POOL_ID = 'TopCATDB'

def abort(msg):
    """
    Print the message to standard error and exit.
    """
    print >> sys.stderr, msg
    sys.exit(1)


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
    The get_props function checks if the GLASSFISH_PROPS_FILE file exists and 
    then puts it into a Dictionary
    """
    props_dict = {}
    if  not  path.exists(file_name):
        abort("There is no file " + file_name)
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
            abort(key + " must be set in the file " + file_name)
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
        msg = ("ERROR " + props_dict["dbType"] + 
               " not supported. Supported databases are: ")
        for key in SUPPORTED_DATABASES.keys():
            msg = msg + "\n    " + key
        abort(msg)
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


def run_cmd(command, errormsg, abortOnError=True):
    """
    Run the command
    """
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile()) 
    if retcode > 0:
        if abortOnError:
            abort(errormsg)
        else:
            print >> sys.stderr, errormsg
            return 1
    return 0


def create(conf_props):
    """
    Create the jdbc connection pool and resource, and the topcat admin user and
    enable the principal to role Manager
    """
    if VERBOSE > 0:
        print "Create the database connection pool and resource"
    install_props_file(conf_props)
    create_connection_pool(conf_props)
    create_jdbc_resource()
    create_topcat_admin(conf_props)
    enable_principal_to_role_mng()


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
    run_cmd(command, "ERROR creating JDBC connection pool")          


def create_jdbc_resource():
    """
    Set up jdbc resource
    """
    command = (ASADMIN + " create-jdbc-resource --connectionpoolid " + 
                 CONNECTION_POOL_ID + " " + "jdbc/" + 
                 CONNECTION_POOL_ID)
    run_cmd(command, "ERROR creating JDBC resource")          


def create_topcat_admin(conf_props):
    """
    Set up topcat admin user
    """
    user = conf_props['topcatAdminUser']
    print ("\nCreating TopCAT Admin User '" + user + 
           "'. Please enter new password:")
    command = (ASADMIN + " create-file-user --groups topcatAdmin " + user)
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)
    if retcode > 0:
        abort("ERROR creating user " + user)


def enable_principal_to_role_mng():
    """
    Enable default principal to role manager
    """
    if VERBOSE > 0:
        print "Enable Default Principal to role Manager"
    command = (ASADMIN + " set server-config.security-service.activate" + 
               "-default-principal-to-role-mapping=true")
    run_cmd(command, "ERROR enabling Default Principal to role Manager")          


def install_props_file(conf_props):
    """
    Copy the TOPCAT_PROPS_FILE file
    """
    dest_dir = path.join(conf_props["glassfish"], "glassfish", "domains",
                     conf_props["domain"], "lib", "classes")
    if not path.exists(dest_dir):
        abort("ERROR Cannot find the directory " + dest_dir)
    dest = path.join(dest_dir, TOPCAT_PROPS_FILE)
        
    if path.exists(dest):
        print ("Found existing " + TOPCAT_PROPS_FILE + " in " + str(dest_dir)
               + " new file not copied")
    else:
        if not path.exists(TOPCAT_PROPS_FILE):
            abort("ERROR Cannot find " + TOPCAT_PROPS_FILE + 
                   " in the current directory")
        copyfile(TOPCAT_PROPS_FILE, dest)
        if VERBOSE > 0:
            print "copied " + TOPCAT_PROPS_FILE + " to " + str(dest)


def deploy(conf_props):
    """
    Deploy the TopCAT application and the Admin Console
    """
    # TOPCat
    if VERBOSE > 0:
        print "Deploy the TopCAT application"
    command = (ASADMIN + " deploy --name TopCAT "
               + conf_props['topcatWar'])
    run_cmd(command, "ERROR deploying TopCAT")          
        
    # TOPCatAdmin
    if VERBOSE > 0:
        print "Deploy the TopCATAdmin Console"
    command = (ASADMIN + " deploy --contextroot TopCATAdmin --name TopCATAdmin "
               + conf_props['topcatAdminWar'])
    run_cmd(command, "ERROR deploying TopCATAdmin Console")          


def undeploy(conf_props):
    """
    Undeploy the TopCAT application & the TopCATAdmin Console.
    Delete the database connection pool and resource.
    """
    ret = 0
    # TOPCat
    if VERBOSE > 0:
        print "Undeploy the TopCAT application"
    command = ASADMIN + " undeploy TopCAT"
    ret = ret + run_cmd(command, "ERROR undeploying TopCAT", False)          


    # TOPCatAdmin
    if VERBOSE > 0:
        print "Undeploy the TopCATAdmin Console"
    command = ASADMIN + " undeploy TopCATAdmin"
    ret = ret + run_cmd(command, "ERROR undeploying TopCATAdmin Console",
                        False)

    if VERBOSE > 0:
        print "Delete the database connection pool and resource"

    # jdbc resource
    command = (ASADMIN + " " + 
    "delete-jdbc-resource jdbc/" + CONNECTION_POOL_ID)
    ret = ret + run_cmd(command, "ERROR deleting jdbc resource", False)

    # topcat admin user
    user = conf_props['topcatAdminUser']
    command = (ASADMIN + " delete-file-user " + user)
    ret = ret + run_cmd(command, "ERROR deleting topcat admin user", False)
    
    # jdbc connection pool
    command = (ASADMIN + " " + 
    "delete-jdbc-connection-pool " + CONNECTION_POOL_ID)
    ret = ret + run_cmd(command, "ERROR deleting jdbc connection pool", False)

    # Default Principal to Role Manger
    if VERBOSE > 0:
        print "Disable Default Principal to Role Manger"
    command = (ASADMIN + " set server-config.security-service.activate-" + 
               "default-principal-to-role-mapping=false")
    ret = ret + run_cmd(command,
                        "ERROR disabling Default Principal to role Manger",
                        False)
    if ret > 0:
        exit(1)



PARSER = OptionParser("usage: %prog [options] install | uninstall")
PARSER.add_option("-v", "--verbose", action="count", default=0,
                    help="increase output verbosity")

(OPTIONS, ARGS) = PARSER.parse_args()

if len(ARGS) != 1:
    abort("Must have one argument: 'install' or 'uninstall'")

CMD = ARGS[0].upper()
if CMD not in ["INSTALL", "UNINSTALL"]:
    abort("Must have one argument: 'install' or 'uninstall'")
    
if not path.exists ("setup.py"):
    abort ("This must be run from the unpacked distribution directory")

VERBOSE = OPTIONS.verbose

CONF_PROPS = get_and_validate_props(GLASSFISH_PROPS_FILE, REQ_VALUES_TOPCAT)
CONF_PROPS = add_optional_props(CONF_PROPS)

ASADMIN = path.join(CONF_PROPS["glassfish"], "bin", "asadmin")
# if windows:
#    ASADMIN = ASADMIN + ".bat"
ASADMIN = ASADMIN + " --port " + CONF_PROPS["port"]

IJ = path.join(CONF_PROPS["glassfish"], "javadb", "bin", "ij")
MYSQL = "mysql"

if CMD == "INSTALL":
    create(CONF_PROPS)
    deploy(CONF_PROPS)
else:  # UNINSTALL
    undeploy(CONF_PROPS)


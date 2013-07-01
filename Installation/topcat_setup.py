#!/usr/bin/env python
"""
Set up TopCAT
"""

from subprocess import call
from os import path
from tempfile import TemporaryFile
from shutil import copyfile
from optparse import OptionParser
from sys import exit

# Variables
GLASSFISH_PROPS_FILE = "topcat_glassfish.properties"
TOPCAT_PROPS_FILE = "topcat.properties"

REQ_VALUES_TOPCAT = ["topcatProperties", "driver", "glassfish", "topcatWar",
                     "topcatAdminWar", "topcatAdminUser"]

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
    The get_props function checks if the GLASSFISH_PROPS_FILE file exists and 
    then puts it into a Dictionary
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
    Create the jdbc connection pool and resource, and the topcat admin user and
    enable the principal to role Manager
    """
    if VERBOSE > 0:
        print "Create the database connection pool and resource"
    install_props_file(conf_props)
    if conf_props['dbType'].upper() == "DERBY":
        start_derby()
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
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR creating jdbc connection pool"
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
    print ("\nCreating TopCAT Admin User '" + user + 
           "'. Please enter new password:")
    command = (ASADMIN + " create-file-user --groups topcatAdmin " + user)
    if VERBOSE > 1:
        print command
    retcode = call(command, shell=True)
    if retcode > 0:
        print "ERROR creating user " + user
        exit(1)


def enable_principal_to_role_mng():
    """
    Enable default principal to role manager
    """
    if VERBOSE > 0:
        print "Enable Default Principal to role Manager"
    command = (ASADMIN + " set server-config.security-service.activate" + 
               "-default-principal-to-role-mapping=true")
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR enabling Default Principal to role Manager"
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


def delete(conf_props):
    """
    Delete the database connection pool and resource
    """
    user = conf_props['topcatAdminUser']
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

    command = (ASADMIN + " delete-file-user " + user)
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR deleting topcat admin user"
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
        print "ERROR deleting jdbc connection pool"
        error = True

    if VERBOSE > 0:
        print "Disable Default Principal to role Manger"
    command = (ASADMIN + " set server-config.security-service.activate-" + 
               "default-principal-to-role-mapping=false")
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR disabling Default Principal to role Manger"
        exit(1)

    if error:
        exit(1)


def deploy(conf_props):
    """
    Deploy the TopCAT application and the Admin Console
    """
    # TOPCat
    if VERBOSE > 0:
        print "Deploy the TopCAT application"
    command = (ASADMIN + " deploy --name TopCAT "
               + conf_props['topcatWar'])
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR deploying TopCAT"
        exit(1)
        
    # TOPCatAdmin
    if VERBOSE > 0:
        print "Deploy the TopCATAdmin Console"
    command = (ASADMIN + " deploy --contextroot TopCATAdmin --name TopCATAdmin "
               + conf_props['topcatAdminWar'])
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR deploying TopCATAdmin Console"
        exit(1)


def undeploy():
    """
    Undeploy the TopCAT application & the TopCATAdmin Console
    """
    # TOPCatAdmin
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
        print "ERROR undeploying TopCAT"

    # TOPCatAdmin
    if VERBOSE > 0:
        print "Undeploy the TopCATAdmin Console"
    command = ASADMIN + " undeploy TopCATAdmin"
    if VERBOSE > 1:
        print command
    if VERBOSE > 2:
        retcode = call(command, shell=True)
    else:
        retcode = call(command, shell=True, stdout=TemporaryFile())
    if retcode > 0:
        print "ERROR undeploying TopCATAdmin Console"


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


PARSER = OptionParser()
PARSER.add_option("--install", dest="install",
                  help=("create the jdbc connection pool and resource, " + 
                        "and the topcat admin user and enable the " + 
                        "principal to role manager. Deploy the topcat and " + 
                        "topcat admin applications to Glassfish"),
                  action="store_true")
PARSER.add_option("--uninstall", dest="uninstall",
                  help=("delete the jdbc connection pool and resource, " + 
                        "and the topcat admin user and disable the " + 
                        "principal to role manager. Undeploy the topcat and " + 
                        "topcat admin applications from Glassfish"),
                  action="store_true")
PARSER.add_option("--status", dest="status",
                  help="display status information",
                  action="store_true")
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

if OPTIONS.install:
    create(CONF_PROPS)
    deploy(CONF_PROPS)
elif OPTIONS.uninstall:
    undeploy()
    delete(CONF_PROPS)
elif OPTIONS.status:
    status()
else:
    print ("\nYou must provide an option\n")
    print PARSER.print_help()
    exit(1)

exit(0)

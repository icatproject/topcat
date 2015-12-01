# This is a temporary setup_utils.py for topcat!!
import os
import re
import subprocess
import StringIO
import threading
import shlex
import sys
import shutil
import filecmp
from optparse import OptionParser
import stat
import glob
import platform

def abort(msg):
    """Print to stderr and stop with exit 1"""
    print >> sys.stderr, "\n", msg, "\nSetup is not complete\n"
    sys.exit(1)

def getActions(binDir=False, appDir=False):
    if not os.path.exists ("setup"): abort ("This must be run from the unpacked distribution directory")
    parser = OptionParser("usage: %prog [options] configure | install | uninstall")
    try:
        root = os.getuid() == 0
    except:  # Probably windows
        root = 1

    if binDir:
        if root: default = '/usr/bin'
        else: default = '~/bin'
        parser.add_option("--binDir", "-b", help="location to store executables [" + default + "]", default=default)

    if appDir:
       if root: default = '/usr/share'
       else: default = '~/java'
       parser.add_option("--appDir", "-a", help="location to store java applications [" + default + "]", default=default)

    parser.add_option("--verbose", "-v", help="produce more output - this may appear twice to get even more", action="count")

    options, args = parser.parse_args()

    if len(args) != 1:abort("Must have one argument: 'configure, install' or 'uninstall'")

    arg = args[0].upper()
    if arg not in ["CONFIGURE", "INSTALL", "UNINSTALL"]: abort("Must have one argument: 'configure, install' or 'uninstall'")

    if binDir and not os.path.isdir(os.path.expanduser(options.binDir)): abort("Please create directory " + options.binDir + " or specify --binDir")
    if appDir and not os.path.isdir(os.path.expanduser(options.appDir)): abort("Please create directory " + options.appDir + " or specify --appDir")

    return Actions(options.verbose), options, arg

class Actions(object):

    def __init__(self, verbosity):
        self.verbosity = verbosity
        self.asadminCommand = None
        self.domain = None
        self.config_path = None
        self.lib_path = None
        self.clashes = 0
        self.version = None
        self.domain_path = None

    def configFileExists(self, file):
        return os.path.exists(os.path.join(self.config_path, file))

    def configure(self, file_name, expected, config_file_path=None, dir=None):
        if not config_file_path: config_file_path = self.config_path
        if dir:
            config_file_path = os.path.join(config_file_path, dir)
            local_path = os.path.join(dir)
        else:
            config_file_path = os.path.join(config_file_path, file_name)
            local_path = file_name
        config = os.path.exists(config_file_path)
        if config: config = os.path.getmtime(config_file_path)
        local = os.path.exists(local_path)
        if local: local = os.path.getmtime(local_path)
        if not local:
            if config:
                if dir:
                    shutil.copytree(config_file_path, dir)
                    print "\nCopied directory " + config_file_path + " to " + dir
                    print "Please edit contents of directory ", dir, "to meet your requirements"
                else:
                    shutil.copy(config_file_path, file_name)
                    print "\nCopied " + config_file_path + " to " + file_name
                    print "Please edit", file_name, "to meet your requirements"
            else:
                if dir:
                    shutil.copytree(dir + ".example", dir)
                    print "\nCopied directory " + file_name + ".example" + " to " + dir
                    print "Please edit contents of directory ", dir, "to meet your requirements"
                else:
                    shutil.copy(file_name + ".example", file_name)
                    print "\nCopied " + file_name + ".example" + " to " + file_name
                    print "Please edit", file_name, "to meet your requirements"
            abort("... and then re-run the command")
        if dir:
            props = self.getProperties(os.path.join(dir, file_name), [])
            example = self.getProperties(os.path.join(dir + ".example", file_name), [])
        else:
            props = self.getProperties(file_name, [])
            example = self.getProperties(file_name + ".example", [])
        for key in expected:
            prop = props.get(key)
            if not prop:
                self.clashes += 1
                print "Error: property", key, "is not set in", file_name

        if self.verbosity > 1:
            for key in props.keys():
                if key in example:
                    if props[key] != example[key]: print "\nValue for" , key, "in", file_name, "is", "'" + props[key] + "'", "which differs from example:", "'" + example[key] + "'"
                else:  print "\nValue for" , key, "in", file_name, "is", "'" + props[key] + "'", "is not in example"
            for key in example.keys():
                if key not in props: print "\nValue for" , key, "not in", file_name, "but is in example:", "'" + example[key] + "'"

    def checkNoErrors(self):
        if self.clashes:
            abort("Please edit configuration files and try again as " + str(self.clashes) + " errors were reported.")

    def getGlassfish(self, file_name, required):
        if not os.path.exists(file_name):
            shutil.copy(file_name + ".example", file_name)
            if platform.system() != "Windows": os.chmod(file_name, stat.S_IRUSR | stat.S_IWUSR)
            abort ("\nPlease edit " + file_name + " to meet your requirements then re-run the command")
        if os.stat(file_name).st_mode & stat.S_IROTH:
            if platform.system() == "Windows":
                print "Warning: '" + file_name + "' should not be world readable"
            else:
                os.chmod(file_name, stat.S_IRUSR | stat.S_IWUSR)
                print "'" + file_name + "' mode changed to 0600"
        props = self.getProperties(file_name, required)

        glassfish = props["glassfish"]
        if not os.path.exists(glassfish): abort("glassfish directory " + glassfish + " specified in " + file_name + " does not exist")

        self.asadminCommand = os.path.join(glassfish, "bin", "asadmin") + " --port " + props["port"]

        # Test that domain is running and that password set up
        out, err, rc = self.execute(self.asadminCommand + " get property.administrative.domain.name")
        if rc:
            if err.startswith("Remote server does not listen"): abort('Please use the "asadmin start-domain" command to start your domain')
            if err.startswith("Authentication failed for user: null"): abort ('Please use the "asadmin login" command and accept the default user name to be able to access your domain')
            abort(err)


        self.domain = self.getAsadminProperty("property.administrative.domain.name")

        domain_path = os.path.join(glassfish, "glassfish", "domains", self.domain)
        self.domain_path = domain_path
        if not os.path.exists(domain_path): abort("Domain directory " + domain_path + " does not exist")
        self.config_path = os.path.join(domain_path, "config")
        if not os.path.exists(self.config_path): abort("Domain's config directory " + self.config_path + " does not exist")
        self.lib_path = os.path.join(domain_path, "lib", "applibs")
        if not os.path.exists(self.lib_path): abort("Domain's lib directory " + self.lib_path + " does not exist")

        cmd = self.asadminCommand + " version"
        out, err, rc = self.execute(cmd)
        if rc: abort(err)
        vline = out.splitlines()[0]
        pos = vline.find("(")
        self.version = int(vline[:pos].split()[-1].split(".")[0])
        if self.verbosity: print "You are using Glassfish version", self.version

        return props

    def deleteFileRealmUser(self, username):
        self.asadmin("delete-file-user " + username, tolerant=True)

    def stopDomain(self):
        cmd = self.asadminCommand + " stop-domain " + self.domain
        if self.verbosity: print "\nexecute: " + cmd
        out, err, rc = self.execute(cmd)
        if rc:
            print cmd, " ->" + err
            out, err, rc = self.execute("jps")
            if rc:
                abort(err)
            for line in out.splitlines():
                line = line.strip().split()
                if line[1] == "ASMain":
                    cmd = "kill -9 " + line[0]
                    if self.verbosity: print "\nexecute: " + cmd
                    self.execute(cmd)

    def startDomain(self):
        self.asadmin("start-domain " + self.domain)

    def installToApplibs(self, jar):
        files = glob.glob(jar)
        if len(files) != 1: abort("Exactly one file must match " + jar)
        shutil.copy(files[0] , self.lib_path)
        if self.verbosity:
            print "\n", files[0], "copied to", self.lib_path

    def removeFromApplibs(self, jar):
        dest = os.path.join(self.lib_path, jar)
        files = glob.glob(dest)
        if len(files) > 1: abort("Exactly one file must match " + dest)
        if len(files) == 1:
            os.remove(files[0])
            if self.verbosity:
                print "\n", os.path.basename(files[0]), "removed from", self.lib_path

    def getJDBCProps(self, driver):
        result = "--datasourceclassname " + driver
        if driver.startswith("oracle"):
            result += " --validateatmostonceperiod=60 --validationtable=dual --creationretryattempts=10 --isconnectvalidatereq=true"
        result += " --restype javax.sql.DataSource --failconnection=true --steadypoolsize 2"
        result += " --maxpoolsize 32 --ping"
        return " " + result + " "

    def addFileRealmUser(self, username, password, group):
        if self.getAsadminProperty("configs.config.server-config.security-service.activate-default-principal-to-role-mapping") == "false":
            self.setAsadminProperty("configs.config.server-config.security-service.activate-default-principal-to-role-mapping", "true")
            self.stopDomain()
            self.asadmin("start-domain " + self.domain)

        digit = False
        lc = False
        uc = False
        for c in password:
            if c.isdigit(): digit = True
            elif c.islower(): lc = True
            elif c.isupper(): uc = True
        if not (digit and lc and uc) : abort("password must contain at least one digit, a lower case character and an upper case character")

        f = open("pw", "w")
        print >> f, "AS_ADMIN_USERPASSWORD=" + password
        f.close()
        self.asadmin("--passwordfile pw create-file-user --groups " + group + " " + username)
        os.remove("pw")

    def deploy(self, file, contextroot=None, deploymentorder=100, libraries=[]):
        files = glob.glob(file)
        if len(files) != 1: abort("Exactly one file must match " + file)
        cmd = self.asadminCommand + " " + "deploy"
        if self.version >= 4:
            cmd = cmd + " --deploymentorder " + str(deploymentorder)
        if contextroot:
            cmd = cmd + " --contextroot " + contextroot
        if libraries:
            libstring = ""
            for library in libraries:
                path = os.path.join(self.lib_path, library)
                libs = glob.glob(path)
                if len(libs) != 1: abort("Exactly one library must match " + path)
                libadd = os.path.basename(libs[0])
                if libstring:
                    libstring += "," + libadd
                else:
                    libstring = "--libraries " + libadd
            cmd = cmd + " " + libstring
        cmd = cmd + " " + files[0]
        if self.verbosity: print "\nexecute: " + cmd
        out, err, rc = self.execute(cmd)
        if self.verbosity > 1:
            if out: print out
        if err:
            for line in err.splitlines():
                line = line.strip()
                if line:
                    if line.startswith("PER01"): continue
                    print line

    def getProperties(self, fileName, needed):
        """Read properties files and check that the properties in the needed list are present"""

        if not os.path.exists(fileName):
            abort (fileName + " file not found - please run './setup configure'")

        p = re.compile(r"")
        f = open(fileName)
        props = {}
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and not line.startswith("!"):
                nfirst = len(line)
                for sep in [r"\s*=\s*", r"\s*:\s*", r"\s+"]:
                    match = re.search(sep, line)
                    if match and match.start() < nfirst:
                        nfirst = match.start()
                        nlast = match.end()
                if nfirst == len(line):
                    key = line
                    value = ""
                else:
                    key = line[:nfirst]
                    value = line[nlast:]
                props[key] = value
        f.close()

        for item in needed:
            if (item not in props):
                abort(item + " must be specified in " + fileName)

        return props

    def execute(self, cmd):

        if platform.system() == "Windows":
            cmd = cmd.split()
            proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        else:
            cmd = shlex.split(cmd)
            proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stringOut = StringIO.StringIO()

        mstdout = Tee(proc.stdout, stringOut)
        mstdout.start()
        stringErr = StringIO.StringIO()
        mstderr = Tee(proc.stderr, stringErr)
        mstderr.start()
        rc = proc.wait()

        mstdout.join()
        mstderr.join()

        out = stringOut.getvalue().strip()
        stringOut.close()

        err = stringErr.getvalue().strip()
        stringErr.close()

        return out, err, rc

    def isInstalled(self):
        return 'topcat' in self.execute(self.asadminCommand + " list-applications")[0]


    def asadmin(self, command, tolerant=False, printOutput=False):
        cmd = self.asadminCommand + " " + command
        if self.verbosity: print "\nexecute: " + cmd
        out, err, rc = self.execute(cmd)
        if self.verbosity > 1 or printOutput:
            if out: print out
            if err: print err

        if not tolerant and rc:
            if not self.verbosity: print cmd, " ->"
            abort(err)

    def installFile(self, file, dir=None):
        if not dir: dir = self.config_path
        if not os.path.isdir(dir): abort ("Please create directory " + dir + " to install " + file)
        if not os.path.exists(file): abort (file + " not found")
        dest = os.path.join(dir, file)
        if os.path.exists(dest):
            diff = not filecmp.cmp(file, dest)
            if diff:
                if os.path.getmtime(file) > os.path.getmtime(dest):
                    shutil.copy(file , dir)
                    print "\n", dest, "has been overwritten"
                else:
                   abort(dest + " is newer than " + file)
        else:
            shutil.copy(file , dir)
            if self.verbosity:
                print "\n", file, "copied to", dir

    def removeFile(self, file, dir=None):
        if not dir: dir = self.config_path
        dest = os.path.join(dir, file)
        if os.path.exists(dest):
            os.remove(dest)
            if self.verbosity:
                print "\n", file, "removed from", dir


    def installToAppWeb(self, file, app, dir=None):
        appName = self.getAppName(app)
        webDir = os.path.join(self.domain_path, "applications", appName, dir)
        if not os.path.isdir(webDir): abort ("Please create directory " + webDir + " to install " + file)
        if not os.path.exists(file): abort (file + " not found")

        shutil.copy(file , webDir)
        if self.verbosity:
            print "\n", file, "copied to", webDir

    def installDir(self, file, dir=None):
        if not dir: dir = self.config_path
        if not os.path.isdir(dir): abort ("Please create directory " + dir + " to install " + file)
        if not os.path.exists(file): abort (file + " not found")
        if not os.path.isdir(file): abort (file + " is not a directory")
        dest = os.path.join(dir, file)
        if os.path.exists(dest):
            if (os.path.getmtime(file) - os.path.getmtime(dest)) > -.001:  # Directory times from python are odd
                shutil.rmtree(dest)
                shutil.copytree(file , dest)
                print "\n", dest, "has been overwritten"
            else:
                print os.path.getmtime(file) - os.path.getmtime(dest)
                abort("Directory " + dest + " is newer than " + file)
        else:
            shutil.copytree(file , dest)
            if self.verbosity:
                print "\n", file, "copied to", dir

    def removeDir(self, file, dir=None):
        if not dir: dir = self.config_path
        dest = os.path.join(dir, file)
        if os.path.exists(dest):
            shutil.rmtree(dest)
            if self.verbosity:
                print "\n", directory, "removed from", dir

    def getAppName(self, app):
        cmd = self.asadminCommand + " " + "list-applications"
        out, err, rc = self.execute(cmd)
        if rc: abort(err)
        for line in out.splitlines():
            if (line.startswith(app + "-")):
                return line.split()[0]

    def getAsadminProperty(self, name):
        cmd = self.asadminCommand + " get " + name
        if self.verbosity: print "\nexecute: " + cmd
        out, err, rc = self.execute(cmd)
        if rc: abort(err)
        return out.splitlines()[0].split("=")[1]

    def setAsadminProperty(self, name, value):
        cmd = self.asadminCommand + " set " + name + "=" + value
        if self.verbosity: print "\nexecute: " + cmd
        out, err, rc = self.execute(cmd)
        if rc: abort(err)

class Tee(threading.Thread):

    def __init__(self, inst, *out):
        threading.Thread.__init__(self)
        self.inst = inst
        self.out = out

    def run(self):
        while 1:
            line = self.inst.readline()
            if not line: break
            for out in self.out:
                out.write(line)

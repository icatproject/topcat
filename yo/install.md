#TopCAT Installation Guide

##Install

  1. Install glassfish 4.0 following the guide at http://icatproject.org/installation/glassfish/
  2. Extract the topcat distro zip file and change directory to the unpacked distribution
  3. Rename all the *.example files by removing the .example extensions. The directory must have 5 files named:
    - topcat-setup.properties
    - topcat.properties
    - topcat.json
    - lang.json
    - topcat.css
  4. Configure each of the 5 files as required. They should be self explanatory. For configuration of topcat.properties, please see the [topcat configuration wiki](https://github.com/icatproject/topcat/wiki/TopCAT-Configuration).
  5. Change permission of the properties file to 0600
    `chmod 0600 *.properties`
  6. Make setup script executable:
    `chmod +x setup`
  7. Run configure
    `./setup configure`
  8. Run install
    `./setup install`

##Uninstall
  1. Run uninstall
    `./setup uninstall`
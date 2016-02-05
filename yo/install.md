#TopCAT Installation Guide

## Compatibility

TopCAT requires icat.server 4.6 and ids.server 1.5.0

##Prerequisites

  - The TopCAT distribution: topcat-2.0.0-distro.zip
  - A suitable deployed container (here assumed to be glassfish) to support a web application. Testing has been carried out with Glassfish 4.0. [Glassfish installation instructions](http://icatproject.org/installation/glassfish/) are available.
  -  A database as described in [Database installation instructions]http://icatproject.org/installation/database/) installed on the server.
  - Python (version 2.4 to 2.7) installed on the server.

##The topcat-setup.properties file

See topcat-setup.properties.example file in the distro and use it as your base for configuration.

  - **driver** - is the name of the jdbc driver and must match the jar file for your database that you stored in the previous step.
  - **dbProperties** - identifies the icat database and how to connect to it.
  - **glassfish** - is the path to the top level of the glassfish installation. It must contain "glassfish/domains", and will be referred to here as GLASSFISH_HOME as if an environment variable had been set.
  - **port** - is the administration port of the chosen glassfish domain which is typically 4848.
  - **topcatUrlRoot** - is the context path where topcat will be deployed (e.g /topcat). Use / for root context.
  - **mail.host** - is the smtp host address
  - **mail.user** - is the name of mail acount user when connecting to the mail server
  - **mail.from** - is the mail from address
  - **mail.property** - is the javamail properties. See https://javamail.java.net/nonav/docs/api/ for list of properties
  - **adminUsername** - The basic authentication user name for the admin REST API
  - **adminPassword** - The basic authentication password for the admin REST API



##The topcat.properties file

See topcat.properties.example file in the distro and use it as your base for configuration.

  - **file.directory** - is the directory path for temporary prepared files
  - **mail.enable** - whether to enable mailing
  - **mail.subject** - the subject of the  the email. The following tokens are available:
      - **${userName}** - user username
      - **${email}** - user email
      - **${facilityName}** - the facility key name
      - **${preparedId}** - the prepared Id of the download request
      - **${fileName}** - the download name
      - **${downloadUrl}** - the download url
  - **mail.body.https** - is the email body message for https downloads. All subject tokens as above are available.
  - **mail.body.globus** - is the email body message for https downloads. All subject tokens as above are available.
  - **mail.body.smartclient** - is the email body message for smartclient downloads. All subject tokens as above are available.


##The topcat.json file

See topcat.json.example file in the distro and use it as your base for configuration.

Please see the [TopCAT Configuration Guide in the wiki](https://github.com/icatproject/topcat/wiki/TopCAT-Configuration-Guide)


##The lang.json file

See lang.json.example file in the distro and use it as your base for configuration.

Please see the "Teaching your app a language" section from the [angular-translate guide ](https://angular-translate.github.io/docs/#/guide/02_getting-started) for more details.


##The topcat.css file

This is an empty file to allow you to customise your TopCAT site.



##Install

  1. Extract the topcat distro zip file and change directory to the unpacked distribution
  2. Rename all the *.example files by removing the .example extensions. The directory must have 5 files named:
    - topcat-setup.properties
    - topcat.properties
    - topcat.json
    - lang.json
    - topcat.css
  3. Configure each of the 5 files as required. See above.
  4. Change permission of the properties files to 0600
    `chmod 0600 *.properties`
  5. Make setup script executable:
    `chmod +x setup`
  6. Run configure
    `./setup configure`
  7. Run install
    `./setup install`

##Uninstall
  1. Run uninstall
    `./setup uninstall`

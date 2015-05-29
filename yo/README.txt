## Topcat v2 Notes

The angularJS development stack used to develop Topcatv2 is Yeoman http://yeoman.io/

In addition to AngularJS, the following modules were used:

  - _angular-bootstrap_: Provides native AngularJS directives for Bootstrap components (http://angular-ui.github.io/bootstrap/)
  - _angular-ui-router_: Provides routing with nested views. Necessary for tabs with routing (bookmarkable tabs) (https://github.com/angular-ui/ui-router)
  - _angular-ui-router-tab_: Provides tabs directive with routing support (https://github.com/rpocklin/ui-router-tabs)
  - _angular-ui-grid_: Provides functionality to display data in a grid (http://ui-grid.info)
  - _angular-deferred-bootstrap_: Used for initialising AngularJS app with constants (https://github.com/philippd/angular-deferred-bootstrap)
  - _angularjs-truncate_: Used to truncate strings
  - _ui-router-extras_: Provides sticky state essential for navigating between tabs with routing and not resetting states
  - _lodash_: JavaScript utility library
  - _angular-pretty-bytes_: filter to display human readable bytes, KB, MB, GB etc
  - _ngstorage_: No need to serialize and unserialize data to and from localstorage and sessionstorage
  - _squel_: Used to build SQL like queries
  - _angular-translate_: Primarily used to move UI text to a JSON file
  - _angular-translate-loader-static-files_: angular-translate plugin to load static files
  - _karma-read-json_: Use to load json files in tests


## Installation (On windows) for AngularJS Development

Install Git

  - When installing git for windows, select the "Run Git from the Windows Command Prompt" or "Run Git and included uni tools from the Windows Command Prompt"
  - Setting git proxy:
        git config --global http.proxy http://proxy.server.com:8080


Install Python 2.7.X

Install node.js

Install Visual Studio Express 2013 with Update 4 for Windows Desktop (https://www.visualstudio.com/en-us/downloads/download-visual-studio-vs.aspx) Reference: https://github.com/TooTallNate/node-gyp#installation

Checkout maven project from SCM from the location http://topcat.googlecode.com/svn/branches/topcatv2

Run from the command line:

    cd [PROJECT_PATH]/yo
    npm install
    bower install
    grunt serve


Note: Visual Studio Express (VCBUild.exe) is used by the module node-gyp to compile native addons. On windows, if you run "npm install", you will probably  get an error complaining about MSBuild. I think the node-gyp module is used to build the Socket.io-client module used by karma. If you don't use websocket for testing, you may not need it and just ignore the error. Installing Visual Studio Express 2013 with Update 4 for Windows Desktop (Must be the desktop version) did fix the issue for me.


Proxy setting note: npm does not use environment variables for proxy. Instead use the following commands

    npm config set proxy http://proxy.company.com:8080
    npm config set https-proxy http://proxy.company.com:8080


## SSL Certificates

For development, self-signed certificates are used. If valid ssl certicates are required then name them as follow and copy them to the certs directory and then set the environment variable NODE_ENV as 'production':

    ssl private key: key.crt
    ssl root certificate: root.crt
    ssl intermediate cetificate: intermediate.crt

The real certs will be loaded automatically.

If you do want to have more control of the file path and name, you have to edit Gruntfile.js directly.



## Using Live Data

As the ICAT REST API (<4.4.0) is not yet CORS enabled and not yet finalised, we need a proxy service to get data from an ICAT server when using XMLHttpRequest.

The ICAT Data Proxy is used for this purpose. ICAT Data Proxy is available at http://topcat.googlecode.com/svn/branches/topcatv2/icatdataproxy/. Checkout the project using:

    svn checkout http://topcat.googlecode.com/svn/branches/topcatv2/icatdataproxy/ icatdataproxy

Read readme.md file to install and start the service.

You must start the service for TopCAT to load live data.


## AngularJS Development style guide

https://github.com/johnpapa/angular-styleguide#modules



## Development tools

Using sublime text 3 as text editor with the following plugins installed:
    - Package Control (Manage sublime plugins)
    - AngularJS (AngularJS autocomplete)
    - DocBlockr (add doc blocks)
    - HTMLBeautify (Format/Beautify HTML)
    - JsFormats (Format/Beautify javascript)
    - SidebarEnhancements (Add file management to the side file list)
    - SublimeLinter-jshint (On the fly jshint validator MUST HAVE!!!!)

## Development Build

Before committing code, make sure you perform a "grunt build" and test the build using "grunt serve:dist". This is to ensure the application still works after minification as the build is the one that will be deployed!!!!


## RESTFUL API (TEST ONLY/ DEPRECATED)

Included in the project is a RESTFul API developed using jersey.

The pom.xml includes the yeoman-maven-plugin. This builds the angularJS project to the yo/dist directory and is then copied the target directory to be include into the war.

To pass test, copy the src/test/resources/icatserver.properties.example to src/test/resources/icatserver.properties and edit your ICAT server configuration and a valid user


## Example RESTFUL API urls

The icatSessionId key/value pair must be passed in the header for each request

icatSessionId   46df7c6b-fba3-4b07-9fd6-cdcd41bae37


To get an icatSessionId post a request to http://localhost:8080/topcat/webapi/v1/login
with the following key/value using application/x-www-form-urlencoded:

serverName : ISIS
username : YOUR_FEDERAL_ID
password : YOUR_PASSWORD
authenticationType : ldap


List all facilities of a icat server
http://localhost:8080/topcat/webapi/v1/servers/ISIS/facilities

Get a facility by id
http://localhost:8080/topcat/webapi/v1/servers/ISIS/facilities/1

List all investigations for a particular instrument
http://localhost:8080/topcat/webapi/v1/servers/ISIS/instruments/1/investigations

List 1st page of investigations for a particular instrument
http://localhost:8080/topcat/webapi/v1/servers/ISIS/instruments/1/investigations

List 1st page of investigation for a particular instrument sort by title in descending order
http://localhost:8080/topcat/webapi/v1/servers/ISIS/instruments/1/investigations/1?sort=title&order=desc

List the instruments of a facility by id
http://localhost:8080/topcat/webapi/v1/servers/ISIS/facilities/1/instruments

List the datasets for an investigation
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787/datasets

List the 1st page datasets for an investigation
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787/datasets/1

List the 1st page datasets for an investigation sort by name in descending order
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787/datasets/1?sort=name&order=desc

Get an investigation by id
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787



## Bower Command Reference

search for a package

    bower search <package>

install a particular package

    bower install <package>

install a particular package version

    bower install <package>#<version>

install the packages specified in bower.json

    bower install

list installed packages

    bower list

creates a bower.json file for your project

    bower init

update packages

    bower update

update a particular package

    bower update <package>

uninstall a particular package

    bower uninstall <package>


## Grunt commands

Start up web service

    grunt serve



## Structure

Possible combinations of facility, cycle, instrument, investigation, dataset, datafile

facility > cycle
facility > cycle > investigation
facility > instrument
facility > instrument > cycle
facility > instrument > cycle > investigation
facility > investigation
facility > investigation > dataset
facility > investigation > datafile
facility > dataset
facility > dataset > datafile
facility > datafile

## Route/State urls

### Browsing

list the facilities

    /#/browse/facilities/

list the facility cycles for a facility

    /#/browse/facilities/{facility.id}/cycles

list the investigations for a facility cycle of a facility

    /#/browse/facilities/{facility.id}/cycles/{facilityCycle.id}/investigations

list the instruments for a facility

    /#/browse/facilities/{facility.id}/instruments

list the facility cycles for an instrument

    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/cycles

list the investigations of a facility cycles for an instrument

    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/cycles/{facilityCycle.id}/investigations

list the investigation for an instrument

    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/investigations

list the investigations for a facility

    /#/browse/facilities/{facility.id}/investigations

list the datasets for an investigation

    /#/browse/facilities/{facility.id}/investigations/{investigation.id}/datasets

list the datasets for a facility

    /#/browse/facilities/{facility.id}/datasets

list the datafiles for an investigation

    /#/browse/facilities/{facility.id}/investigations/{investigation.id}/datafiles

list the datafiles for a dataset

    /#/browse/facilities/{facility.id}/datasets/{dataset.id}/datafiles

list the datafiles for a facility

    /#/browse/facilities/{facility.id}/datafiles

### Searching

    /#/search?query&type&facility&startDate&endDate&parameters[]&samples[]


### Cart

    /#/cart


## Routing possibilities

"structure" : ["facility", "instrument", "cycle", "investigation", "dataset", "datafile"],
facility-instrument
instrument-cycle
cycle-investigation
investigation-dataset
dataset-datafile
datafile

"structure" : ["facility", "instrument", "investigation", "dataset", "datafile"],
facility-instrument
instrument-investigation
investigation-dataset
dataset-datafile
datafile

"structure" : ["facility", "instrument", "dataset", "datafile"],
facility-instrument
instrument-dataset
dataset-datafile
datafile

"structure" : ["facility", "instrument", "datafile"],
facility-instrument
instrument-datafile
datafile

"structure" : ["facility", "cycle", "instrument", "investigation", "dataset", "datafile"],
facility-cycle
cycle-instrument
instrument-investigation
investigation-dataset
dataset-datafile
datafile


"structure" : ["facility", "cycle", "investigation", "dataset", "datafile"],
facility-cycle
cycle-investigation
investigation-dataset
dataset-datafile
datafile


"structure" : ["facility", "cycle", "investigation", "datafile"],
facility-cycle
cycle-investigation
investigation-datafile
datafile


"structure" : ["facility", "cycle", "datafile"],
facility-cycle
cycle-datafile
datafile


"structure" : ["facility", "cycle", "dataset", "datafile"],
facility-cycle
cycle-dataset
dataset-datafile
datafile


"structure" : ["facility", "investigation", "dataset", "datafile"],
facility-investigation
investigation-dataset
dataset-datafile
datafile

"structure" : ["facility", "dataset", "datafile"],
facility-dataset
dataset-datafile
datafile


"structure" : ["facility", "datafile"],
facility-datafile
datafile


## Grouping url by number of parameters

    1 param
    /#/cart
    /#/about
    /#/contact


    2 params
    /#/browse/facilities/

    3 params:


    4 params:

    /#/browse/facilities/{facility.id}/cycles
    /#/browse/facilities/{facility.id}/instruments
    /#/browse/facilities/{facility.id}/investigations
    /#/browse/facilities/{facility.id}/datasets
    /#/browse/facilities/{facility.id}/datafiles


    5 params:


    6 params:

    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/cycles
    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/investigations
    /#/browse/facilities/{facility.id}/cycles/{facilityCycle.id}/investigations
    /#/browse/facilities/{facility.id}/investigations/{investigation.id}/datasets
    /#/browse/facilities/{facility.id}/investigations/{investigation.id}/datafiles
    /#/browse/facilities/{facility.id}/datasets/{dataset.id}/datafiles


    7 params:


    8 params:

    /#/browse/facilities/{facility.id}/instruments/{intrument.id}/cycles/{facilityCycle.id}/investigations




## Options to interpret "my data"

  1. via url. Example:

      /#/browse/facilities/{facility.id}/investigations
      /#/browse/facilities/{facility.id}/mydata/investigations

    The "mydata" parameter has to be maintained throughout the url of the site when the
    "my data" button is toggled.

  2. via query parameter. Example:

      /#/browse/facilities/{facility.id}/investigations
      /#/browse/facilities/{facility.id}/investigations?mydata=true

    The "mydata" query parameter has to be maintained throughout the url of the site when the
    "my data" button is toggled

  3. via cookie. This means users will not be able to directly access a "my data" browse page.
    They will go to the page and then toggle on the "my data" button.


## JSHINT

To suppress an errors, look up the code in https://github.com/jshint/jshint/blob/2.1.4/src/shared/messages.js.

Add the comment /*jshint -CODE_NUMBER */ just above the line of code to suppress the warning.



## UI-GRID

sort direction:

sort: {
  direction: uiGridConstants.ASC,
  priority: 0,
},


Apply a class to a cell:

{ field: 'name', cellClass:'red' },
{ field: 'company',
  cellClass: function(grid, row, col, rowRenderIndex, colRenderIndex) {
    if (grid.getCellValue(row,col) === 'Velity') {
      return 'blue';
    }
  }
}


filter constants:

filter: {
  STARTS_WITH: 2,
  ENDS_WITH: 4,
  EXACT: 8,
  CONTAINS: 16,
  GREATER_THAN: 32,
  GREATER_THAN_OR_EQUAL: 64,
  LESS_THAN: 128,
  LESS_THAN_OR_EQUAL: 256,
  NOT_EQUAL: 512,
  SELECT: 'select',
  INPUT: 'input'
},


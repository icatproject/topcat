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
  - _angular-spinner_: Display spinners on $http calls



## Installation (On windows) for AngularJS Development

Install Git

  - When installing git for windows, select the "Run Git from the Windows Command Prompt" or "Run Git and included uni tools from the Windows Command Prompt"
  - Setting git proxy:
        git config --global http.proxy http://proxy.server.com:8080


Install Python 2.7.X

Install node.js

Install Visual Studio Express 2013 with Update 4 for Windows Desktop (https://www.visualstudio.com/en-us/downloads/download-visual-studio-vs.aspx) Reference: https://github.com/TooTallNate/node-gyp#installation

Clone the project from githut https://github.com/icatproject/topcat

    git clone https://github.com/icatproject/topcat.git
    git checkout -b topcatv2

Run from the command line:

    cd [PROJECT_PATH]/icatdataproxy
    npm install
    npm start

Open another command prompt:

    cd [PROJECT_PATH]/yo
    npm install
    bower install
    grunt serve


Note: Visual Studio Express (VCBUild.exe) is used by the module node-gyp to compile native addons. On windows, if you run "npm install", you will probably  get an error complaining about MSBuild. I think the node-gyp module is used to build the Socket.io-client module used by karma. If you don't use websocket for testing, you may not need it and just ignore the error. Installing Visual Studio Express 2013 with Update 4 for Windows Desktop (Must be the desktop version) did fix the issue for me.

To build topcat and minify the project:

  grunt build

To server the build with grunt:

  grunt serve:dist


Proxy setting note: npm does not use environment variables for proxy. Instead use the following commands:

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

As the ICAT REST API (< 4.4.0) and IDS server (< 1.3.1) are not yet CORS enabled, we need a proxy service to get data from an ICAT server when using XMLHttpRequest.

The ICAT Data Proxy is used for this purpose and can be found in the icatdataproxy directory.

The IDS API is only partially supported (getSize and getStatus only).

Read readme.md file to install and start the service.

You must start the service for TopCAT to retrieve data from ICAT server.


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


## ~~RESTFUL API (DEPRECATED)~~

Included in the project is a RESTFul API developed using jersey.

The pom.xml includes the yeoman-maven-plugin. This builds the angularJS project to the yo/dist directory and is then copied the target directory to be include into the war.


## UI-GRID Notes

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
}



## IDS

### RESTAPI

Url: /ids/getLink
Method: POST
Media Type: application/x-www-form-urlencoded
Content-Type: text/plain
Data:
  Form Param:
    sessionId String
    datafileId String
    username String
Returns:
  download url as a string


Url: /ids/archive
Method: POST
Media Type: application/x-www-form-urlencoded
Content-Type: Depends on sent accept header
Data:
  Form Param:
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String

Url: /ids/delete
Method: DELETE
Media Type: application/x-www-form-urlencoded
Content-Type:
Data:
  Query Param:
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String

Url: /ids/isReadOnly
Method: GET
Content-Type: text/plain

Url: /ids/isTwoLevel
Method: GET
Content-Type: text/plain

Url: /ids/getApiVersion
Method: GET
Content-Type: text/plain


Url: /ids/getSize
Method: GET
Content-Type: text/plain
Data:
  Query Param:
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String
Returns:
  Size as a string in double quotes

Url: /ids/getData
Method: GET
Content-Type: text/plain
Data:
  Query Param:
    preparedId String
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String
    compress Boolean
    zip Boolean
    outname String
  Header Param:
    range String
Returns:



Url: /ids/getStatus
Method: GET
Content-Type: text/plain
Data:
  Query Param:
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String
Returns:
  ONLINE
  RESTORING
  ARCHIVED



Url: /ids/isPrepared
Method: GET
Content-Type: text/plain
Data:
  Query Param:
    preparedId String


Url: /ids/getServiceStatus
Method: GET
Content-Type: appplication/json
Data:
  Query Param:
    preparedId String

Url: /ids/ping
Method: GET
Content-Type: text/plain
Data:
  Query Param:
    preparedId String


Url: /ids/prepareData
Method: POST
Media Type: application/x-www-form-urlencoded
Content-Type: text/plain
Data:
  Form Param:
    preparedId String
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String
    compress Boolean
    zip Boolean


Url: /ids/put
Method: PUT
Media Type: application/octet-stream
Content-Type: text/plain
Data:
  File
  Query Param:
    sessionId String
    name String
    datafileFormatId String
    datasetId String
    description String
    doi String
    datafileCreateTime Integer
    datafileModTime Integer


Url: /ids/put
Method: POST
Media Type: multipart/form-data
Content-Type: text/plain
Data:
  Form Param:


Url: /ids/restore
Method: POST
Media Type: application/x-www-form-urlencoded
Data:
  Form Param:
    sessionId String
    investigationIds String
    datasetIds String
    datafileIds String














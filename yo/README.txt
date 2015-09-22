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
  - _angular-translate_: Used to move UI text to a JSON file
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


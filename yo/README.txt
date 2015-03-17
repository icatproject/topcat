#### Topcat v2 Notes ####

The angularJS development stack used to develop Topcatv2 is Yeoman http://yeoman.io/

In addition to AngularJS, the following modules were used:

    * angular-bootstrap: Provides native AngularJS directives for Bootstrap components (http://angular-ui.github.io/bootstrap/)
    * angular-ui-router: Provides routing with nested views. Necessary for tabs with routing (bookmarkable tabs) (https://github.com/angular-ui/ui-router)
    * angular-ui-router-tab: Provides tabs directive with routing support (https://github.com/rpocklin/ui-router-tabs)
    * angular-datatables: Provides functionality to display data in grid table  (http://l-lin.github.io/angular-datatables)
    * angular-deferred-bootstrap: Used for initialising AngularJS app with constants (https://github.com/philippd/angular-deferred-bootstrap)


== Installation (On windows) for AngularJS Development ==

Install Git

Install Python 2.7.X

Install node.js

Install Visual Studio Express 2013 with Update 4 for Windows Desktop (https://www.visualstudio.com/en-us/downloads/download-visual-studio-vs.aspx) Reference: https://github.com/TooTallNate/node-gyp#installation

Checkout maven project from SCM from the location http://topcat.googlecode.com/svn/branches/topcatv2

Run from the command line:

    cd [PROJECT_PATH]/yo
    npm install
    bower install
    grunt serve


== RESTFUL API ==

Included in the project is a RESTFul API developed using jersey.

The pom.xml includes the yeoman-maven-plugin. This builds the angularJS project to the yo/dist directory and
is then copied the target directory to be include into the war.

To pass test, copy the src/test/resources/icatserver.properties.example to src/test/resources/icatserver.properties
and edit your ICAT server configuration and a valid user



== Example RESTFUL API urls ==

The icatSessionId key/value pair must be passed in the header for each request

icatSessionId   46df7c6b-fba3-4b07-9fd6-cdcd41bae37


To get an icatSessionId send a request to http://localhost:8080/topcat/webapi/v1/login
with the following key/value headers:

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

List the 1st page datasets for an investigation sort by name and
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787/datasets/1?sort=name&order=desc

Get an investigation by id
http://localhost:8080/topcat/webapi/v1/servers/ISIS/investigations/24089787



== Bower Command Reference ==

#search for a package
bower search <package>

#install a particular package
bower install <package>

#install a particular package version
bower install <package>#<version>

#install the packages specified in bower.json
bower install

#list installed packages
bower list

#creates a bower.json file for your project
bower init

#update packages
bower update

#update a particular package
bower update <package>

#uninstall a particular package
bower uninstall <package>


== Grunt commands ==

#Start up web service
grunt serve



== Structure ==

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

=== Route/State urls ===

== Browsing ==

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


== Searching ==

/#/search?query&type&facility&startDate&endDate&parameters[]&samples[]


/#/cart



=== Options to interpret "my data" ===

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










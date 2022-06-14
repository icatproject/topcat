# Release Notes

## 2.4.9 (14 June 2022)

  * Add ability to remove items from cart via a POST request to /cartItems (PR #483)

## 2.4.8 (26th Mar 2021)

  * Carts made by anonymous users are now separate between different sessions (PR #473)
  * Added JSON-B annotations to allow TopCAT to be deployed on Payara 5 (PR #475)

## 2.4.7 (9th Dec 2019)

  * StatusCheck only expires downloads on TopcatExceptions, but will obey poll.interval.wait on other exceptions (issue #462)

## 2.4.6 (30th Oct 2019)

  * Ensure download cart button is disabled when limits are exceeded (issue #459)
  * Inform user when cart submission for download fails (issue #458)

## 2.4.5 (15th Oct 2019)

  * Improve feedback when getSize requests fail (issue #439)
  * Fix Sample inclusions in metapanel queries (issue #441)
  * Allow automatic (anonymous) login for DOI redirect requests (issue #443)
  * StatusCheck now logs changes in download statuses
  * StatusCheck now expires downloads on any exception, not just TopcatException (issue #449)
  * Allow HTML markup in download type descriptions (issue #450)
  * Fix: always order query results by (at least) ID to prevent pagination issues (issue #453)
  
## 2.4.4 (1st Jul 2019)

  * Add optional preamble HTML to login page, defined in lang.json (issue #415)
  * Add showAsButton to authenticator configuration (in topcat.json) to give an authenticator its own login button (issue #418)
  * Add extraLoginButtons to facility configuration (in topcat.json) (issue #419)
  * Remove DOI and some login redirections from browser history (issue #424)
  * Allow ids.timeout to be in seconds or minutes as well as milliseconds (issue #349)
  * Add optional displayName and description to downloadTransportTypes (issue #427)
  * Add API to disable/enable download transport types (issue #434)
  * Add topcat_admin script to the distribution (issue #437)
  
## 2.4.3 (25th Mar 2019)

  * update component versions for Vagrant and Travis
  * Update to reflect schema changes in icat.server 4.10 (issue #404)
  * Make study variable name available to Investigation in topcat.json (issue #403)
  * Fix problem with translation strings in metatabs (issue #407)
  * Stop showing loading spinner on cells that are always empty (issue #410)

## 2.4.2 (31st Jan 2019)

* improve DOI redirection failure message (issue #388)
* mail configuration properties can be removed if not required (issue #389)
* configurable control of caching behaviour for investigations (issue #394)
* cache performance and low-priority queue behaviour can now be monitored from the browser console
* fix issue #398 : split long getSize URLs into multiple requests
* add REST call to clear cached size for an entity (issue #401)
* **database migration required** (simplest: drop the Cache table)

## 2.4.1

* getSize calculations now use the IdsClient API (issue #386), which should improve performance.
* fix issue #379 : restore copying topcat.css into styles/ in setup
* fix issue #381 : IcatClient.getFullName returns UserName if no FullName is found
* fix issue #384 : Cart no longer inherited by different user on same browser; also, if
  a different user logs in, the browse path is reset.
* added optional "disabled" flag to site.search configuration in topcat.json. If present and true, the Search tab will not be added.


## 2.4.0

* If the the Icat/IDS returns a 404 error Topcat will now longer mark a download as expired. This used to happen if the Java EE container (e.g. Glassfish) was running but the application had not fully booted up yet.
* Now support file sizes in powers of 1000 bytes or 1024 bytes.
* In topcat.properties adminUsernames can be list of space separated usernames (following Icat stack conventions) as well as the existing comma-space separated.
* New mandatory proprties in topcat.properties: facility.list, facility.[facilityName].icatUrl, facility.[facilityName].idsUrl; this allows Topcat's Javascript
  and JavaEE components to specify different ICAT/IDS urls for facilities if required, and removes a potential security risk.
* Similarly, facility.[facilityName].downloadType.[transport] can be used to define transport-specific URLs for each facility. If the property is not found,
  the facility's idsUrl is used.
* If Topcat encounters problems in communicating with a facility's ICAT/IDS during startup, it
  will alert the user and attempt to ignore the facility and continue to load other configured facilities.
* Search limits are described in the interface.
* If the IDS is not available and Topcat tries to run a getIcatUrl call, Topcat will notify the user via an alert box.
* A file count column can now be added to the Cart.
* When a Topcat goes into maintenance mode users (within a minute) will be automatically logged out.
* If using a single tier, in addition to datafiles you can now add 'Download' buttons to investigations and datasets in the 'Browse' section.
* Improved caching in cart.
* Improved calculation of file size/count.
* Downloads with transport type http should now work (and be treated similarly to https).
* Login failure messages are more specific.
* Fix error when adding entities to cart wherein IDs were converted to large negative numbers as they were read as signed ints rather than longs.
* Fix "display shimmer" at certain zoom factors and window heights in some browsers.
* Topcat now passes the row entity to the show() method of entity action buttons; plugins can inspect this to control whether the button is shown for a particular row.
* **There are database migrations that need to be applied**

## 2.3.6

* fix issue with DOI redirections.

## 2.3.5

* fix issues with line-endings in distribution.
* fix issues with download status checks.
* fix issue with Downloads icon disappearing in some circumstances.

## 2.3.4

* **NOTE**: the distribution zip for this release is unusable: a "fix" for Windows line-endings in text files corrupted the binary files
(including the war file).
* Minor fix to check status of downloads once per minute rather than once per second.

## 2.3.3

* Very minor fix to display instrument scientists in meta tabs.

## 2.3.2

* Very minor fix to work with strict mode in older browsers.

## 2.3.1

* Very minor fix to make the anonymous plugin work.

## 2.3.0

* Now supports file uploads
* The javascript API now has some documentation
* There has been an increase in plugability
* The page functionality has been removed from lang.json and the facility specific images have been remove from the core product. Instead there is a special directory within the release directory called 'content', which after running ./setup install will add any of these files in this directory to the application. As well as adding html pages you can add any media you like. For instance you can use this mechanism to override the favicon.
* You can now add custom html to the sign in page. E.g. this could be used for adding a 'Forgot password' or 'Register' links.
* The ability to cancel or restore a download within the admin interface.
* Bug fixes.


## 2.2.1

* Limits can now be set for cart:
    * a maximum number of files
    * a maximum rotal file size
* Properly chunk GET requests
* If a user doesn't have a fullName in Icat the username will be used instead.
* Removed getStatus from Cart - you will need to remove your cart's status field from Topcat.json
* The prepareData and getStatus calls are now as background thread on the server, so speeding up the user's cart submission time.
* Topcat now performs a much more performant getSize request (for investigations and datasets), which gets cached server side. **Please note** there is now a new "maxCacheSize" field in topcat.properties which needs adding in.
* When a user adds an item to a cart the corresonding getSize call will made to warm the cache up.
* Client side getStatus calls has been removed from the Download (cart) dialog. This was used to determine if an email field needs to be show or not. Now, if the IDS is two tier this email field will always be shown.
* As well as the asynchronously loaded 'size' grid field, there now two more new asynchronous fields (which are much more effient alternatives to 'size'):
    * fileCount fields can now be added to investigation or dataset grids
    * datasetCount fields can now be added to investigation grids
* A 'size' field now be added to the metatabs. However, in order to display this field you have to explicitly click a button.

* **There are database migrations that need applying**

## 2.2.0

* Topcat is now plugable - this is yet to be documented.
* Bug fixes.
* Performance improvements - most notabley low priority requests such as getSize have been throttled back to give way to high priority requests.
* lang.json - CART_ITEM.COLUMN and CART_ITEM.STATUS now sits under CART.COLUMN and CART.STATUS.
* Smartclient is now supported.
* There are database migrations which need to be applied.

## 2.1.0

* Bug fixes.
* Improved documentation.
* The topcat.json (Please refer to the installation docs for more details)
	* "topcatApiPath" -> "topcatUrl" (for "site")
	* "facilities" attribute is now an array of facility objects rather that an index of facility objects.
	* "url" -> "idsUrl" (for "facilities" > [facility] > authenticationTypes > [authenticationType])
	* Now gets validated making it less prone to erroneous configuration.
    * "jpqlExpression" attribute (for "facilities" > [facility] > "browse" > [entity type] > "gridOptions" > "columnDefs" > [columnDef]) no longer exists; instead it has been replaced by "jpqlFilter" and "jpqlSort". 
* Now shows real name (if available) by logout button

## 2.0.0

* Complete rewrite.
	* Now uses Angular (instead of GWT) with a RESTful EJB web API.

## 1.13.1

* Fixed download issue using browse all tree view (This issue is specific to the ISIS facility. If you are already using version 1.13.0, it is not necessary to upgrade).

## 1.13.0

* Add support for ICAT 4.5.0
* Removed free text search
* Enabled "Check Selected Size" button
* Use ids.client 1.2.0


## 1.12.0

* Added External redirect Authentication type
* Changed url separator token from &amp; to ;
* Last used icat server and authentication type now saved to a cookie
* Added ability for users to create datasets
* Added ability for users to upload a datafile
* Removed support for ICAT servers below 4.2
* Changed persistence.xml to enable logging to be configured at container level
* Added new configuration options to topcat-setup.properties to change the url path of topcat and topcatadmin
* Fixed deployment issue when topcat is behind a reverse proxy example apache configuration. 
* Removed the 200 investigation limit on the Browse All data tree
* Resolved issue where large number of datafiles download requests would exceed the max url length limit
* Added support for archived storage with IDS 1.2.0
* Added various download buttons and context menu items. A single datafile downloaded using the context menu will be uncompressed.
* Downloads are no longer split
* Added ability to delete downloads in "My Downloads"
* Bug fixes
* schema changes
    * Add ALLOW_UPLOAD and ALLOW_CREATE_DATASET column to TOPCAT_ICAT_SERVER
    * Add MESSAGE column to TOPCAT_USER_DOWNLOAD

## 1.11.0

* Harmonized installation
* Uses Maven release mechanism
* Added support for ICAT 4.3
* Added support in TopCatAdmin to add an announcement message to the Topcat page header
* Added free text search (support icat 4.3 only)
* Display the full facility name on search results and investigation details
* No download filename prompt if single file selected for download
* Date/time download filename suggested if more than one download file is selected
* Updated gwt to 2.5.1
* Updated gxt to 2.3.1
* Removed MESSAGE property in topcat.properties as it is no longer used
* Bug fixes
* schema changes:
    * Add the column DOWNLOAD_TYPE to the table TOPCAT_ICAT_SERVER
        


## 1.10.0

Changes to the WS interface


## 1.9.0

* Bug fixes.
    
* Added admin console
* Added support for the ICAT Data Service
    
* Added support for ICAT version no v42, this points to the existing ICATInterfacev420
    
* Added MESSAGE to topcat.properties, the message gets displayed by topcat
* Added verbose option to the setup script, -v, -vv and -vvv
* Properties file renamed from glassfish.props to topcat-setup.properties
* schema changes:
    * Drop the columns AUTHENTICATION_SERVICE_URL and AUTHENTICATION_SERVICE_TYPE from TOPCAT_ICAT_SERVER
    * Add the column DOWNLOAD_SERVICE_URL to the table TOPCAT_ICAT_SERVER 
    * Add the column PREPARED_ID to the table TOPCAT_USER_DOWNLOAD 
    * Add the column DISPLAY_NAME to the table ICAT_AUTHENTICATION 
    * Rename the table ICAT_AUTHENTICATION to TOPCAT_ICAT_AUTHENTICATION

        

## 1.8.0

This is a bug fix release for sites still running ICAT 3.4.

* Parameter searching has temporarily been disabled
        
## 1.7.0


* Added range searches to the parameter search 
* Added table ICAT_AUTHENTICATION. Now when logging in to an ICAT if there is more than one type of authentication available the user will be presented with a drop down list of types. NB there is still stuff hard coded for CAS and entries for AUTHENTICATION_SERVICE_URL and AUTHENTICATION_TYPE in the table TOPCAT_ICAT_SERVER. These will be removed in a future release.
        


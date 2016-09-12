#Release Notes

## 2.2.0

* Topcat is now plugable - this is yet to be documented.
* Bug fixes.
* Performance improvements - most notabley low priority requests such as getSize have been throttled back to give way to high priority requests.
* lang.json - CART_ITEM.COLUMN and CART_ITEM.STATUS now sits under CART.COLUMN and CART.STATUS.
* Smartclient is now supported.

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
        


# TopCAT Configuration

TopCAT v2 is configured using 5 files:

 - topcat-setup.properties
 - topcat.properties
 - topcat.json
 - lang.json
 - topcat.css

#### topcat-setup.properties

This properties file configures glassfish and database used by TopCAT.

This file is copied to the glassfish domain config directory on installation.

###### Configuration properties

  - **driver** - is the name of the jdbc driver and must match the jar file for your database that you stored in the previous step.
  - **dbProperties** -
  identifies the icat database and how to connect to it.
  - **glassfish** - is the path to the top level of the glassfish installation. It must contain "glassfish/domains", and will be referred to here as GLASSFISH_HOME as if an environment variable had been set.
  - **port** - is the administration port of the chosen glassfish domain which is typically 4848.
  - **topcatUrlRoot** - is the context path where topcat will be deployed (e.g /topcat). Use / for root context.
  - **mail.host** - is the smtp host address
  - **mail.user** - is the name of mail acount user when connecting to the mail server
  - **mail.from** - is the mail from address
  - **mail.property** - is the javamail properties. See https://javamail.java.net/nonav/docs/api/ for list of properties
  - **adminUsername** - The basic authentication user name for the admin REST API
  - **adminPassword** - The basic authentication password for the admin REST API


#### topcat.properties

This properties file configures the TopCAT application backend.

Here you configure the location of preparedfiles, enable email and various email templates.

###### Configuration properties

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

#### topcat.json

This json file configures the TopCAT frontend.

Here you configure how your icat data should be viewed. You can also create static pages in combination with lang.json

This file is copied to the glassfish application web config directory on installation.

Please note this json file is deleted from the application web directory on topcat reinstallation. We recommend you not to edit this file directly in the web directory. Instead, edit the json file in the extracted install directory and do a reinstall. You will not lose your settings this way.

See [topcat.json options](#topcatjson-options) for configurtion properties.

#### lang.json

This json file configures all text on the TopCAT frontend. Static html pages can also be created using this file in addition to topcat.json

This file is copied to the glassfish application web languages directory on installation.

Please note this json file is deleted from the application web directory on topcat reinstallation. We recommend you not to edit this file directly in the web directory. Instead, edit the json file in the extracted install directory and do a reinstall. You will not lose your settings this way.

TopCAT v2 uses angular-translate to in order to store text in a single file. The file contains a single json object. Please see the "Teaching your app a language" section from the [angular-translate guide ](https://angular-translate.github.io/docs/#/guide/02_getting-started) for details. You should use the lang.json.example that comes with the topcat distro as your starting point.

#### topcat.css

An empty style sheet to allow you to customise your TopCAT site.

This file is copied to the glassfish application web languages styles directory on installation.

Please note this css file is deleted from the application web directory on topcat reinstallation. We recommend you not to edit this file directly in the web directory. Instead, edit the css file in the extracted install directory and do a reinstall. You will not lose your settings this way.

### topcat.json options

The frontend configuration topcat.json is made up of a json object with two main properties:

```
{
    site: {

    },
    facilities: {

    }
}

```

TopCAT v2 allows the configuration of multiple facilities. These facilities can be on a single icat server or from multiple icat servers. If you wish to display a facility in TopCAT, you must add a new configuration facility object under the facilities property.

A simplified summary of the configuration object is shown below:

```
{
    site: {
        "topcatUrl": "string",
        "home" : "string",
        "paging" : {
            "pagingType": "string",
            "paginationNumberOfRows": integer,
            "paginationPageSizes": [
                integer
            ],
            "scrollPageSize": integer,
            "scrollRowFromEnd": integer,
        }
        "facilitiesGridOptions": {},
        "cartGridOptions" : {}
        "myDataGridOptions" : {}
        "myDownloadGridOptions" : {},
        "metaTabs" : {}
        "pages" : {}

    },
    facilities: {
        "string" : {
            "facilityName" : "string",
            "title" : "string",
            "icatUrl": "string",
            "idsUrl": "string",
            "facilityId": integer,
            "hierarchy" : [
                "string"
            ],
            "authenticationType": [],
            "downloadTransportType" : [],
            "browseGridOptions" : {},
            "metaTabs" : {}
        }
    }
}
```

For multiple facilities, you add an additional FACILITY\_KEY\_NAME property and a [facility object](#facility-object-properties) as its value. e.g.

```
facilities: {
    "FACILITY_KEY_NAME_1" : {

    },
    "FACILITY_KEY_NAME_2" : {

    }
}
```


## Site Configuration

The topcat.json configuration must contain one property named **site**.

The site object covers the configuration which are not specific to a particular facility. These includes:

  - The topcat API url
  - The home state name
  - The pagination across the whole site. Scoll or page.
  - The grid configuration for My Data tab
  - The grid configuration for facilities browse (shown when you have more than one facility)
  - The grid configuration for Cart tab
  - The grid cofniguration for Download tab
  - The metaTab panel for facilities browse
  - Html pages you wish to add to TopCAT such as About, Contact, Help pages etc.



### Site Object Properties

###### Property: _**topcatUrl**_ (required)

Value: String

Example:

```
"site" : {
    "topcatUrl" : "api/v1"
}
```

The relative or full path to the TopCAT api. You must leave this as **api/v1** unless you want the TopCAT api hosted on a different server from the web frontend.

****

###### Property: _**home**_ (Required)

Value: String (must be **my-data**, **browse**, **cart** or **download**)

Example:

```
"site": {
    "home": "my-data"
}
```

TopCAT allows you define which main tab should be the home page.

Possible values are **home**, **browse**, **cart** or **download**.

****

###### Property: _**paging**_ (Required)

Value: Paging Object (see [Paging](#paging-object-properties) object)

Example :

```
"site": {
    "paging": {
        "pagingType": "scroll",
        "scrollPageSize": 100,
        "scrollRowFromEnd" : 20
    }
}

```

This configures the pagination type used throughout the entire TopCAT frontend.


****

###### Property: _**facilitiesGridOptions**_ (Required)

Value: GridOptions Object (see [GridOption](#gridoption-object-properties) object)

Example:

```
{
    "facilitiesGridOptions": {
        "enableFiltering": true,
        "columnDefs": [
            {
                "field": "fullName",
                "displayName": "Facility Full Name",
                "title": "BROWSE.COLUMN.FACILITY.FULLNAME",
                "filter": {
                    "condition": "starts_with",
                    "placeholder": "Containing...",
                    "type": "input"
                },
                "link": true
            },
            {
                "field": "name",
                "displayName": "Facility Name",
                "title": "BROWSE.COLUMN.FACILITY.NAME",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            }
        ]
    }
}


```

This configures the grid displayed on the browse facilities page. The browse facilities page is only displayed if multiple facilities are set.

See the [icat schema for facility](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Facility) for possible column fields.

****

###### Property: _**cartGridOptions**_ (Required)

Value: GridOption Object (see [GridOption](#gridoption-object-properties) object)

Example:

```
{
    "cartGridOptions": {
        "enableFiltering": true,
        "columnDefs": [
            {
                "field": "name",
                "title": "CART.COLUMN.NAME",
                "type" : "string",
                "filter": {
                    "condition": "starts_with",
                    "placeholder": "Containing...",
                    "type": "input"
                },
                "cellTooltip": true
            },
            {
                "field": "entityType",
                "title": "CART.COLUMN.ENTITY_TYPE",
                "cellFilter": "entityTypeTitle",
                "type" : "string",
                "filter": {
                    "condition": "starts_with",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            },
            {
                "field": "size",
                "title": "CART.COLUMN.SIZE",
                "type" : "number",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            },
            {
                "field": "availability",
                "title": "CART.COLUMN.AVAILABILITY",
                "type" : "string",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            }
        ]
    }
}

```


This configures the grid displayed on the Cart tab.

Possible column fields are:

  - **name** (string) - the name of the item added to the cart
  - **entityType** (string) - the entity type of the item i.e. investigation, dataset or datafile
  - **size** (number) - displays the total size of the item. The size is retrieved from the ids server via an ajax call. A spinner is display while the result is being retrieved.
  - **availability** (string) - displays the availability of an item. The availability status is retrieved from the ids server via an ajax call. This is useful if the ids is configure for two level storage where files may have to be restored from tape. If files are always online, this field will not be of any use.

****

###### Property: _**myDataGridOptions**_ (Required)

Value: GridOption Object (see [GridOption](#gridoption-object-properties) object)

Example:

```
{
    "myDataGridOptions": {
        "entityType" : "investigation",
        "investigation": {
            "enableFiltering": true,
            "enableSelection": true,
            "includes" : [
                "investigation.investigationInstruments.instrument"
            ],
            "columnDefs": [
                {
                    "field": "title",
                    "displayName": "Title",
                    "title": "BROWSE.COLUMN.INVESTIGATION.TITLE",
                    "type": "string",
                    "filter": {
                        "condition": "contains",
                        "placeholder": "Containing...",
                        "type": "input"
                    },
                    "cellTooltip": true,
                    "link": true
                },
                {
                    "field": "visitId",
                    "displayName": "Visit Id",
                    "title": "BROWSE.COLUMN.INVESTIGATION.VISIT_ID",
                    "type": "string",
                    "filter": {
                        "condition": "contains",
                        "placeholder": "Containing...",
                        "type": "input"
                    },
                    "link": true
                },
                {
                    "field": "investigationInstruments[0].instrument.fullName",
                    "displayName": "Instrument",
                    "type": "string",
                    "visible": true,
                    "filter": {
                        "condition": "starts_with",
                        "placeholder": "Containing...",
                        "type": "input"
                    }
                },
                {
                    "field": "size",
                    "displayName": "Size",
                    "title": "BROWSE.COLUMN.INVESTIGATION.SIZE",
                    "type": "number"
                },
                {
                    "field": "startDate",
                    "displayName": "Start Date",
                    "title": "BROWSE.COLUMN.INVESTIGATION.START_DATE",
                    "type": "date",
                    "cellFilter": "date: 'yyyy-MM-dd'",
                    "filters": [
                        {
                            "placeholder": "From...",
                            "type": "input"
                        },
                        {
                            "placeholder": "To...",
                            "type": "input"
                        }
                    ],
                    "sort": {
                        "direction": "desc"
                    }
                },
                {
                    "field": "endDate",
                    "displayName": "End Date",
                    "title": "BROWSE.COLUMN.INVESTIGATION.END_DATE",
                    "type": "date",
                    "cellFilter": "date: 'yyyy-MM-dd'",
                    "filters": [
                        {
                            "placeholder": "From...",
                            "type": "input"
                        },
                        {
                            "placeholder": "To...",
                            "type": "input"
                        }
                    ]
                }
            ]
        }
    }
}

```



This configures the grid displayed on the My Data tab. The My Data grid can list either investigation or dataset entity types. The type you want to list is set using the _**entityType**_ property in the GridOption object. This entityType property is specific to myDataGridOptions and is not used elsewhere.

Once entityType is set, the possible fields available are as per the [icat schema](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html) for investigation or dataset. In addition, the following fields are also available:

  - **size** (number) - displays the total size of the entity. The size is retrieved from the ids server via an ajax call. A spinner is display while the result is being retrieved.
  - **availability** (string) - displays the availability of an item. The availability status is retrieved from the ids server via an ajax call. This is useful if the ids is configure for two level storage where files may have to be restored from tape. If files are always online, this field will not be of any use.

****

###### Property: _**myDownloadGridOptions**_ (Required)

Value: GridOption Object (see [GridOption](#gridoption-object-properties) object)

Example:

```
{
    "myDownloadGridOptions": {
        "enableFiltering": true,
        "columnDefs": [
            {
                "field": "fileName",
                "title": "DOWNLOAD.COLUMN.FILE_NAME",
                "type" : "string",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            },
            {
                "field": "transport",
                "title": "DOWNLOAD.COLUMN.TRANSPORT",
                "type" : "string",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            },
            {
                "field": "status",
                "title": "DOWNLOAD.COLUMN.STATUS",
                "type" : "string",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                }
            },
            {
                "field": "createdAt",
                "title": "DOWNLOAD.COLUMN.CREATED_AT",
                "type" : "date",
                "cellFilter": "date: 'yyyy-MM-dd HH:mm:ss'",
                "filter": {
                    "placeholder": "Date...",
                    "type": "input"
                },
                "sort": {
                    "direction": "desc"
                }
            }
        ]
    }
}

```

This configures the grid displayed on the Download tab.

Possible column fields are:

  - **fileName** (string) - the download name of the request
  - **transport** (string) - the transport type of the download request
  - **facilityName** (string) - the name of the facility the download request was made to
  - **status** (string) - the status of the download request
  - **createdAt** (date) - the timestamp when the download request was submitted

****

###### Property: _**metaTabs**_ (required)

Value: object with a property named **facility** whose value is a [metaTab object](#metatab-object-properties)

Example:

```
"metaTabs": {
    "facility": [
        {
            "title": "Facility Details",
            "title": "METATABS.FACILITY.TABTITLE",
            "field": "facility",
            "default": true,
            "data": [
                {
                    "title": "Full Name",
                    "title": "METATABS.FACILITY.FULLNAME",
                    "field": "fullName"
                },
                {
                    "title": "Description",
                    "title": "METATABS.FACILITY.DESCRIPTION",
                    "field": "description"
                },
                {
                    "title": "Name",
                    "title": "METATABS.FACILITY.NAME",
                    "field": "name"
                },
                {
                    "title": "URL",
                    "title": "METATABS.FACILITY.URL",
                    "field": "url"
                }
            ]
        }
    ]
}

```

This property sets what metadata is displayed in the metatab panel area (at the bottom half of the page) when a user click on a row on the browse facilties grid.

For the site object, metaTabs must have a single property named **facility**




###### Property: _**pages**_ (Optional)

Value : array of page objects (see [Page](#page-object-properties) object)

Example: See [Page](#page-object-properties) object

You can create simple html pages in TopCAT. Each page is defined using a page object which defines the url of the page. The html page content is defined in lang.json file.

*****



### Paging Object Properties

###### Property: _**pagingType**_ (Required)

Value: string (must be _page_ or _scroll_)

The pagination type used throughout the site. Possible value are _page_ or _scroll_. _page_ uses a sequence of number to navigate between each page while _scroll_ uses a scroll bar or mouse wheel to navgiate to the next page.

****

###### Property: _**paginationNumberOfRows**_ (Required only for pagingType of _page_)

Value: integer

The default number of rows to display per page.

****

###### Property: _**paginationPageSizes**_ (Optional for pagingType of _page_)

Value: array of integers

This adds a dropdown menu on the paginator navigation bar so that users can select the number of rows per page. The possible number of rows per page options are the one defined in the array.

****

###### Property: _**scrollPageSize**_ (Required for pagingType of _scroll_)

Value: integer

The number or rows to fetch at the start and when scrolling for the next page

****

###### Property: _**scrollRowFromEnd**_ (Required for pagingType of _scroll_)

Value: Integer (Should be smaller than scrollPageSize)

This is the number of rows remaining before a fetch is made for the next page. For example, if the scrollPageSize is set to 100 and scrollRowFromEnd is 20. When you scroll to row 80, a request is made to fetch the next 100 rows. This gives the user a smoother scolling experience as the data will be preloaded.

*****

### GridOption Object Properties

###### Property: _**enableFiltering**_ (Optional)

Value: boolean (must be **true** or **false**)

False by default. When enabled, this setting adds filter boxes to each column header, allowing filtering within the column for the entire grid. Filtering can then be disabled on individual columns using the columnDefs.

****

###### Property: _**enableSelection**_ (Optional)

Value: boolean (must be **true** or **false**)

This sets whether a selection (checkbox) column should be added to the grid. This selection column is used to add/remove items to the cart and as such, only entities **investigation**, **dataset** and **datafile** are supported. It only make sense to use this property for the grid in My Data tab and Browse tab.


****

###### Property: _**columnDefs**_ (Required)

Value: array of [columnDefs](#columndefs-object-properties) objects


This defines the columns for a grid.


****

###### Property: includes (Optional)

Value: Array of strings

For grids that display an entity, it is possible to add a field from a different entity that has a relationship to the current entity. For example, if the grid entity is investigation, it is possible to add a column to display the instrument name of the investigation. To do this, you must add an includes property and add a relationship (see [icat schema](#http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Facility)) using dot notation. e.g.

```
"includes" : [
    "investigation.investigationInstruments.instrument"
]
```

All include in the array must be unique.


The field in columnDef for the full name of the instrument will be:

```
    "field": "investigationInstruments[0].instrument.fullName",

```

investigationInstruments is an array since it has a one to many relationship. Since a column can display only a single value, an index of the array must be specified.


*****

### ColumnDefs Object Properties

###### Property: _**cellFilter**_ (Optional)

Value: string

Example:

```
{
    "type" : "date",
    "cellFilter": "date: 'yyyy-MM-dd HH:mm:ss'",
}
```

This applies a format filter to the cell value.

Possible values are:

  - **entityTypeTitle** - replaces the entitytype name to the ones defined under ENTITIES in lang.json
  - **date: 'DATE_FORMAT'** - format date [DATE_FORMAT](https://docs.angularjs.org/api/ng/filter/date)
  - **bytes** - format integer to b, Kb, Mb, etc

****

###### Property: _**cellTooltip**_ (optional)

Value: boolean

This sets whether or not to show a tooltip when a user hovers over a cell in this column.

****

###### Property: _**displayName**_ (Either displayName or title must be set)

Value: string

The column name that will be shown in the grid header. See also [title](#property-translatedisplayname-optional-either-translatedisplayname-or-displayname-must-be-set).

****

###### Property: _**field**_

Value: string

The data property name whose value will be used for this column. For icat enitities please see [icat schema](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html) for field names.

You can also use fields of related icat entities. See [includes property](#property-includes-optional).

****

###### Property: _**filter**_ (Optional)

Value: filter object

Example:

```
"filter": {
    "condition": "contains",
    "placeholder": "Containing...",
    "type": "input"
}
```

Specify a single filter field on this column.

****

###### Property: _**filters**_ (Optional)

Value: array of filter objects (see [filter](#filter-object-properties) object)

Example:

```
"filters": [
    {
        "placeholder": "From...",
        "type": "input"
    },
    {
        "placeholder": "To...",
        "type": "input"
    }
]
```

Specify multiple filter fields on this column.

****

###### Property: _**link**_ (Optional)

Value: boolean

Turns this column value into a hyperlink. The hyperlink navigates to the next entity in your hierarchy

****

###### Property: _**maxWidth**_ (Optional)

Value: integer

Set the maximum width of a column in pixels

****

###### Property: _**minWidth**_ (Optional)

Value: integer

Set the minimum width of a column in pixels

****

###### Property: _**sort**_ (Optional)

Value: object

Example:

```

"sort" : {
    "direction": "asc"
}

```

The default column to sort by for a grid. Only one columnDefs object per grid should have a sort property.

The _direction_ property value must be either **asc** or **desc**.

****

###### Property: _**title**_ (Optional. Either title or displayName must be set)

Value: string

The property to use from lang.json for this column name shown in the grid header.

If both displayName and title are set, title is used.

****

###### Property: _**type**_ (Optional but recommended)

Value: string (must be **string**, **boolean**, **number**, **date**)

The type of the data value for this column. This is use for sorting. If type is not provided, the grid tries to guess the type.

****

### Filter Object Properties


###### Property: _**condition**_  (Required)

Value: string (Must be **contains**, **starts_with**, **ends_with**)

This sets how the term entered in a filter will be matched. **contains** matches any part of the value. **start_with** will only match if values starts with the entered term. **ends_with** will only match values that ends with the entered term.

****

###### Property: _**placeholder**_ (Required)

Value : string

The placeholder string shown in the filter input box.

****

###### Property: _**type**_ (Required)

Value: string (Must be **input** or **select**)

The filter input type. Input is a standard input box while select is a dropdown menu. If set to select, you must include a [selectOptions](#property-selectoptions-optional) property to list the available options.

****

###### Property: _**selectOptions**_ (Optional)

Value: Array of option objects

Example:

```
filter : {
    "type" : "select",
    "selectOptions" : [
        {
            value: 1,
            label: 'male'
        },
        {
            value: 2,
            label: 'female'
        }
    ]
}

```

The select options used for a select type filter.


### Page Object Properties

Page object example:

```
{
    "url" : "/about",
    "stateName": "about",
    "templateTranslateName" : "PAGE.ABOUT.HTML",
    "addToNavBar": {
        "linkLabel" : "MAIN_NAVIGATION.ABOUT",
        "align" : "left"
    }

}
```

In lang.json object:

```
{
    "MAIN_NAVIGATION" : {
        "ABOUT" : "About"
    },
    "PAGE" : {
        "ABOUT" {
            "HTML" : "<h1>ABOUT</h1><p>About me</p>"
        }
    }
}
```

###### Property: _**url**_ (required)

Value: string

Example: see [Page](#page-object-properties) object example

The relative url of the page. It must start with a /.

****

###### Property: _**stateName**_ (required)

Value: string

Example: see [Page](#page-object-properties) object example

A unique name for the page state/route. This name can be used to create hyperlinks in html pages using the attribute `ui-sref="STATE_NAME"` instead of `href=""`. e.g. `<a ui-sref="about">Link</a>` where the stateName for the page is named `about`.

****

###### Property: _**templateTranslateName**_ (required)

Value: string

Example: see [Page](#page-object-properties) object example

The content of the html page must be set in the lang.json file. The value for templateTranslateName must be a property in lang.json whose value is the html content of the page.

Note that json specification does not allow new lines in values so html content must be in a single line and escaped. This [online tool](http://bernhardhaeussner.de/odd/json-escape/) can be used to do this.

****

###### Property: _**addToNavBar**_ (Optional)

Value: object

Example: see [Page](#page-object-properties) object example

This property, if set, will add a navigation button on the main navigation bar.

The object has two properties:

  - **linkLabel** (string - required) - a property in lang.json that will be used as the navigation button text label
  - **align** (string - required) - whether the navigation button align to the left or right of the navigation bar. Value must be **left** or **right**



## Facilities Configuration

For each facility you wish to add to TopCAT, you must add a property/key and a facility object to the facilities object.

Example:

```
"facilities": {
    "KEY_NAME_1" : {
        ....
    },
    "KEY_NAME_2" : {

    }
}
```

The property name is use by TopCAT to identify the facility and must be unique. It appears in urls so avoid using special characters or spaces.

### Facility Object Properties

###### Property: _**facilityName**_ (required)

Value: string

A facility name used internally by TopCAT. This must match the key/property name of the facility object.

****

###### Property: _**title**_ (required)

Value: string

The name of the facility you wish to display to the user. This name appears in columns and notification messages.

****

###### Property: _**icatUrl**_ (required)

Value: string

The icat server url for the facility.

****

###### Property: _**facilityId**_ (required)

Value: integer

The id value of the facility in icat. The id can be retrieve using https://facilities02.esc.rl.ac.uk/icat/entityManager?query=SELECT+f+from+Facility+f&sessionId=SESSION_ID (replace SESSION_ID with a valid sessionId).

You can get a valid sessionId using the following wget command (replace YOUR_USERNAME, YOUR_PASSWORD and YOUR_ICAT_URL. You must urlencode, your username or password if they contain special characters).

```
wget --no-check-certificate --post-data json=%7B%22plugin%22%3A%22ldap%22%2C%20%22credentials%22%3A%5B%7B%22username%22%3A%22YOUR_USERNAME%22%7D%2C%20%7B%22password%22%3A%22YOUR_PASSWORD%22
%7D%5D%7D -qO - https://YOUR_ICAT_URL/icat/session
```

The id value is typically 1 if your icat server has only one facility configured.

****

###### Property: _**idsUrl**_ (required)

Value: string

The url of the ids server for the facility.

****

###### Property: _**hierarchy**_ (required)

Value: Array of strings

Example:

```
"hierarchy": [
    "facility",
    "proposal",
    "investigation",
    "dataset",
    "datafile"
],

```

The hierarchy property defines how your icat data is browsed in TopCAT. Your icat data in made of different entities (see [icat schema](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html)) with the root entity being facility. By defining a hierarchy, you specify the next entity that should be displayed when a user browse the data in a facility.

The supported entities are:

  - [facility](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Facility)
  - [instrument](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Instrument)
  - [facilityCycle](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#FacilityCycle)
  - proposal
  - [investigation](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Investigation)
  - [dataset](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Dataset)
  - [datafile](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#Datafile)

**proposal** is not a real icat entity but a virtual one created for TopCAT. It represents the name column of the Investigation entity. It only has one field called **name** and can used to group investigations using the same name value.

Hierarchy rules:

  - Facility must always be the first entity in the hierarchy array
  - All entities except facility are optional
  - The following orders are supported:
      - facility → instrument
      - facility → facilityCycle
      - facility → proposal
      - facility → investigation
      - facility → dataset
      - facility → datafile
      - instrument → facilityCycle
      - instrument → proposal
      - instrument → investigation
      - instrument → dataset
      - instrument → datafile
      - facilityCycle → proposal
      - facilityCycle → facilityCycle
      - facilityCycle → investigation
      - facilityCycle → dataset
      - facilityCycle → datafile
      - proposal → instrument
      - proposal → investigation
      - investigation → dataset
      - investigation → datafile
      - dataset → datafile

****

###### Property: _**authenticationType**_ (required)

Value: array of authenticationType object

Example:

```
"authenticationType": [
    {
        "title": "Username/Password",
        "plugin": "ldap"
    },
    {
        "title": "DB",
        "plugin": "db"
    },
    {
        "title": "Anonymous",
        "plugin": "anon"
    }
]

```

The authenticationType property sets the possible authentication type that TopCAT can use to authenticate against icat.

An authenticationType object has the following properties:


  - **title** (string - required) - the dropdown menu option name. Dropdown menu  is displayed only if more than one authentication type is set.
  - **plugin** (string - required) - the icat plugin name used for authentication. i.e. **db**, **ldap**, **uows**, **anon** (depends on icat server).

****

###### Property: _**downloadTransportType**_ (required)

Value: array of downloadTransportType object

Example:

```
"downloadTransportType": [
    {
        "displayName" : "Https",
        "type" : "https",
        "default" : true,
        "url": "https://fdsgos11.fds.rl.ac.uk"
    },
    {
        "displayName" : "Globus",
        "type" : "globus",
        "url": "https://fdsgos11.fds.rl.ac.uk"
    }
]
```

The downloadTransportType property sets the possible download transport type that TopCAT can use to download files. You must have at least one downloadTransportType object in the array.

A downloadTransportType object has the following properties:


  - **displayName** (string - required) - the dropdown menu option name the the user select
  - **type** (string - required) - the transport type. Must be **https** or **globus**.
  - **default** (boolean - optional) - which downloadTransportType should be the default on the dropdown menu. Only one downloadTransportType should have this property
  - **url** (string - required) - the url of the ids server.


TopCAT also support a transport call smartclient which uses a desktop application call [IDS Smart Client](http://icatproject.org/user-documentation/ids-smartclient/) to download files. TopCAT automatically detects if the application is running. If it is running, TopCAT will add the option to the transport dropdown menu.


###### Property: _**browseGridOptions**_ (required)

Value: object mapping hierarchy values to [GridOptions](#gridoption-object-properties) objects

Example:

```
"browseGridOptions": {
    "instrument": {
        "enableFiltering": true,
        "columnDefs": [
            {
                "field": "fullName",
                "displayName": "Full Name",
                "title": "BROWSE.COLUMN.INSTRUMENT.FULLNAME",
                "type": "string",
                "filter": {
                    "condition": "contains",
                    "placeholder": "Containing...",
                    "type": "input"
                },
                "sort": {
                    "direction": "asc"
                },
                "link": true
            }
        ]
    },
    "proposal": {
        "enableFiltering": true,
        "columnDefs": [
            {
                "field": "name",
                "displayName": "Name",
                "type": "string",
                "sort": {
                    "direction": "asc"
                },
                "filter": {
                    "condition": "starts_with",
                    "placeholder": "Containing...",
                    "type": "input"
                },
                "cellTooltip": true,
                "link": true
            }
        ]
    },
    .......
}
```
This configures the grid displayed on the browse tab. Browsing data is based on the hierarchy defined for a facility. Each facility can have its own hierarchy.

For each entity defined in the hierarchy array, you must add a matching property with a [GridOptions](#gridoption-object-properties) object as its value in the browseGridOptions object.



###### Property: _**metaTabs**_ (Optional)

Value: object with a property for each hierarchy entity with a [metaTab object](#metatab-object-properties) as its value

Example:

```
"metaTabs": {
    "instrument": [
        {
            "title": "METATABS.INSTRUMENT.TABTITLE",
            "field": "instrument",
            "default": true,
            "data": [
                {
                    "title": "METATABS.INSTRUMENT.NAME",
                    "field": "fullName"
                },
                {
                    "title": "METATABS.INSTRUMENT.DESCRIPTION",
                    "field": "description"
                },
                {
                    "title": "METATABS.INSTRUMENT.TYPE",
                    "field": "type"
                },
                {
                    "title": "METATABS.INSTRUMENT.URL",
                    "field": "url"
                }
            ]
        }
    ]
}
```


For each entity defined in the hierachy, you should have a corresponding property (named the same as the entity) in the metaTab object. Each property must have a [metaTab object](#metatab-object-properties) as its value.



### MetaTab Object Properties

A metaTab object corresponds to a single tab in the metatab area. It allows you to define what fields should be displayed for an entity or related entities.

###### Property: _**displayName**_ (Optional. Either title or displayName must be set)

Value: string

The title of tab.

If both displayName and title are set, title is used.


###### Property: _**title **_ (Optional. Either title or displayName must be set)

Value: string

Use a property from lang.json as the title of the tab.

If both displayName and title are set, title is used.


###### Property: _**default **_ (required)

Value: boolean

Set this to true if the field matched the property name of the object else set it as false.


###### Property: _**queryParams**_ (optional)

Value: array of strings (where string is a relationship class name to the current entity)

Example:

```
"queryParams": [
    "InvestigationUser",
    "User"
]

```

It is possible to add a tab containing metadata from a related entity. For example, if the current entity is an investigation we can add a tab to list all investigation users for the investigation. To do this, we add a queryParams properties and an array which maps the relationship from investigation to investigation user using the entity class name (see [icat schema](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html)) e.g.

[InvestigationUser](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#InvestigationUser) → [User](http://icatproject.org/mvn/site/icat/server/${icat.schema.version.doc}/schema.html#User)


###### Property: _**data**_ (required)

Value: array of objects

Example:

```
"data": [
    {
        "title": "METATABS.INVESTIGATION.NAME",
        "field": "name"
    },
    {
        "title": "METATABS.INVESTIGATION.TITLE",
        "field": "title"
    },
    {
        "title": "METATABS.INVESTIGATION.SUMMARY",
        "field": "summary"
    },
    {
        "title": "METATABS.INVESTIGATION.START_DATE",
        "field": "startDate"
    },
    {
        "title": "METATABS.INVESTIGATION.END_DATE",
        "field": "endDate"
    }
]

```


Example (fields from a related entity) :

```
"data": [
    {
        "field": "user",
        "default": false,
        "data": [
            {
                "title": "METATABS.INVESTIGATION_USERS.NAME",
                "field": "fullName"
            }
        ]
    }
]
```

This sets the field data to display in a meta tab. The object has 4 possible properties:

  - **displayName** (string - optional) - the field label
  - **title** (string - optional) - Use a property from lang.json as the field label
  - **field** (string - required) - the entity field name whose value will be displayed
  - **data** (object - optional) - used when you wish to display relationship fields

Either displayName or title must be set. If both title and title are set, title is used.
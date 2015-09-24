### Configuration

Configuration of topcat is made up of 4 files:

 - topcat-setup.properties
 - topcat.properties
 - topcat.json
 - lang.json

#### topcat-setup.properties

This properties file configure the glassfish and database used for topcat.

This file is copied to the glassfish domain config directory.


#### topcat.properties

This properties file configures the topcat backend application.

Here you configure the location of preparedfiles, whether to enable email and various email templates.

This file is copied to the glassfish domain config directory.


#### topcat.json

This json file configures the topcat frontend.

Here you configure how your icat data should be viewed. You can also create static pages in combination with lang.json

This file is copied to the glassfish application web config directory.

#### lang.json

This json file configures all text on the topcat frontend. Static html pages can also be created using this file in addition to topcat.json

This file is copied to the glassfish application web languages directory.



#### topcat.json options


The frontend configuration topcat.json is made up of a json object with two main properties:

```
{
    facilities: {

    },
    site: {

    }

}

```

Topcat v2 allows the configuration of multiple facilities (a single icat server can have multiple facilties), if you wish topcat to display a facility, you must add a new configuration under the facilities section.

A simplified summary of the configuration object is shown below:

```
{
    facilities: {
        "FACILITY_KEY_NAME" : {
            "facilityName" : "FACILITY_KEY_NAME",
            "title" : "FACILITY_NAME",
            "icatUrl": "URL_OF_ICAT_SERVER",
            "idsUrl": "URL_OF_ICAT_SERVER",
            "idsIsToLevel": boolean,
            "facilityId": ID_NUMBER_OF_THE_FACILITY_IN_ICAT,
            "hierarchy" : [
                LIST_OF_HIERACHY_ENTITIES_IN_ORDER
            ],
            "authenticationType": [
                LIST_OF_AUTHENTICATION_TYPE_OBJECTS
            ],
            "downloadTransportType" : [
                LIST_OF_TRANSPORT_TYPE_OBJECTS
            ],
            "browseGridOptions" : {
                KEY_VALUE_PAIRS_FOR_BROWSE_GRID
            },
            "metaTabs" : {
                KEY_VALUE_PAIRS_FOR_META_TAB
            }
        }
    },
    site: {
        "topcatApiPath": "RELATIVE_PATH_TO_TOPCAT_API",
        "home" : "HOME_ROUTE_NAME",
        "pagingType": "PAGINING_TYPE",
        "paginationNumberOfRows": DEFAULT_NUMBER_OF_ROWS_PER_PAGE,
        "paginationPageSizes": [
            LIST_OF_NUMBERS_OF_ROWS_PER_PAGE
        ],
        "scrollPageSize": NUMBER_OF_ROWS_PER_SCROLL,
        "scrollRowFromEnd": NUMBER_OF_ROWS_FROM_END_BEFORE_SCROLL,
        "facilitiesGridOptions": {
            KEY_VALUE_PAIRS_FOR_FACILITIES_GRID
        },
        "cartGridOptions" : {
            KEY_VALUE_PAIRS_FOR_CART_GRID
        }
        "myDataGridOptions" : {
            KEY_VALUE_PAIRS_FOR_MYDATA_GRID
        }
        "myDownloadGridOptions" : {
            KEY_VALUE_PAIRS_DOWNLOAD_GRID
        },
        "facility" : {
            KEY_VALUE_PAIRS_FOR_META_TABS
        }
        "pages" : {
            LIST_OF_OBJECTS_TO_CREATE_STATIC_HTML_PAGES
        }

    }
}
```

For multiple facilities, you add additional FACILITY\_KEY\_NAME keys and a configuration object for the additional facility. e.g.

```
{
    facilities: {
        "FACILITY_KEY_NAME_1" : {

        },
        "FACILITY_KEY_NAME_2" : {

        }
    }
}
```


### Site Configuration Object

The topcat.json configuration object must contain one property named **site**. The property must have an value containing an [object](#site-properties) with the properties below.


#### Site Properties

_**topcatApiPath**_ (required)

Value: String

Example:

```
"site" : {
    "home" : "my-data"
}
```


The relative or full path to the topcat api. You should leave this as api/v1 unless you want the api on a different server.

_**home**_ (Required)

Value: String (must be _my-data_, _browse_, _cart_ or _download_)

Example:

```
"site": {
    "home": "my-data"
}
```

Topcat allows you define which main tab should be the home page when a home link or when a user first login in topcat. Possible values are _home_, _browse_, _cart_ or _download_.

_**paging**_ (Required)

Name: paging
Value: Paging Object

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

See [Paging](#paging-properties) object



_**facilitiesGridOptions**_ (Required)

Value: Grid Option Object

See [Grid Option](#grid-option-properties) object

*****

#### Paging Properties

_**pagingType**_ (Required)

Value: string (must be _page_ or _scroll_)

The pagination type used throughout the site. Possible value are _page_ or _scroll_. _page_ uses a sequence of number to navigate between each page while _scroll_ uses a scroll bar or mouse wheel to navgiate to the next page.


_**paginationNumberOfRows**_ (Required only for pagingType of _page_)

Value: integer

The default number of rows to display per page.

_**paginationPageSizes**_ (Optional for pagingType of _page_)

Value: array of integers

This adds a dropdown menu on the paginator navigation bar so that users can select the number of rows per page. The possible number of rows per page options are the one defined in the array.


_**scrollPageSize**_ (Required for pagingType of _scroll_)

Value: integer

The number or rows to fetch at the start and when scrolling for the next page


_**scrollRowFromEnd**_ (Required for pagingType of _scroll_)
Value: Integer (Should be smaller than scrollPageSize)

This is the number of rows remaining before a fetch is made for the next page. For example, if the scrollPageSize is set to 100 and scrollRowFromEnd is 20. When you scroll to row 80, a request is made to fetch the next 100 rows. This gives the user a smoother scolling experience as the data will be preloaded.

*****

#### Grid Option Properties

_**enableFiltering**_ (Optional)

Value: boolean (must be true or false)

False by default. When enabled, this setting adds filter boxes to each column header, allowing filtering within the column for the entire grid. Filtering can then be disabled on individual columns using the columnDefs.


_**columnDefs**_ (Required)

Value: array of columnDef objects

See [columnDefs](#columndefs-properties)

*****

#### ColumnDefs Properties

_**cellTooltip**_ (optional)

Whether or not to show a tooltip when a user hovers over a cell in this column.

_**displayName**_ (Either displayName or translateDisplayName must be set)

Value: string

The column name that will be shown in the grid header.

_**field**_

Value: string

The data property name whose value will be used for this column.

_**filter**_ (Optional)

Value: filter object

Specify a single filter field on this column.


_**filters**_ (Optional)

Value: array of filter objects

Specify multiple filter fields on this column.

See [filter](#filter-properties) object


_**link**_ (Optional)

Value : boolean (must be true or false)

Turns this column value into a hyperlink. The hyperlink navigates to the next entity in your hierarchy

_**translateDisplayName**_ (Optional. Either translateDisplayName or displayName must be set)

Value: string

The property to use from lang.json for this column name that will be shown in the grid header.
If both displayName and translateDisplayName are set, translateDisplayName has priority over displayName.


_**type**_ (Optional but recommended)

Value: string (must be string, boolean, number, date)

The type of the data value for this column. This is use for sorting. If type is not provided, the grid tries to guess the type.


#### filter Properties


_**condition**_  (Required. Must be contains, STARTS_WITH, ENDS_WITH, CONTAINS)

Value: string

Determine how the term enter with be matched. Contains will match is the entered term matches any part of the start. Start_with will only match if the value starts with the entered term. End with will only match value that ends with the there entered term.


_**placeholder**_ (Required)

Value : string

The placeholder string shown in the input box.


_**type**_ (Required. Must be input or select)

Value: string

The filter input type. **input** is a standard input box. Select is a drop menu. If set to **select**, you must include a selectOptions property to list the available options.

_**selectOptions**_ (Optional)

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

The select

















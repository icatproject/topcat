# Quick Configuration Guide

##Configure topcat.json

  1. Start your configuration using the topcat.json.example in the distribution root directory. Rename this file to topcat.json.

  2. Leave everything as default under the **site** property. You can customise this later.

  3. Go to the facilities property further down the file.

  4. Change the existing property named **mysite** directly under facilities to something more suitable for your facility (avoid special characters or spaces. This is used as an identifier and appears in the url). e.g. `dls`

  5. Change the **facilityName** property to exactly the name used above for the property name. e.g. `dls`

  6. Change the **title** property to your full facility name  e.g. `Diamond Light Source`

  7. Change the **icatUrl** to that of your icat servers e.g. `https://icat.server.ac.uk` (the icat REST api is expected to be at `https://icat.server.ac.uk/icat`)

  8. Change the **icatUrl** to that of your ids servers e.g. `https://ids.server.ac.uk` (the ids REST api is expected to be at `https://ids.server.ac.uk/ids`)

  9. Change the **facilityId** property value to the id number of your facility in icat. You can retrieve this fro your database or try the method [here](facility.html#Property:_facilityId_required).

  10. Change the **hierarchy** property array. Add/remove items from the array depending on the hierarchy you want to browse your data. If you're not sure, leave it as it is and change it later.

  11. Add or remove any of the authenticationType objects aalredy listed in the array of the **authenticationType** property. These should match the authentication plugins you have installed for your icat.

  12. The **downloadTransportType** property has one existing downloadTransportType object in the array. Change the url property of this object to that of your ids.

  13. Your topcat.json now has the minimum configuration to install and get a working topcat



##Customisating topcat

The easiest way to customise topcat is to do it live by editing the `config/topcat.json` and `languages/lang.json` in the glassfish application directory once you have installed topcat. You will see the change with a browser refresh.

Please note that files in the glassfish application are deleted on redeployment (i.e. on glassfish re-installation). Once you are happy with your configuration you should copy the files back to where you extracted the topcat distribution or else you may loose your configuration.


#### Changing the home page

When you first login or click on a Home page link, you will be taken to My Data tab. If you want the users to be taken to a different tab, you can change this by changing the [site.home](site.html#) property. The value can be cart, browse or download.


#### Changing pagination type

The example topcat site is configured to use mouse scrolling to paginate results on all the grids. You can change this to a standard page pagination where you have a page navigation bar along the bottom of the grid as shown below.

![page navigation](images/page-navigation.png)


To do this, change the site.paging.pagingType property to scroll.

You can also set the default number of items per row for the page using the site.paging.paginationNumberOfRows property.

The page navigation allows users to set the number of rows per page. You can change these presets in the site.paging.paginationPageSizes property.


### Change the page size when using scroll paging type

You can change how many rows are fetched when a page scroll is performed by change the property site.paging.scrollPageSize. You should also change the site.paging.scrollRowFromEnd property to a suitable value. The scrollRowFromEnd property is the number of rows remaing on a page before a fetch for the next data is performed.


### Add a new column to My Data grid

You wish to add an investigation doi column to the My Data grid. The doi column should be added between the existing size and start date column. Users should be able to filter this column.


The first thing you want to do is look at the [icat schema for investigation](http://icatproject.org/mvn/site/icat/server/4.6.0-SNAPSHOT/schema.html#Investigation) and find out the field name and type for doi.


Once you know this information you can add a new columnDef object to the site.myDataGridOptions.investigation.columnDefs array in between the existing size and start_date object as shown below:

```
{
    "site": {
        ...
        ...
        "myDataGridOptions": {
            ...
            ...
            "investigation": {
                "columnDefs": [
                    {
                        "field": "title",
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.TITLE",
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
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.VISIT_ID",
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
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.INSTRUMENT.NAME",
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
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.SIZE",
                        "type": "number"
                    },
                    {
                        "field": "doi",
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.DOI",
                        "type": "string",
                        "filter": {
                            "condition": "contains",
                            "placeholder": "Containing...",
                            "type": "input"
                        }
                    },
                    {
                        "field": "startDate",
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.START_DATE",
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
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.END_DATE",
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

```

In order to keep all the site text in one location  (lang.json file), the translateDisplayName property was used instead of displayName for the column title. We must now add the corresponding BROWSE.COLUMN.INVESTIGATION.DOI property in the lang.json file.


```
"BROWSE": {
        "COLUMN": {
            ...
            ...
            "INVESTIGATION":{
                "ID": "Id",
                "NAME": "Name",
                "TITLE": "Title",
                "SUMMARY": "Summary",
                "VISIT_ID": "Visit Id",
                "START_DATE": "Start Date",
                "END_DATE": "End Date",
                "DOI": "Doi",
                "RELEASE_DATE": "Release Date",
                "SIZE" : "Size",
                "FACILITY" : "Facility",
                "INSTRUMENT" : {
                    "NAME" : "@:BROWSE.COLUMN.INSTRUMENT.NAME"
                }
            },
            ...
            ...
```

###Change the title of a column

You wish to change the column title of the first column named "Title" in My Data to "Investigation Title".

To do this, first check the site.myDataGridOptions.columnDefs columnDef object for the title field:

```
{
    "site": {
        ...
        ...
        "myDataGridOptions": {
                    {
                        "field": "title",
                        "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.TITLE",
                        "type": "string",
                        "filter": {
                            "condition": "contains",
                            "placeholder": "Containing...",
                            "type": "input"
                        },
                        "cellTooltip": true,
                        "link": true
                    },
                    ...
                    ...

```

Get the translateDisplayName value and change the corresponding BROWSE.COLUMN.INVESTIGATION.TITLE property in language.json:

```
    "BROWSE": {
        "COLUMN": {
            ...
            "INVESTIGATION":{
                "ID": "Id",
                "NAME": "Name",
                "TITLE": "Investigation Title",
                "SUMMARY": "Summary",
                "VISIT_ID": "Visit Id",
                "START_DATE": "Start Date",
                "END_DATE": "End Date",
                "DOI": "Doi",
                "RELEASE_DATE": "Release Date",
                "SIZE" : "Size",
                "FACILITY" : "Facility",
                "INSTRUMENT" : {
                    "NAME" : "@:BROWSE.COLUMN.INSTRUMENT.NAME"
                }
            },
            ...
            ...

```

Note that other grids may also be using this property in which case their column title will also change to "Investigation Title".


If you want only the title column in My Data grid to change. You should add a new property in lang.json and use that property as the translateDisplayName value e.g.


```
{
    "MY_DATA": {
            "COLUMN": {
                "INVESTIGATION":{
                    "TITLE": "Investigation Title",
                }
            }
    }
}

```

And in topcat.json:

```
                    ...
                    ...
                    {
                        "field": "title",
                        "translateDisplayName": "MY_DATA.COLUMN.INVESTIGATION.TITLE",
                        "type": "string",
                        "filter": {
                            "condition": "contains",
                            "placeholder": "Containing...",
                            "type": "input"
                        },
                        "cellTooltip": true,
                        "link": true
                    },
                    ...
                    ...

```


### Change the site logo


Change the html in the SITE_NAME_LOGO property in lang.json. You may want to copy the image to the glassfish application web images directory.



### Prevent investigations from being added to cart


You may wish to prevent users from downloading entire investigations. Items are added to the cart using the selection boxes on the very first column of grids. What we must do is remove this selection columns when the user is browsing a list of investigations.

![selection column](images/selection-column.png)

There are 2 places where a user can do this. On the My Data grid and on the Browse grid. We can disable the selection column using the [enableSelection]() property and setting it to false for the 2 grids.


```
{
    "site": {
        ...
        ...
        "myDataGridOptions": {
            "entityType" : "investigation",
            "investigation": {
                "enableFiltering": true,
                "enableSelection": false,
                "includes" : [
                    "investigation.investigationInstruments.instrument"
                ],
                ...
                ...
```

```
{
    "facilities": {
        "dls": {
            ...
            ...
        "browseGridOptions": {
            ...
            ...
            "investigation": {
                    "enableFiltering": true,
                    "enableSelection": false,
                    "includes" : [
                        "investigation.investigationInstruments.instrument"
                    ],
                    ...
                    ...

```

The remove selection column will look as below:

![no selection column](images/no-selection-column.png)


It is also possible to disable the selection column for datasets and datafiles.


## Change my data to list datasets instead of investigations

You may want to list a user's datasets instead of investigations on My Data. To do this, change the site.myDataGridOptions.entityType to **dataset** and define the columnDefs array for the dataset.


```
{
    "site" : {
        ...
        ...
        "myDataGridOptions": {
            "entityType" : "investigation",
            "investigation": {
                "enableFiltering": true,
                "enableSelection": true,
                "columnDefs": [
                    {
                        "field": "name",
                        "translateDisplayName": "BROWSE.COLUMN.DATASET.NAME",
                        "type": "string",
                        "filter": {
                            "condition": "contains",
                            "placeholder": "Containing...",
                            "type": "input"
                        },
                        "cellTooltip": true,
                        "link": true
                    },
                    ...
                    ...

```



## Add the name of the investigation type (a relationship) as a column to the browse investigation grid

First refer to the [icat schema](http://icatproject.org/mvn/site/icat/server/4.6.0-SNAPSHOT/schema.html#Investigation). You can see that InvestigationType is a relationship (one to one) with the field name **type** and the field we want to display from the InvestigationType class is **name**. The field we use for the columnDef is thus **type.name**.

We add the columnDef to the investigation property for browseGridOptions as below:


```
{
    ...
    ...
    "facilities": {
        "dls": {
            "browseGridOptions": {
                ...
                ...
                "investigation": {
                    ...
                    ...
                    "columnDefs": [

                        {
                            "field": "type.name",
                            "translateDisplayName": "BROWSE.COLUMN.INVESTIGATION.TYPE.NAME",
                            "type": "string",
                            "filter": {
                                "condition": "starts_with",
                                "placeholder": "Containing...",
                                "type": "input"
                            }
                        },

```

Also add the BROWSE.COLUMN.INVESTIGATION.TYPE.NAME text in lang.json:

```
{
    "BROWSE": {
        ...
        ...
        "COLUMN": {
            ...
            ...
            "INVESTIGATION":{
                ...
                ...
                "TYPE" : {
                    "NAME" : "Investigation Type"
                }

```


The icat server by default does not return relationship data. We must specifically ask to include the data. We can do this using the includes property and add the include in the array for the investigation grid:

```
{
    ...
    ...
    "facilities": {
        "dls": {
            "browseGridOptions": {
                ...
                ...
                "investigation": {
                    ...
                    ...
                    "includes" : [
                        "investigation.investigationInstruments.instrument",
                        "investigation.type"
                    ],
                    ...
                    ...

```
























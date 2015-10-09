# ColumnDefs Object Properties

## Property: _**cellFilter**_ (Optional)

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


## Property: _**cellTooltip**_ (optional)

Value: boolean

This sets whether or not to show a tooltip when a user hovers over a cell in this column.



## Property: _**displayName**_ (Either displayName or translateDisplayName must be set)

Value: string

The column name that will be shown in the grid header. See also [translateDisplayName](#property-translatedisplayname-optional-either-translatedisplayname-or-displayname-must-be-set).



## Property: _**field**_

Value: string

The data property name whose value will be used for this column. For icat enitities please see [icat schema](http://icatproject.org/mvn/site/icat/server/4.6.0-SNAPSHOT/schema.html) for field names.

You can also use fields of related icat entities. See [includes property](#property-includes-optional).



## Property: _**filter**_ (Optional)

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



## Property: _**filters**_ (Optional)

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



## Property: _**link**_ (Optional)

Value: boolean

Turns this column value into a hyperlink. The hyperlink navigates to the next entity in your hierarchy


## Property: _**maxWidth**_ (Optional)

Value: integer

Set the maximum width of a column in pixels


## Property: _**minWidth**_ (Optional)

Value: integer

Set the minimum width of a column in pixels


## Property: _**sort**_ (Optional)

Value: object

Example:

```

"sort" : {
    "direction": "asc"
}

```

The default column to sort by for a grid. Only one columnDefs object per grid should have a sort property.

The _direction_ property value must be either **asc** or **desc**.


## Property: _**translateDisplayName**_ (Optional. Either translateDisplayName or displayName must be set)

Value: string

The property to use from lang.json for this column name shown in the grid header.

If both displayName and translateDisplayName are set, translateDisplayName is used.


## Property: _**type**_ (Optional but recommended)

Value: string (must be **string**, **boolean**, **number**, **date**)

The type of the data value for this column. This is use for sorting. If type is not provided, the grid tries to guess the type.

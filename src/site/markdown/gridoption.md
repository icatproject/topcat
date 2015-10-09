# GridOption Object Properties

## Property: _**enableFiltering**_ (Optional)

Value: boolean (must be **true** or **false**)

False by default. When enabled, this setting adds filter boxes to each column header, allowing filtering within the column for the entire grid. Filtering can then be disabled on individual columns using the columnDefs.


## Property: _**enableSelection**_ (Optional)

Value: boolean (must be **true** or **false**)

This sets whether a selection (checkbox) column should be added to the grid. This selection column is used to add/remove items to the cart and as such, only entities **investigation**, **dataset** and **datafile** are supported. It only make sense to use this property for the grid in My Data tab and Browse tab.


## Property: _**columnDefs**_ (Required)

Value: array of [columnDefs](columndefs.html) objects


This defines the columns for a grid.


## Property: includes (Optional)

Value: Array of strings

For grids that display an entity, it is possible to add a field from a different entity that has a relationship to the current entity. For example, if the grid entity is investigation, it is possible to add a column to display the instrument name of the investigation. To do this, you must add an includes property and add a relationship (see [icat schema](#http://icatproject.org/mvn/site/icat/server/4.6.0-SNAPSHOT/schema.html#Facility)) using dot notation. e.g.

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

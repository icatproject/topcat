# Filter Object Properties


## Property: _**condition**_  (Required)

Value: string (Must be **contains**, **starts_with**, **ends_with**)

This sets how the term entered in a filter will be matched. **contains** matches any part of the value. **start_with** will only match if values starts with the entered term. **ends_with** will only match values that ends with the entered term.


## Property: _**placeholder**_ (Required)

Value : string

The placeholder string shown in the filter input box.


## Property: _**type**_ (Required)

Value: string (Must be **input** or **select**)

The filter input type. Input is a standard input box while select is a dropdown menu. If set to select, you must include a [selectOptions](#property-selectoptions-optional) property to list the available options.


## Property: _**selectOptions**_ (Optional)

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
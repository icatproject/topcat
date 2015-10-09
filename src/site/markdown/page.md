# Page Object Properties

## Page Object Example

In topcat.json:

```

"pages" : [
    {
        "url" : "/about",
        "stateName": "about",
        "templateTranslateName" : "PAGE.ABOUT.HTML",
        "addToNavBar": {
            "linkLabel" : "MAIN_NAVIGATION.ABOUT",
            "align" : "left"
        }
    },
    {
        "url" : "/contact",
        "stateName": "contact",
        "templateTranslateName" : "PAGE.CONTACT.HTML",
        "addToNavBar": {
            "linkLabel" : "MAIN_NAVIGATION.CONTACT",
            "align" : "left"
        }
    }
]
```

In lang.json object:

```
{
    "MAIN_NAVIGATION" : {
        "ABOUT" : "About"
        "CONTACT" : "Contact"
    },
    "PAGE" : {
        "ABOUT" : {
            "HTML" : "<h1>ABOUT</h1><p>About me</p>"
        },
        "CONTACT" : {
            "HTML" : "<h1>Contact</h1><p>Email me at mysite.ac.uk</p>"
        },
    }
}
```

## Property: _**url**_ (required)

Value: string

Example: see [Page](#Page-Object-Example) object example

The relative url of the page. It must start with a /.


## Property: _**stateName**_ (required)

Value: string

Example: see [Page](#Page-Object-Example) object example

A unique name for the page state/route. This name can be used to create hyperlinks in html pages using the attribute `ui-sref="STATE_NAME"` instead of `href=""`. e.g. `<a ui-sref="about">Link</a>` where the stateName for the page is named `about`.


## Property: _**templateTranslateName**_ (required)

Value: string

Example: see [Page](#Page-Object-Example) object example

The content of the html page must be set in the lang.json file. The value for templateTranslateName must be a property in lang.json whose value is the html content of the page.

Note that json specification does not allow new lines in values so html content must be in a single line and escaped. This [online tool](http://bernhardhaeussner.de/odd/json-escape/) can be used to do this.


## Property: _**addToNavBar**_ (Optional)

Value: object

Example: see [Page](#Page-Object-Example) object example

This property, if set, will add a navigation button on the main navigation bar.

The object has two properties:

  - **linkLabel** (string - required) - a property in lang.json that will be used as the navigation button text label
  - **align** (string - required) - whether the navigation button align to the left or right of the navigation bar. Value must be **left** or **right**
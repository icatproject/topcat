# Paging Object Properties

## Property: _**pagingType**_ (Required)

Value: string (must be _page_ or _scroll_)

The pagination type used throughout the site. Possible value are _page_ or _scroll_. _page_ uses a sequence of number to navigate between each page while _scroll_ uses a scroll bar or mouse wheel to navgiate to the next page.


## Property: _**paginationNumberOfRows**_ (Required only for pagingType of _page_)

Value: integer

The default number of rows to display per page.


## Property: _**paginationPageSizes**_ (Optional for pagingType of _page_)

Value: array of integers

This adds a dropdown menu on the paginator navigation bar so that users can select the number of rows per page. The possible number of rows per page options are the one defined in the array.


## Property: _**scrollPageSize**_ (Required for pagingType of _scroll_)

Value: integer

The number or rows to fetch at the start and when scrolling for the next page


## Property: _**scrollRowFromEnd**_ (Required for pagingType of _scroll_)

Value: Integer (Should be smaller than scrollPageSize)

This is the number of rows remaining before a fetch is made for the next page. For example, if the scrollPageSize is set to 100 and scrollRowFromEnd is 20. When you scroll to row 80, a request is made to fetch the next 100 rows. This gives the user a smoother scolling experience as the data will be preloaded.
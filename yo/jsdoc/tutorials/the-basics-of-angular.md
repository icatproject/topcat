
Angular is an MVC ([Model View Controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)) framework, so before you go any further is important you first understand this design pattern. 

## Views

A view takes the model (i.e the data) passed to it via controller, and presents it to the end user. Via Angular's two way binding mechanism the view can also alter underlying model, which in turn will update the view it self.

## Controllers

The presents the model to the view and provides behavior via methods.

## Models

Models in Angular are just simple Javascript objects e.g. Strings or Arrays. 

## Two way binding

This Angular's killer feature, this is what makes Angular a framework of choice for many people. Essentially, it is the idea that the view's inputs get bound to underlying model, such that if any changes to view is made (e.g. tying in text) the model will get updated, and if any changes to model are made the view gets updated.

## Services

Services are singletons that contain functionality you want to share between your controllers e.g. to retrieve data from a web server.

## Directives

Directives allow you extend the DOM's functionality by creating custom tags and/or attributes.

## Finding out more

You can find out more by visiting Angular's site [here](https://angularjs.org/).


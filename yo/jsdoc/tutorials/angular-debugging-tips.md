
## Interactively debugging scope objects

In AngularJS a scope object represents the application's model. They are arranged in a hierarchical fashion bound to the DOM. So essentially they represent what objects a view (i.e. a template) can see/use.

When inspecting the DOM, you can spot where and element (and it's children) has an associated scope, this is indicated by an ng-scope (css) class.

When debugging, it is often very useful to inspect these scope objects to see what the state is etc or interact with it to call methods. This can be done by first selecting an element inside the browser's body via the DOM inspector, and then running the following in the consle

    scope = angular.element($0).scope();

Note: $0 represents the currently selected element. The angular.element(nativeElement) method produces an element wrapper.

## Interactively debugging services

In order access an angular service in the browser console do the following:

    exampleService  = angular.element(document.body).injector().get('exampleService');

e.g:


    $http = angular.element(document.body).injector().get('$http');

    //we can now interact with it
    $http.get('/').then(function(result){
        console.log(result);
    });


'use strict';

window.deferredBootstrapper.bootstrap({
    element : document.body,
    module : 'angularApp',
    resolve : {
        APP_CONFIG : [ '$http', function($http) {
            return $http.get('/data/config.json');
        } ]
    }
});


// Declare app level module which depends on views, and components
angular.module('angularApp', [
  'ui.router', 'ui.router.tabs', 'ngResource', 'ui.bootstrap', 'datatables', 'datatables.scroller', 'truncate'
]).
config(function($stateProvider, $urlRouterProvider) {
    // For any unmatched url redirect to /browse
    $urlRouterProvider.otherwise('/browse');

    // Set up the states
    $stateProvider
        .state('home', {
            //url: "",
            controller: 'HomeController',
            templateUrl: 'views/home.html'
        })
        .state('home.browse', {
            url: '/browse',
            views : {
                '' : {
                    controller: 'BrowseController',
                    templateUrl: 'views/browse.html',
                },
                'searchForm@home.browse' : {
                    controller: 'SearchFormController',
                    templateUrl: 'views/search-form.html',
                },
                'browse@home.browse': {
                    controller: 'BrowseDataController',
                    templateUrl: 'views/browse-content.html',
                }
            }
        })
        .state('home.cart', {
            url: '/cart',
            templateUrl: 'views/cart.html'
        })
        .state('home.browse.meta-1', {
            url: '/meta-1?browse&pagingType&query&type&facility&startDate&endDate',
            templateUrl: 'views/meta/1.html'
        })
        .state('home.browse.meta-2', {
            url: '/meta-2?browse&pagingType&query&type&facility&startDate&endDate',
            templateUrl: 'views/meta/2.html'
        })
        .state('home.browse.meta-3', {
            url: '/meta-3?browse&pagingType&query&type&facility&startDate&endDate',
            templateUrl: 'views/meta/3.html'
        })
        .state('home.browse-dataset', {
            url: '/dataset?browseType&id',
            controller: 'BrowseDatasetController',
            templateUrl: 'views/browse.html',
            parent: 'home.browse.meta-1'

        })
        ;

}).
factory('DTLoadingTemplate', function dtLoadingTemplate() {
    return {
        html: '<img src="images/loading-background.png">'
    };
});

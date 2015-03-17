'use strict';

/**
 * deferred bootstrap to load main configuration to APP_CONFIG
 */
window.deferredBootstrapper.bootstrap({
  element : document.body,
  module : 'angularApp',
  resolve : {
    APP_CONFIG : [ '$http', function($http) {
      return $http.get('data/config.json');
    } ]
  }
});

/**
 * @ngdoc overview
 * @name angularApp
 * @description
 * # angularApp
 *
 * Main module of the application.
 */
var routerApp = angular.module('angularApp', [
  'ngResource',
  //'ngRoute',
  'ngSanitize',
  'ui.router',
  'ct.ui.router.extras.sticky',
  'ui.bootstrap',
  'datatables',
  'datatables.scroller',
  'truncate'
]);

routerApp.config(function($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/browse/facilities/meta1');
    $urlRouterProvider.when('/browse', '/browse/facilities/meta1'); //redirect TODO is this nescessary?

    $stateProvider
        .state('home', {
            abstract: true,
            //url: '',
            templateUrl: 'views/abstract-home.html',
            controller: 'HomeCtrl'
        })
        .state('home.browse', {
            abstract: true,
            url: '/browse',
            views: {
              'browse': {
                template: '<div ui-view></div>',
                //controller: 'BrowseCtrl'
              }
            },
            sticky: true,
            deepStateRedirect: true
        })
        .state('home.browse.main', {
            url: '',
            abstract: true,
            templateUrl: 'views/main-browse.html'
        })
        .state('home.browse.main.facilities', {
            abstract: true,
            url: '/facilities',
            views: {
              'search-form-view': {
                templateUrl: 'views/partial-search-form.html',
                controller: 'SearchFormCtrl as searchForm'
              },
              'browse-view': {
                templateUrl: 'views/partial-browse-panel.html',
                controller: 'BrowsePanelCtrl as browse'
              },
              'meta-view': {
                templateUrl: 'views/partial-meta-panel.html',
                //controller: 'BrowseTestCtrl as metaShared'
              }
            }
        })

        .state('home.browse.main.facilities.meta1', {
            url: '/meta1',
            views: {
              'meta1': {
                templateUrl: 'views/meta-panel/1.html',
                //controller: 'BrowseTestCtrl as meta1'
              },
            },
            sticky: true,
            deepStateRedirect: true
        })
        .state('home.browse.main.facilities.meta2', {
            url: '/meta2',
            views: {
              'meta2': {
                templateUrl: 'views/meta-panel/2.html',
                //controller: 'BrowseTestCtrl as meta2'
              },
            },
            sticky: true,
            deepStateRedirect: true
        })

        .state('home.cart', {
            url: '/cart', //?data&meta&pagingType&query&type&facility&startDate&endDate',
            views: {
              'cart': {
                templateUrl: 'views/main-cart.html',
                controller: 'CartCtrl as cart'
              }
            },
            sticky: true,
            deepStateRedirect: true
        })
        // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
        .state('about', {
            url: '/about',
            templateUrl: 'views/main-about.html'
        })
        .state('contact', {
            url: '/contact',
            templateUrl: 'views/main-contact.html'
        });

})
/*.config(function($stickyStateProvider) {
  $stickyStateProvider.enableDebug(true);
})*/
.run(['$rootScope', '$state', '$stateParams', function ($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
}]);


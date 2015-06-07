(function() {
    'use strict';

    /**
     * deferred bootstrap to load main configuration to APP_CONFIG
     */
    window.deferredBootstrapper.bootstrap({
        element : document.body,
        module : 'angularApp',
        resolve : {
            APP_CONFIG : [ '$http', function($http) {
                return $http.get('data/config-multi.json');
            } ],
            LANG : [ '$http', function($http) {
                return $http.get('languages/en.json');
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
    angular
        .module('angularApp', [
            'ngResource',
            //'ngRoute',
            'ngSanitize',
            'ui.router',
            'ct.ui.router.extras.sticky',
            //'ct.ui.router.extras.previous',
            'ui.bootstrap',
            'truncate',
            'inform',
            'inform-exception',
            'prettyBytes',
            'checklist-model',
            'ngStorage',
            'pascalprecht.translate',
            'ui.grid',
            'ui.grid.pagination',
            'ui.grid.infiniteScroll',
            'ui.grid.selection',
            'bytes'
        ])
        .constant('_', window._)
        .config(['$translateProvider', 'LANG', function($translateProvider, LANG) {
            $translateProvider.translations('en', LANG);

            $translateProvider.useStaticFilesLoader({
                prefix: '/languages/',
                suffix: '.json'
            });
            $translateProvider.preferredLanguage('en');
         }])
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.interceptors.push('HttpErrorInterceptor');
            $httpProvider.interceptors.push('ICATRequestInterceptor');
        }])
        .config(function($stateProvider, $urlRouterProvider) {
            //workaround https://github.com/angular-ui/ui-router/issues/1022
            $urlRouterProvider.otherwise(function($injector) {
              var $state = $injector.get('$state');
              $state.go('home.browse.facility');
            });

            $stateProvider
                .state('home', {
                    abstract: true,
                    //url: '',
                    templateUrl: 'views/abstract-home.html',
                    controller: 'HomeController'
                })
                .state('home.browse', {
                    abstract: true,
                    url: '/browse',
                    views: {
                      '': {
                        templateUrl: 'views/main-browse.html'
                      }
                    },
                    sticky: true,
                    deepStateRedirect: true
                })
                /*.state('home.browse.main', {
                    url: '',
                    abstract: true,
                    templateUrl: 'views/main-browse.html'
                })*/
                .state('home.browse.facility', {
                    url: '/facilities',
                    resolve: {
                        /*sessions: ['DataManager', function(DataManager){
                            return DataManager.login();
                        }],*/
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                        '' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowseFacilitiesController as vm'
                        },
                        'search-form-view': {
                            templateUrl: 'views/partial-search-form.html',
                            controller: 'SearchFormController as searchForm'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    }
                })

                .state('home.browse.facility.search', {
                    url: '^/search?data&meta&pagingType&query&type&facility&startDate&endDate',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/search-result.html'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    }
                })
                .state('home.cart', {
                    url: '/cart', //?data&meta&pagingType&query&type&facility&startDate&endDate',
                    views: {
                      'cart': {
                        templateUrl: 'views/main-cart.html',
                        controller: 'CartController as ct'
                      }
                    },
                    sticky: true,
                    deepStateRedirect: true
                })
                // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
                .state('about', {
                    /*resolve : {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },*/
                    url: '/about',
                    templateUrl: 'views/main-about.html'
                })
                .state('contact', {

                    url: '/contact',
                    templateUrl: 'views/main-contact.html'
                })
                .state('test', {
                    url: '/test',
                    templateUrl: 'views/test.html',
                    controller: 'TestController as test'
                })
                .state('login', {
                    url: '/login',
                    templateUrl: 'views/login.html',
                    controller: 'LoginController as vm'
                })
                .state('logout', {
                    url: '/logout',
                    controller: 'LogoutController'
                })
                .state('logout.facility', {
                    url: '/:facilityName',
                    controller: 'LogoutController'
                })
                ;

        })
        .config(['$logProvider', function($logProvider) {
            $logProvider.debugEnabled(true);
        }])
    /*.config(function($stickyStateProvider) {
      $stickyStateProvider.enableDebug(true);
    })*/
        .run(['SquelCustomQuery', function(SquelCustomQuery){
            SquelCustomQuery.init();
        }])
        .run(['$rootScope', '$state', '$stateParams', function ($rootScope, $state, $stateParams) {
            //make $state and $stateParams available at rootscope.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
        }])
        .run(['$rootScope', '$state', function ($rootScope, $state) {
            //watch for state change resolve authentication errors
            $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
                if (error && error.isAuthenticated === false) {
                    $state.go('login');
                }
            });

            //save last page to rootscope
            /*$rootScope.$on('$stateChangeStart', function() {
                $log.debug.log('previous state', $previousState.get() !== null ? $previousState.get().state.name : $previousState.get());
                $rootScope.previousState = $previousState.get();
            });*/

        }])
        .run(['RouteCreatorService', function(RouteCreatorService) {
            RouteCreatorService.createStates();
        }])
        .run(['$rootScope', 'Cart', function($rootScope, Cart) {
            //listen to cart change events and save the cart
            $rootScope.$on('Cart:change', function(){
                Cart.save();
            });

            Cart.init();

            if (Cart.isRestorable()) {
                Cart.restore();
            }
        }]);
})();

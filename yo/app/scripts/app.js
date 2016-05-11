(function() {
    'use strict';

    var noCacheHeaders = {
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache',
        'Expires': '0'
    };

    /**
     * deferred bootstrap to load main configuration to APP_CONFIG
     */
    window.deferredBootstrapper.bootstrap({
        element : document.documentElement,
        module : 'angularApp',
        resolve : {
            APP_CONFIG : ['$http', '$q',  function($http, $q) {
                var port = parseInt(window.location.port);
                var url;
                if(port === 10080 || port === 9000){
                    url = 'config/topcat_dev.json';
                } else {
                    url = 'config/topcat.json';
                }
                return $http({
                    url: url,
                    method: 'GET',
                    transformResponse: function (json) {
                        try {
                            return jsonlint.parse(json);
                        } catch(e){
                            alert(url + "\n\n" + e.message);
                        }
                        return {};
                    }
                }).then(function(response){
                    var config = response.data;
                    var defered = $q.defer();
                    var promises = [];
                    _.each(config.facilities, function(facility){
                        if(!facility.icatUrl){
                            promises.push($.get(facility.idsUrl + "/ids/getIcatUrl").then(function(icatUrl){
                                facility.icatUrl = icatUrl;
                            }));
                        }
                    });
                    $q.all(promises).then(function(){
                        defered.resolve(config);
                    });
                    return defered.promise;
                }).then(function(config){
                    var defered = $q.defer();
                    var promises = [];
                    _.each(config.facilities, function(facility){
                        if(!facility.authenticationTypes){
                            promises.push($.get(facility.icatUrl + "/icat/properties").then(function(properties){
                                facility.authenticationTypes = _.map(properties.authenticators, function(authenticator){
                                    return {
                                        title: authenticator.friendly || authenticator.mnemonic,
                                        plugin: authenticator.mnemonic
                                    };
                                });
                            }));
                        }
                    });
                    $q.all(promises).then(function(){
                        defered.resolve(config);
                    });
                    return defered.promise;
                });
            }],
            LANG : ['$http', function($http) {
                var url = 'languages/lang.json';
                return $http({
                    url: url,
                    method: 'GET',
                    transformResponse: function (json) {
                        try {
                            return jsonlint.parse(json);
                        } catch(e){
                            alert(url + "\n\n" + e.message);
                        }
                        return {};
                    }
                });
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
            'ngSanitize',
            'ui.router',
            'ct.ui.router.extras.sticky',
            'ui.bootstrap',
            'truncate',
            'inform',
            'inform-exception',
            'prettyBytes',
            'ngStorage',
            'pascalprecht.translate',
            'ui.grid',
            'ui.grid.pagination',
            'ui.grid.infiniteScroll',
            'ui.grid.selection',
            'ui.grid.saveState',
            'ui.grid.resizeColumns',
            'ui.grid.moveColumns',
            'bytes',
            'angularSpinner',
            'ng.deviceDetector',
            'angularMoment',
            'emguo.poller',
            'angular-bind-html-compile',
            'angular-loading-bar',
            'ipCookie',
            'bootstrap'
        ])
        .run(['APP_CONFIG', 'LANG', 'objectValidator', function(APP_CONFIG, LANG, objectValidator){
            try {
                objectValidator.createAppConfigValidator().validate(APP_CONFIG);
            } catch(e){
                alert(e);
            }
        }])
        .constant('_', window._)
        .constant('APP_CONSTANT', {
            smartClientUrl: 'https://localhost:8888'
        })
        .config(function($uibTooltipProvider){
            $uibTooltipProvider.setTriggers({'show': 'show'});
        })
        .config(['$translateProvider', 'LANG', function($translateProvider, LANG) {
            $translateProvider.translations('en', LANG);

            $translateProvider.useStaticFilesLoader({
                prefix: '/languages/',
                suffix: '.json'
            });
            $translateProvider.preferredLanguage('en');

            $translateProvider.useSanitizeValueStrategy(null);
         }])
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.interceptors.push('HttpErrorInterceptor');
        }])
        .config(['$logProvider', function($logProvider){
            $logProvider.debugEnabled(true);
        }])
        .config(function($stateProvider, $urlRouterProvider, APP_CONFIG) {

            var maintenanceMode = APP_CONFIG.site.maintenanceMode;
            if(maintenanceMode && maintenanceMode.show){
                $stateProvider.state('maintenance-mode', {
                    url: '{path:.*}',
                    views: {
                      '': {
                        templateUrl: 'views/maintenance-mode.html',
                        controller: 'MaintenanceModeController as maintenanceModeController'
                      }
                    }
                });
                return;
            }

            $stateProvider
                .state('home', {
                    abstract: true,
                    templateUrl: 'views/abstract-home.html',
                    controller: 'HomeController'
                })
                .state('home.browse', {
                    abstract: true,
                    url: '/browse',
                    views: {
                      'browse@home': {
                        templateUrl: 'views/main-browse.html'
                      }
                    }
                })

                .state('home.browse.facility', {
                    url: '/facility',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                      '': {
                        templateUrl: 'views/browse-facilities.html',
                        controller: 'BrowseFacilitiesController as browseFacilitiesController'
                      },
                      'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    }
                })

                .state('home.search', {
                    abstract: true,
                    url: '/search',
                    views: {
                        'search@home': {
                            templateUrl: 'views/main-search.html',
                            controller: 'SearchController as searchController'
                        }
                    }
                })

                .state('home.search.start', {
                    url: '/start',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                        '' : {
                            templateUrl: 'views/search-start.html'
                        },
                    }
                })

                .state('home.search.results', {
                    url: '^/search?text&startDate&endDate&parameters&samples&facilities&investigation&dataset&datafile',
                    views: {
                        '' : {
                            templateUrl: 'views/search-results.html',
                            controller: 'SearchResultsController as searchResultsController'
                        },
                        'meta-view@home.search' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    }
                })
                .state('home.my-data', {
                    url: '/my-data/:facilityName',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                        'my-data@home': {
                            templateUrl: 'views/main-my-data.html'
                        },
                        '@home.my-data': {
                            templateUrl: 'views/partial-my-data-panel.html',
                            controller: 'MyDataController as myDataController'
                        },
                        'meta-view@home.my-data' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    }
                })
                .state('home.cart', {
                    url: '/cart',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                      'cart': {
                        templateUrl: 'views/main-cart.html',
                        controller: 'CartController'
                      }
                    }
                })
                .state('home.download', {
                    url: '/download',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    },
                    views: {
                      'download': {
                        templateUrl: 'views/main-download.html',
                        controller: 'DownloadController'
                      }
                    },
                    /*sticky: true,
                    deepStateRedirect: true*/
                })
                .state('login', {
                    url: '/login',
                    templateUrl: 'views/login.html',
                    controller: 'LoginController as loginController',
                    resolve: {
                        SMARTCLIENTPING : ['SmartClientManager', function(SmartClientManager) {
                            return SmartClientManager.ping();
                        }]
                    }
                })
                .state('logout', {
                    url: '/logout',
                    controller: 'LogoutController',
                    resolve: {
                        SMARTCLIENTPING : ['SmartClientManager', function(SmartClientManager) {
                            return SmartClientManager.ping();
                        }]
                    }
                })
                .state('logout.facility', {
                    url: '/:facilityName',
                    controller: 'LogoutController',
                    resolve: {
                        SMARTCLIENTPING : ['SmartClientManager', function(SmartClientManager) {
                            return SmartClientManager.ping();
                        }]
                    }
                })
                .state('homeRoute', {
                    url: '/',
                    controller: 'HomeRouteController'
                }).state('admin', {
                    url: '/admin/:facilityName',
                    templateUrl: 'views/admin.html',
                    controller: 'AdminController as adminController'
                })
                .state('doi-redirect', {
                    url: '/doi-redirect/:facilityName/:entityType/:entityId',
                    controller: 'DoiRedirectController',
                    resolve: {
                        authenticate : ['Authenticate', function(Authenticate) {
                            return Authenticate.authenticate();
                        }]
                    }
                });
                $urlRouterProvider.otherwise('/');

        })
        .config(function (pollerConfig) {
            pollerConfig.neverOverwrite = true;
        })
        .run(['$rootScope', '$state', '$stateParams', function ($rootScope, $state, $stateParams) {
            //make $state and $stateParams available at rootscope.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
        }])
        .run(['$rootScope', '$state', '$sessionStorage', function ($rootScope, $state, $sessionStorage) {
            //store the last state
            $rootScope.$on('$stateChangeStart', function(event, toState, toParams){
                if(!toState.name.match(/^(login|logout)/)){
                    $sessionStorage.lastState = {
                        name: toState.name,
                        params: toParams
                    };
                }
            });

            //listen for state change resolve authentication errors
            $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
                if (error && error.isAuthenticated === false) {
                    $state.go('login');
                }
            });
        }])
        .run(['SmartClientPollManager', function(SmartClientPollManager) {
            //run checking of smartclient
            SmartClientPollManager.runOnStartUp();
        }])
        .run(['RouteCreatorService', 'PageCreatorService', function(RouteCreatorService, PageCreatorService) {
            PageCreatorService.createStates();
            RouteCreatorService.createStates();
        }]);
})();

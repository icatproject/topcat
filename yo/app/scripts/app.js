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
            'datatables',
            'datatables.scroller',
            'truncate',
            'inform',
            'inform-exception',
            'prettyBytes',
            'checklist-model',
            'ngStorage',
            'pascalprecht.translate'
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
        }])
        .config(function($stateProvider, $urlRouterProvider) {
            //workaround https://github.com/angular-ui/ui-router/issues/1022
            $urlRouterProvider.otherwise(function($injector) {
              var $state = $injector.get('$state');
              $state.go('home.browse.facilities');
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
                .state('home.browse.facilities', {
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
                            controller: 'BrowseFacilitiesController as browse'
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
                .state('home.browse.facilities.facility-cycle', {
                    url: '/{facilityName}/cycles',
                    views: {
                        '@home.browse.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        }
                    },
                    param: {
                        entityType : 'investigation'
                    }
                })
                .state('home.browse.facilities.facility-instrument', {
                    url: '/{facilityName}/instruments',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'instrument'
                    }
                })
                .state('home.browse.facilities.facility-investigation', {
                    url: '/{facilityName}/investigations',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'investigation'
                    }

                })
                .state('home.browse.facilities.facility-dataset', {
                    url: '/{facilityName}/datasets',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'dataset'
                    }

                })
                .state('home.browse.facilities.facility-datafile', {
                    url: '/{facilityName}/datafiles',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'datafile'
                    }
                })
                /*.state('home.browse.facilities.entitylistbyid', {
                    url: '/{facilityName}/{entityType}/{id}',
                    views: {
                        '' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    }
                })*/
                .state('home.browse.facilities.instrument-investigation', {
                    url: '/{facilityName}/instruments/{id}/investigations',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'investigation'

                    }
                })
                .state('home.browse.facilities.instrument-dataset', {
                    url: '/{facilityName}/instruments/{id}/datasets',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'dataset'
                    }
                })
                .state('home.browse.facilities.instrument-datafile', {
                    url: '/{facilityName}/instruments/{id}/datafiles',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'datafile'
                    }
                })
                .state('home.browse.facilities.investigation-dataset', {
                    url: '/{facilityName}/investigations/{id}/datasets',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'dataset'
                    }
                })
                .state('home.browse.facilities.investigation-datafile', {
                    url: '/{facilityName}/investigations/{id}/datafiles',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'datafile'
                    }
                })
                .state('home.browse.facilities.dataset-datafile', {
                    url: '/{facilityName}/datasets/{id}/datafile',
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelController as browse'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : 'datafile'
                    }
                })

                /*.state('home.browse.facilities.meta1', {
                    url: '/?meta1',
                    views: {
                        'meta1': {
                            templateUrl: 'views/meta-panel/1.html'
                        }
                    },
                    sticky: true,
                    deepStateRedirect: true
                })
                .state('home.browse.facilities.meta2', {
                    url: '/?meta2',
                    views: {
                        'meta2': {
                            templateUrl: 'views/meta-panel/2.html'
                        }
                    },
                    sticky: true,
                    deepStateRedirect: true
                })*/
                .state('home.browse.facilities.search', {
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
                        controller: 'CartController as cart'
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
                console.log('$stateChangeError called', error);
                if (error && error.isAuthenticated === false) {
                    $state.go('login');
                }
            });

            //save last page to rootscope
            /*$rootScope.$on('$stateChangeStart', function() {
                console.log('previous state', $previousState.get() !== null ? $previousState.get().state.name : $previousState.get());
                $rootScope.previousState = $previousState.get();
            });*/

        }]);
})();

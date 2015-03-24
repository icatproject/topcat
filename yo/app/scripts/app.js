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
    angular
        .module('angularApp', [
            'ngResource',
            //'ngRoute',
            'ngSanitize',
            'ui.router',
            'ct.ui.router.extras.sticky',
            'ui.bootstrap',
            'datatables',
            'datatables.scroller',
            'truncate',
            'inform',
            'inform-exception',
            'prettyBytes'
        ])
        .constant('_', window._)
        .config(function($stateProvider, $urlRouterProvider) {

            $urlRouterProvider.otherwise('/browse/facilities/meta1');
            $urlRouterProvider.when('/browse', '/browse/facilities/meta1'); //redirect TODO is this nescessary?

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
                      'browse': {
                        template: '<div ui-view></div>'
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
                        '': {
                            template: '<div ui-view></div>'
                        },
                        'search-form-view': {
                            templateUrl: 'views/partial-search-form.html',
                            controller: 'SearchFormController as searchForm'
                        },
                        'meta-view': {
                            templateUrl: 'views/partial-meta-panel.html'
                        }
                    }
                })
                .state('home.browse.main.facilities.facility-cycle', {
                    url: '/{server}/{facility}/cycles',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        entityType : 'investigation'
                    }
                })
                .state('home.browse.main.facilities.facility-instrument', {
                    url: '/{server}/{facility}/instruments',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        entityType : 'instrument'
                    }
                })
                .state('home.browse.main.facilities.facility-investigation', {
                    url: '/{server}/{facility}/investigations',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        entityType : 'investigation'
                    }

                })
                .state('home.browse.main.facilities.facility-dataset', {
                    url: '/{server}/{facility}/datasets',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        entityType : 'dataset'

                    }

                })
                .state('home.browse.main.facilities.facility-datafile', {
                    url: '/{server}/{facility}/datafiles',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        entityType : 'datafile'
                    }
                })
                /*.state('home.browse.main.facilities.entitylistbyid', {
                    url: '/{facility}/{entityType}/{id}',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    }
                })*/
                .state('home.browse.main.facilities.instrument-investigation', {
                    url: '/{server}/{facility}/instruments/{id}/investigations',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        idType : 'instrument',
                        entityType : 'investigation'

                    }
                })
                .state('home.browse.main.facilities.investigation-dataset', {
                    url: '/{server}/{facility}/investigations/{id}/datasets',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        idType : 'investigation',
                        entityType : 'dataset'
                    }
                })
                .state('home.browse.main.facilities.dataset-datafile', {
                    url: '/{server}/{facility}/datasets/{id}/datafile',
                    views: {
                        '@home.browse.main.facilities' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    param: {
                        idType : 'dataset',
                        entityType : 'datafile'
                    }
                })

                .state('home.browse.main.facilities.meta1', {
                    url: '/meta1',
                    views: {
                        'meta1': {
                            templateUrl: 'views/meta-panel/1.html'
                        },
                        '' : {
                            templateUrl: 'views/partial-browse-panel.html',
                            controller: 'BrowsePanelContoller as browse'
                        }
                    },
                    sticky: true,
                    deepStateRedirect: true
                })
                .state('home.browse.main.facilities.meta2', {
                    url: '/meta2',
                    views: {
                        'meta2': {
                            templateUrl: 'views/meta-panel/2.html'
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
                        controller: 'CartController as cart'
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
            $rootScope._ = window._;
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
    }]);
})();

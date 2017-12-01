(function() {
    'use strict';

    var plugins = [];
    var reachablePluginsLength;
    window.registerTopcatPlugin = function(fn){
        var scripts = document.getElementsByTagName('script');
        var index = scripts.length - 1;
        var pluginUrl = scripts[index].src.replace(/scripts\/plugin\.js/, '');
        plugins.push(fn(pluginUrl));
    };

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
        module : 'topcat',
        resolve : {
            APP_CONFIG: function($http, $q, $timeout) {
                var port = parseInt(window.location.port);
                var url;
                if(port === 10080 || port === 9000 || port === 9876){
                    url = './config/topcat_dev.json';
                } else {
                    url = './config/topcat.json';
                }
                return $http({
                    url: url,
                    method: 'GET',
                    transformResponse: function (json) {
                        try {
                            return jsonlint.parse(json);
                        } catch(e){
                            alert("Invalid topcat.json\n\n" + e.message);
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
                            }, function(response){
                                var msg = (response?(response.responseText?response.responseText:'no text'):'no response');
                                console.log('GET ' + facility.idsUrl + '/ids/getIcatUrl failed with ' + msg);
                                alert("Could not aquire icat url from " + facility.idsUrl + "/ids/getIcatUrl - skipping facility " + facility.name);
                                config.facilities.splice(config.facilities.indexOf(facility), 1);
                                if( config.facilities.length == 0 ){
                                    alert("No reachable facilities found");
                                } else {
                                    // Return a new promise to treat this as a success
                                    return $.Deferred().resolve({}).promise();
                                }
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
                            }, function(response){
                                var msg = (response?(response.responseText?response.responseText:'no text'):'no response');
                                console.log('GET ' + facility.icatUrl + '/icat/properties failed with ' + msg);
                                alert("Could not get authenticators list from " + facility.icatUrl + "/icat/properties - skipping facility " + facility.name);
                                config.facilities.splice(config.facilities.indexOf(facility), 1);
                                if( config.facilities.length == 0 ){
                                    alert("No reachable facilities found");
                                } else {
                                    // Return a new promise to treat this as a success
                                    return $.Deferred().resolve({}).promise();
                                }
                            }));
                        }
                    });
                    $q.all(promises).then(function(){
                        defered.resolve(config);
                    });
                    return defered.promise;
                }).then(function(config){
                    var topcatUrl = config.site.topcatUrl || window.location.href.replace(/^(https{0,1}:\/\/[^\/]+).*$/, '$1');                    
                    return $http.get(topcatUrl + '/topcat/confVars/maintenanceMode').then(function(response){
                        try {
                            config.site.maintenanceMode = JSON.parse(response.data.value);
                        } catch(e){
                            config.site.maintenanceMode = {show: false, message: ""};
                        }
                        return config;
                    });
                }).then(function(config){
                    var defered = $q.defer();
                    reachablePluginsLength = config.plugins ? config.plugins.length : 0;
                    
                    if(config.plugins && config.plugins.length > 0){
                        _.each(config.plugins, function(pluginUrl){
                            var src = pluginUrl + "/scripts/plugin.js";
                            $http.get(src).then(function(){
                                $(document.body).append($("<script>").attr('src', src));
                            }, function(){
                                console.log(src + " is unreachable");
                                reachablePluginsLength--;
                            });
                        });
                        waitForPlugins();
                    } else {
                        defered.resolve(config);
                    }

                    var pluginScriptCount = 0;

                    function waitForPlugins(){
                        if(plugins.length == reachablePluginsLength){
                            pluginScriptRegisteryCounter = 0;

                            _.each(plugins, function(plugin){

                                if(plugin.stylesheets){
                                    _.each(plugin.stylesheets, function(stylesheetUrl){
                                        $(document.body).append($("<link>").attr({rel: "stylesheet", href: stylesheetUrl}));
                                    });
                                }
                                if(plugin.scripts){
                                    _.each(plugin.scripts, function(scriptUrl){
                                        if(scriptUrl instanceof Array) scriptUrl = scriptUrl[0];
                                        $(document.body).append($("<script>").attr('src', scriptUrl));
                                        pluginScriptCount++;
                                    });
                                }
                                
                                waitForPluginScripts();
                            });
                            
                        } else {
                            $timeout(waitForPlugins, 50);
                        }
                    }

                    function waitForPluginScripts(){
                        if(pluginScriptRegisteryCounter == pluginScriptCount){
                            defered.resolve(config);
                        } else {
                            $timeout(waitForPluginScripts, 50);
                        }
                    }

                    return defered.promise;
                });
            },
            LANG: function($http) {
                var url = './languages/lang.json';
                return $http({
                    url: url,
                    method: 'GET',
                    transformResponse: function (json) {
                        try {
                            return jsonlint.parse(json);
                        } catch(e){
                            alert("Invalid lang.json:\n\n" + e.message);
                        }
                        return {};
                    }
                });
            }
        }
    });

    var app = angular.module('topcat', [
        'ngResource',
        'ngSanitize',
        'ui.router',
        'ct.ui.router.extras.sticky',
        'ui.bootstrap',
        'truncate',
        'inform',
        'inform-exception',
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
        'ng.deviceDetector',
        'angularMoment',
        'emguo.poller',
        'angular-bind-html-compile',
        'angular-loading-bar',
        'ipCookie',
        'bootstrap'
    ]);

    app.factory('plugins', function(){
        return plugins;
    });

    var pluginScriptRegisteryCounter = 0;
    (function(){
        var isMethodCalled = false;

        _.each(app, function(method, methodName){
            if(typeof method == 'function'){
                app[methodName] = function(){
                    if(!isMethodCalled){
                        isMethodCalled = true;
                        pluginScriptRegisteryCounter++;
                    }
                    var out = method.apply(this, arguments);
                    isMethodCalled = false;
                    return out;
                };
            }
        });

        function checkIfScriptsHaveLoaded(){
            if(reachablePluginsLength && plugins.length == reachablePluginsLength){
                var allHaveLoaded = true;

                _.each(plugins, function(plugin){
                    _.each(plugin.scripts, function(script){
                        if(script instanceof Array){
                            var hasLoaded = script[1];
                            if(!hasLoaded()){
                                allHaveLoaded = false;
                            }
                        }
                    });
                });

                if(allHaveLoaded){
                    _.each(plugins, function(plugin){
                        _.each(plugin.scripts, function(script){
                            if(script instanceof Array) pluginScriptRegisteryCounter++; 
                        });
                    });
                } else {
                    setTimeout(checkIfScriptsHaveLoaded, 50);
                }
            } else {
                setTimeout(checkIfScriptsHaveLoaded, 50);
            }
        }

        checkIfScriptsHaveLoaded();

    })();
    
    app.run(function(APP_CONFIG, LANG, objectValidator){
        try {
            var pluginSchemas = [];

            _.each(plugins, function(plugin){
                if(plugin.configSchema) pluginSchemas.push(plugin.configSchema);
            });

            objectValidator.createAppConfigValidator(pluginSchemas).validate(APP_CONFIG);
        } catch(e){
            alert("Invalid topcat.json: \n\n" + e);
        }
    });

    
    app.config(function($uibTooltipProvider){
        $uibTooltipProvider.setTriggers({'show': 'show'});
    });

    app.config(function($translateProvider, LANG) {
        $translateProvider.translations('en', LANG);

        $translateProvider.useStaticFilesLoader({
            prefix: '/languages/',
            suffix: '.json'
        });
        $translateProvider.preferredLanguage('en');

        $translateProvider.useSanitizeValueStrategy(null);
    });
    
    app.config(function($httpProvider) {
        $httpProvider.interceptors.push('HttpErrorInterceptor');
    });

    app.config(function($logProvider){
        $logProvider.debugEnabled(true);
    });

    app.config(function($stateProvider, $urlRouterProvider, APP_CONFIG){
        $stateProvider.state('login-admin', {
            url: '/login-admin',
            templateUrl: 'views/login.html',
            controller: 'LoginController as loginController'
        })


        var maintenanceMode = APP_CONFIG.site.maintenanceMode;
        var cookies = {};
        _.each(document.cookie.split(/;\s*/), function(pair){
            pair = pair.split(/=/);
            cookies[pair[0]] = pair[1];
        });
        if(maintenanceMode && maintenanceMode.show){
            if(cookies['isAdmin'] == 'true'){
                maintenanceMode.show = false
            } else {
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
        }

        $stateProvider
            .state('home', {
                abstract: true,
                templateUrl: 'views/abstract-home.html',
                controller: 'HomeController as homeController'
            })
            .state('home.browse', {
                abstract: true,
                url: '/browse',
                views: {
                  '': {
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
                    '': {
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
                    '': {
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
            .state('login', {
                url: '/login',
                templateUrl: 'views/login.html',
                controller: 'LoginController as loginController'
            })
            .state('logout', {
                url: '/logout',
                controller: 'LogoutController'
            })
            .state('logout.facility', {
                url: '/:facilityName',
                controller: 'LogoutController'
            })
            .state('homeRoute', {
                url: '/',
                controller: 'HomeRouteController'
            }).state('admin', {
                abstract: true,
                url: '/admin/',
                templateUrl: 'views/admin.html',
                controller: 'AdminController as adminController'
            })
            .state('admin.downloads', {
                url: 'downloads/:facilityName',
                templateUrl: 'views/admin-downloads.html',
                controller: 'AdminDownloadsController as adminDownloadsController'
            })
            .state('admin.messages', {
                url: 'messages',
                templateUrl: 'views/admin-messages.html',
                controller: 'AdminMessagesController as adminMessagesController'
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

    });

    app.config(function($httpProvider) {
        $httpProvider.interceptors.push(function($rootScope, $q) {
          return {
           'request': function(config) {
                $rootScope.requestCounter++;
                return config;
            },

            'requestError': function(config) {
                $rootScope.requestCounter--;
                return config;
            },

            'response': function(response) {
                $rootScope.requestCounter--;
                return response;
            },

            'responseError': function(rejection) {
                $rootScope.requestCounter--;
                return $q.reject(rejection);
            }
          };
        });
    });

    //add plugin hosts to cross site scripting white list
    app.config(function($sceDelegateProvider, APP_CONFIG) {
        var whiteList = ['self'];

        if(APP_CONFIG.plugins){
            _.each(APP_CONFIG.plugins, function(plugin){
                var matches = plugin.match(/^(https{0,1}:\/\/[^\/]+)/);
                if(matches){
                    whiteList.push(matches[1] + "**");
                }
            })
        }

        $sceDelegateProvider.resourceUrlWhitelist(whiteList);
    });

    app.run(function ($rootScope, $state, $stateParams) {
        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;
    });

    app.run(function ($rootScope, $state, $sessionStorage) {
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
    });

    app.run(function(RouteCreatorService, PageCreatorService) {
        PageCreatorService.createStates();
        RouteCreatorService.createStates();
    });

    app.run(function($injector, $timeout){
        _.each(plugins, function(plugin){
            if(plugin.setup) $injector.invoke(plugin.setup);
        });

        $timeout(function(){
            $('body').append($('<span>').addClass('is-init'));
        }, 100);

    });

})();

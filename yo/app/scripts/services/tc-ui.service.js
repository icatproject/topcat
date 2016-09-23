

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUi', function($rootScope, helpers, RuntimeStatesProvider){

    	this.create = function(tc){
    		return new Ui(tc);
    	};

        function Ui(tc){

            var mainTabs = [];

            this.mainTabs = function(){
                return mainTabs;
            };

            this.registerMainTab = helpers.overload({
                'string, string, object': function(name, view, options){
                    mainTabs.push({name: name, view: view, options: options});

                    var url = '/' + name;
                    if(options.multiFacility) url += "/:facilityName";

                    var state = {
                      url: url,
                      resolve: {
                        authenticate: function(Authenticate){
                            return Authenticate.authenticate();
                        }
                      },
                      params: options.params,
                      views: {
                        '': {
                            templateUrl: view,
                            controller: options.controller
                        }
                      }
                    };

                    RuntimeStatesProvider.addState('home.' + name, state);
                },
                'string, string': function(name, view){
                    this.registerMainTab(name, view, {});
                }
            });

            var cartButtons = [];

            this.cartButtons = function(){ return cartButtons; };

            this.registerCartButton = helpers.overload({
                'string, object, function': function(name, options, click){
                    cartButtons.push({name: name, options: options, click: click});
                },
                'string, function': function(name, click){
                    return this.registerCartButton(name,  {}, click);
                }
            });

            var entityActionButtons = [];

            this.entityActionButtons = function(){ return entityActionButtons; };

            this.registerEntityActionButton = helpers.overload({
                'string, object, function': function(name, options, click){
                    options.entityTypes = options.entityTypes || ['investigation', 'dataset', 'datafile'];
                    entityActionButtons.push({name: name, options: options, click: click});
                },
                'string, function': function(name, click){
                    return this.registerEntityActionButton(name,  {}, click);
                }
            });


            var pages = [];

            this.pages = function(){ return pages; };
            
            this.registerPage = helpers.overload({
                'string, string, object': function(name, view, options){
                    pages.push({name: name, view: view, options: options});


                    var state = {
                      url: options.url || ('/' + name),
                      params: options.params,
                      views: {
                        '': {
                            templateUrl: view,
                            controller: options.controller
                        }
                      }
                    };

                    if(options.authenticate){
                        state['resolve'] = {
                            authenticate: function(Authenticate){
                                return Authenticate.authenticate();
                            }
                        };
                    }

                    RuntimeStatesProvider.addState(name, state);

                },
                'string, string': function(name, view){
                    this.registerPage(name, view, {});
                }
            });

            var externalGridFilters = {};

            this.externalGridFilters = function(){ return externalGridFilters; };

            this.registerExternalGridFilter = helpers.overload({
                'array, object': function(states, options){
                    _.each(states, function(state){
                        externalGridFilters[state] = externalGridFilters[state] || [];
                        externalGridFilters[state].push({
                            template: options.template || '',
                            setup: options.setup || function(){},
                            modifyQuery: options.modifyQuery || function(){}
                        });
                    });
                }
            });

        }

    });

})();

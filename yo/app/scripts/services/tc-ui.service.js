

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
                      views: {}
                    };

                    state.views[name + '@home'] = {
                        templateUrl: view,
                        controller: options.controller
                    };

                    RuntimeStatesProvider.addState('home.' + name, state);

                    $rootScope.$broadcast('maintab:change');
                },
                'string, string': function(name, view){
                    this.registerMainTab(name, view, {});
                }
            });

        }

    });

})();

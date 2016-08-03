

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUi', function(helpers){

    	this.create = function(tc){
    		return new Ui(tc);
    	};

        function Ui(tc){

            var mainTabs = [];

            this.registerMainTab = helpers.overload({
                'string, string, object': function(name, view, options){
                    
                },
                'string, string': function(name, view){
                    this.registerMainTab(name, view, {});
                }
            });

        }

    });

})();

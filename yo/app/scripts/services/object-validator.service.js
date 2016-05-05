

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('objectValidator', function(){
        var objectValidator = this;

    	this.create = function(schema){
    		return new ObjectValidator(schema);
    	};

    	function ObjectValidator(schema){
            var attributes = {};
            var mandatory = true;
            var type = 'object';
            var description = '';

            this.attribute = function(name, schema){
                attributes[name] = objectValidator.create(schema);
            };

            this.optional = function(){
                mandatory = false;
            };

            this.type = function(_type){
                type = _type;
            };

            this.description = function(_description){
                description = _description;
            };

            this.validate = function(o){
                
            };

        }


	});

})();

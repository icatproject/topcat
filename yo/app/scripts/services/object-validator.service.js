

(function() {
    'use strict';

    angular.module('bootstrap', []).service('objectValidator', function(){
        var objectValidator = this;

    	this.create = function(schema){
    		return new ObjectValidator(schema);
    	};

    	function ObjectValidator(schema, path){
            var attributes = {};
            var mandatory = function(){ return true };
            var type = 'object';
            if(!path) path = [];

            this.attribute = function(name, schema, condition){
                attributes[name] = schema;
            };

            this.mandatory = function(arg1){
                if(arg1 instanceof Function){
                    mandatory = arg1;
                } else {
                    mandatory = function(){ return arg1; };
                }
            };

            this.type = function(_type){
                type = _type;
            };

            this.validate = function(o, parent){
                if(o === undefined){
                    if(!mandatory(parent)) return;
                    throw "object > " + path.join(' > ') + ": this is a mandatory attribute";
                }

                var oType = typeof o;
                if(o instanceof Array) oType = 'array';               
                if(!oType.match(new RegExp('^' + type + '$'))){
                    throw "object > " + path.join(' > ') + ": invalid type expected '" + type + "' got '" + oType + "'";
                }

                if(oType == 'array'){
                    var schema = attributes['*'];
                    if(schema){
                        _.each(o, function(item, name){
                            path.push(name);
                            (new ObjectValidator(schema, path)).validate(item, o);
                            path.pop();
                        });
                    }
                } else {
                    _.each(attributes, function(schema, name){
                        path.push(name);
                        (new ObjectValidator(schema, path)).validate(o[name], o);
                        path.pop();
                    });
                }

                if(oType == 'object'){
                    _.each(o, function(value, name){
                        if(attributes[name] === undefined){
                            path.push(name);
                            throw "object > " + path.join(' > ') + ": unexpected attribute";
                        }
                    });
                }

            };

            schema.call(this);
        }

        this.createAppConfigValidator = function(){
            return this.create(function(){
                this.attribute('site', function(){
                    this.attribute('topcatUrl', function(){ this.type('string'); this.mandatory(false); });
                    this.attribute('home', function(){ this.type('string'); });
                    this.attribute('enableEuCookieLaw', function(){ this.type('boolean'); });
                    this.attribute('paging', function(){ 
                        this.attribute('pagingType', function(){ this.type('string'); });
                        this.attribute('paginationNumberOfRows', function(){ this.type('number'); });
                        this.attribute('paginationPageSizes', function(){
                            this.type('array');
                            this.mandatory(function(o){ return o.pagingType == 'page'; });
                            this.attribute('*', function(){
                                this.type('number');
                            });
                        });
                        this.attribute('scrollPageSize', function(){ this.type('number'); });
                        this.attribute('scrollRowFromEnd', function(){ this.type('number'); });
                    });
                    this.attribute('breadcrumb', function(){
                        this.attribute('maxTitleLength', function(){ this.type('number'); });
                    });
                    this.attribute('serviceStatus', function(){
                        this.mandatory(false);
                        this.attribute('show', function(){ this.type('boolean'); });
                        this.attribute('message', function(){ this.type('string'); });
                    });
                    this.attribute('maintenanceMode', function(){
                        this.mandatory(false);
                        this.attribute('show', function(){ this.type('boolean'); });
                        this.attribute('message', function(){ this.type('string'); });
                    });
                    this.attribute('search', function(){
                        this.attribute('enableParameters', function(){ this.type('boolean'); this.mandatory(false); });
                        this.attribute('enableSamples', function(){ this.type('boolean'); this.mandatory(false); });
                        this.attribute('gridOptions', function(){
                            var that = this;
                            _.each(['investigation', 'dataset', 'datafile'], function(entityType){
                                that.attribute(entityType, function(){
                                    this.attribute('enableSelection', function(){ this.type('boolean'); this.mandatory(false); });
                                    this.attribute('columnDefs', function(){
                                        this.type('array');
                                        this.attribute('*', function(){
                                            this.attribute('field', function(){ this.type("string"); });
                                            this.attribute('link', function(){ this.type('boolean|string'); this.mandatory(false); });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
                this.attribute('facilities', function(){

                });
            });
        }

	});

})();

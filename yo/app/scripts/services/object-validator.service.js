

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
                    throw path.join(' > ') + ": this is a mandatory attribute";
                }

                var oType = typeof o;
                if(o instanceof Array) oType = 'array';               
                if(!oType.match(new RegExp('^' + type + '$'))){
                    throw path.join(' > ') + ": invalid type expected '" + type + "' got '" + oType + "'";
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
                            throw path.join(' > ') + ": unexpected attribute";
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
                                            this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                        });
                                    });
                                });
                            });
                        });
                    });
                    this.attribute('browse', function(){
                        this.attribute('gridOptions', function(){
                            this.attribute('columnDefs', function(){
                                this.type('array');
                                this.attribute('*', function(){
                                    this.attribute('field', function(){ this.type("string"); });
                                    this.attribute('link', function(){ this.type('boolean'); this.mandatory(false); });
                                    this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                });
                            });
                        });
                        this.attribute('metaTabs', function(){
                            this.type('array');
                            this.mandatory(false);
                            this.attribute('*', function(){
                                this.attribute('title', function(){ this.type("string"); });
                                this.attribute('items', function(){
                                    this.type('array');
                                    this.attribute('*', function(){
                                        this.attribute('field', function(){ this.type("string"); });
                                        this.attribute('label', function(){ this.type("string"); this.mandatory(false); });
                                        this.attribute('template', function(){ this.type("string"); this.mandatory(false); });
                                    });
                                });
                            });
                        });
                    });
                    this.attribute('cart', function(){
                        this.attribute('gridOptions', function(){
                            this.attribute('columnDefs', function(){
                                this.type('array');
                                this.attribute('*', function(){
                                    this.attribute('field', function(){ this.type("string"); });
                                    this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                });
                            });
                        });
                    });
                    this.attribute('myDownloads', function(){
                        this.attribute('gridOptions', function(){
                            this.attribute('columnDefs', function(){
                                this.type('array');
                                this.attribute('*', function(){
                                    this.attribute('title', function(){ this.type("string"); this.mandatory(false); });
                                    this.attribute('field', function(){ this.type("string"); });
                                    this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                });
                            });
                        });
                    });
                    this.attribute('pages', function(){
                        this.type('array');
                        this.attribute('*', function(){
                            this.attribute('url', function(){ this.type("string"); });
                            this.attribute('stateName', function(){ this.type("string"); });
                            this.attribute('addToNavBar', function(){ 
                                this.mandatory(false);
                                this.attribute('linkLabel', function(){ this.type("string"); });
                                this.attribute('align', function(){ this.type("string"); });
                            });
                        });
                    });
                });
                this.attribute('facilities', function(){
                    this.type('array');
                    this.attribute('*', function(){
                        this.attribute('title', function(){ this.type("string"); });
                        this.attribute('name', function(){ this.type("string"); });
                        this.attribute('idsUrl', function(){ this.type("string"); });
                        this.attribute('icatUrl', function(){ this.type("string"); this.mandatory(false); });
                        this.attribute('hierarchy', function(){
                            this.type('array');
                            this.attribute('*', function(){
                                this.type("string");
                            });
                        });
                        this.attribute('authenticationTypes', function(){
                            this.type('array');
                            this.attribute('*', function(){
                                this.attribute('title', function(){ this.type("string"); });
                                this.attribute('plugin', function(){ this.type("string"); });
                                this.attribute('casUrl', function(){ this.type("string"); this.mandatory(function(o){
                                    return o.plugin == 'cas';
                                }); });
                            });
                        });
                        this.attribute('downloadTransportTypes', function(){
                            this.type('array');
                            this.mandatory(false);
                            this.attribute('*', function(){
                                this.attribute('type', function(){ this.type("string"); });
                                this.attribute('idsUrl', function(){ this.type("string"); });
                            });
                        });
                        this.attribute('admin', function(){
                            this.attribute('gridOptions', function(){
                                this.attribute('columnDefs', function(){
                                    this.type('array');
                                    this.attribute('*', function(){
                                        this.attribute('title', function(){ this.type("string"); this.mandatory(false); });
                                        this.attribute('field', function(){ this.type("string"); });
                                        this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                    });
                                });
                            });
                        });
                        this.attribute('myData', function(){
                            this.attribute('entityType', function(){ this.type('string'); });
                            this.attribute('gridOptions', function(){
                                this.attribute('enableSelection', function(){ this.type('boolean'); this.mandatory(false); });
                                this.attribute('columnDefs', function(){
                                    this.type('array');
                                    this.attribute('*', function(){
                                        this.attribute('title', function(){ this.type("string"); this.mandatory(false); });
                                        this.attribute('field', function(){ this.type("string"); });
                                        this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                        this.attribute('jpqlFilter', function(){ this.type('string'); this.mandatory(false); });
                                        this.attribute('jpqlSort', function(){ this.type('string'); this.mandatory(false); });
                                        this.attribute('link', function(){ this.type('boolean|string'); this.mandatory(false); });
                                        this.attribute('where', function(){ this.type('string'); this.mandatory(false); });
                                        this.attribute('excludeFuture', function(){ this.type('boolean'); this.mandatory(false); });
                                        this.attribute('sort', function(){
                                            this.mandatory(false);
                                            this.attribute('direction', function(){ this.type("string"); });
                                            this.attribute('priority', function(){ this.type("number"); this.mandatory(false); });
                                        });
                                    });
                                });
                            });
                        });
                        this.attribute('browse', function(){
                            var that = this;
                            _.each(["instrument", "facilityCycle", "investigation", "proposal", "dataset", "datafile"], function(entityType){
                                that.attribute(entityType, function(){
                                    this.mandatory(false);
                                    this.attribute('gridOptions', function(){
                                        if(entityType == 'investigation' || entityType == 'dataset' || entityType == 'datafile'){
                                            this.attribute('enableSelection', function(){ this.type('boolean'); this.mandatory(false); });
                                        }
                                        if(entityType == 'datafile'){
                                            this.attribute('enableDownload', function(){ this.type('boolean'); this.mandatory(false); });
                                        }
                                        this.attribute('columnDefs', function(){
                                            this.type('array');
                                            this.attribute('*', function(){
                                                this.attribute('title', function(){ this.type("string"); this.mandatory(false); });
                                                this.attribute('field', function(){ this.type("string"); });
                                                this.attribute('cellTemplate', function(){ this.type('string'); this.mandatory(false); });
                                                this.attribute('jpqlFilter', function(){ this.type('string'); this.mandatory(false); });
                                                this.attribute('jpqlSort', function(){ this.type('string'); this.mandatory(false); });
                                                this.attribute('link', function(){ this.type('boolean|string'); this.mandatory(false); });
                                                this.attribute('where', function(){ this.type('string'); this.mandatory(false); });
                                                this.attribute('excludeFuture', function(){ this.type('boolean'); this.mandatory(false); });
                                                this.attribute('breadcrumb', function(){ this.type('boolean'); this.mandatory(false); });
                                                this.attribute('breadcrumbTemplate', function(){ this.type('string'); this.mandatory(false); });
                                                this.attribute('sort', function(){
                                                    this.mandatory(false);
                                                    this.attribute('direction', function(){ this.type("string"); });
                                                    this.attribute('priority', function(){ this.type("number"); this.mandatory(false); });
                                                });
                                            });
                                        });
                                    });
                                    this.attribute('metaTabs', function(){
                                        this.type('array');
                                        this.mandatory(false);
                                        this.attribute('*', function(){
                                            this.attribute('title', function(){ this.type("string"); });
                                            this.attribute('items', function(){
                                                this.type('array');
                                                this.attribute('*', function(){
                                                    this.attribute('field', function(){ this.type("string"); });
                                                    this.attribute('label', function(){ this.type("string"); this.mandatory(false); });
                                                    this.attribute('template', function(){ this.type("string"); this.mandatory(false); });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });

                    });
                });
            });
        }

	});

})();

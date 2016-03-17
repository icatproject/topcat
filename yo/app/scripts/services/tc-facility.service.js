

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcFacility', function($sessionStorage, helpers, tcIcat, tcIds, tcUser, tcAdmin, icatSchema, APP_CONFIG){

    	this.create = function(tc, facilityName, APP_CONFIG){
    		return new Facility(tc, facilityName);
    	};

        function Facility(tc, facilityName){
            var icat;
            var ids;
            var admin;
            var user;
            
            this.config = function(){
                var out = APP_CONFIG.facilities[facilityName];
                var sessions = $sessionStorage.sessions || {};
                var facilityId = (sessions[facilityName] || {}).facilityId;

                if(facilityId) out.facilityId = facilityId;
                _.each(out.browse, function(conf, entityType){
                    _.each(conf.gridOptions.columnDefs, function(columnDef){
                        var matches;
                        var field = columnDef.field;
                        if(matches = columnDef.field.replace(/\|.+$/, '').match(/^([^\[\]]+).*?\.([^\.\[\]]+)$/)){
                            var variableName = matches[1];
                            entityType = icatSchema.variables[variableName];
                            if(!entityType){
                                console.error("Unknown variableName: " + variableName, columnDef)
                            }
                            entityType = helpers.uncapitalize(entityType);
                            field = matches[2];
                        }
                        var entitySchema = icatSchema.entities[helpers.capitalize(entityType)];
                        var type = entitySchema.fields[field];
                        if(type){
                            if(!columnDef.type) columnDef.type = type;
                            type = columnDef.type;

                            if(!columnDef.filter){
                                if(type == 'string'){
                                    columnDef.filter = {
                                        "condition": "contains",
                                        "placeholder": "Containing...",
                                        "type": "input"
                                    }
                                }
                            }
                            if(!columnDef.filters){
                                if(type == 'date'){
                                    columnDef.filters = [
                                        {
                                            "placeholder": "From...",
                                            "type": "input"
                                        },
                                        {
                                            "placeholder": "To...",
                                            "type": "input"
                                        }
                                    ];
                                }
                            }
                            if(!columnDef.cellFilter){
                                if(field.match(/Date$/)){
                                    columnDef.cellFilter = "date: 'yyyy-MM-dd'"
                                } else {
                                    columnDef.cellFilter = "date: 'yyyy-MM-dd HH:mm:ss'"
                                }
                            }

                            if(!columnDef.title){
                                var entityTypeNamespace = entityType.replace(/([A-Z])/, '_$1').toUpperCase();
                                var fieldNamespace = field.replace(/([A-Z])/, '_$1').toUpperCase();
                                columnDef.title = 'BROWSE.COLUMN.' + entityTypeNamespace + '.' + fieldNamespace;
                            }
                        }
                    });
                });
                console.log(out);
                return out; 
            }

            this.tc = function(){
                return tc;
            };

            this.icat = function(){
                if(!icat) icat = tcIcat.create(this);
                return icat;
            };

            this.ids = function(){
                if(!ids) ids = tcIds.create(this);
                return ids;
            };

            this.admin = function(){
                if(!admin) admin = tcAdmin.create(this);
                return admin;
            }

            this.user = function(){
                if(!user) user = tcUser.create(this);
                return user;
            }

        }

	});

})();

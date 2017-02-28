


(function() {
    'use strict';

    angular.module('bootstrap', []).service('objectValidator', function(){
        var objectValidator = this;

        this.create = function(schema){
            return new ObjectValidator(schema);
        };

        function ObjectValidator(schema){

            this.validate = function(value, parent, currentSchema, path){
                var that = this;

                if(!parent){
                    currentSchema = schema;
                    path = [];
                }

                if(currentSchema === undefined){
                    throw path.join(' > ') + ": unexpected attribute";
                }

                var type = currentSchema._type || 'object';
                var mandatory = currentSchema._mandatory === undefined ? true : currentSchema._mandatory;
                if(typeof mandatory == 'function') mandatory = mandatory(parent);
                
                if(mandatory && value === undefined){
                    throw path.join(' > ') + ": this is a mandatory attribute";
                }

                if(value !== undefined){
                    var valueType = typeof value;
                    if(value instanceof Array) valueType = 'array';               
                    if(!valueType.match(new RegExp('^' + type + '$'))){
                        throw path.join(' > ') + ": invalid type expected '" + type + "' got '" + valueType + "'";
                    }

                    if(type == 'array' || type == 'object'){
                        var attributes = [];
                        _.each(value, function(value, name){ attributes.push(name); });
                        _.each(currentSchema, function(value, name){ if(!name.match(/^_/)) attributes.push(name); });

                        _.each(attributes, function(name){
                            path.push(name);
                            that.validate(value[name], value, type == 'array' ? currentSchema['_item'] : currentSchema[name], path);
                            path.pop();
                        });
                    }
                }
            }

        };


        this.createAppConfigValidator = function(pluginSchemas){
            var schema = {
                site: {
                    topcatUrl: { _type: 'string', _mandatory: false },
                    home: { _type: 'string' },
                    enableEuCookieLaw: { _type: 'boolean' },
                    paging: { 
                        pagingType: { _type: 'string' },
                        paginationNumberOfRows: { _type: 'number' },
                        paginationPageSizes: {
                            _type: 'array',
                            _mandatory: function(o){ return o.pagingType == 'page';  },
                            _item: {
                                _type: 'number'
                             }
                        },
                        scrollPageSize: { _type: 'number' },
                        scrollRowFromEnd: { _type: 'number' }
                    },
                    breadcrumb: {
                        maxTitleLength: { _type: 'number' }
                    },
                    serviceStatus: {
                        _mandatory: false,
                        show: { _type: 'boolean' },
                        message: { _type: 'string' }
                    },
                    maintenanceMode: {
                        _mandatory: false,
                        show: { _type: 'boolean' },
                        message: { _type: 'string' }
                    },
                    search: {
                        enableParameters: { _type: 'boolean', _mandatory: false },
                        enableSamples: { _type: 'boolean', _mandatory: false },
                        gridOptions: {}
                     },
                    browse: {
                        gridOptions: {
                            columnDefs: {
                                _type: 'array',
                                _item: {
                                    field: { _type: 'string'  },
                                    link: { _type: 'boolean', _mandatory: false },
                                    cellTemplate: { _type: 'string', _mandatory: false }
                                }
                             }
                        },
                        metaTabs: {
                            _type: 'array',
                            _mandatory: false,
                            _item: {
                                title: { _type: 'string'  },
                                items: {
                                    _type: 'array',
                                    _item: {
                                        field: { _type: 'string'  },
                                        label: { _type: 'string', _mandatory: false },
                                        template: { _type: 'string', _mandatory: false }
                                    }
                                 }
                            }
                         }
                    },
                    cart: {
                        maxDatafileCount: {_type: 'number' },
                        maxTotalSize: {_type: 'number' },
                        gridOptions: {
                            columnDefs: {
                                _type: 'array',
                                _item: {
                                    field: { _type: 'string'  },
                                    cellTemplate: { _type: 'string', _mandatory: false }
                                }
                             }
                        }
                    },
                    myDownloads: {
                        gridOptions: {
                            columnDefs: {
                                _type: 'array',
                                _item: {
                                    title: { _type: 'string', _mandatory: false },
                                    field: { _type: 'string'  },
                                    cellTemplate: { _type: 'string', _mandatory: false }
                                }
                             }
                        }
                     },
                    pages: {
                        _type: 'array',
                        _item: {
                            url: { _type: 'string'  },
                            stateName: { _type: 'string'  },
                            addToNavBar: { 
                                _mandatory: false,
                                linkLabel: { _type: 'string'  },
                                align: { _type: 'string'  }
                            }
                         }
                    }
                 },
                facilities: {
                    _type: 'array',
                    _item: {
                        title: { _type: 'string'  },
                        name: { _type: 'string'  },
                        idsUrl: { _type: 'string'  },
                        icatUrl: { _type: 'string', _mandatory: false },
                        hierarchy: {
                            _type: 'array',
                            _item: {
                                _type: 'string'
                             }
                        },
                        authenticationTypes: {
                            _type: 'array',
                            _item: {
                                title: { _type: 'string'  },
                                plugin: { _type: 'string'  },
                                casUrl: { _type: 'string', _mandatory: function(o){
                                    return o.plugin == 'cas';
                                }}
                            }
                        },
                        downloadTransportTypes: {
                            _type: 'array',
                            _mandatory: false,
                            _item: {
                                type: { _type: 'string'  },
                                idsUrl: { _type: 'string'  }
                            }
                        },
                        admin: {
                            gridOptions: {
                                columnDefs: {
                                    _type: 'array',
                                    _item: {
                                        title: { _type: 'string', _mandatory: false },
                                        field: { _type: 'string'  },
                                        cellTemplate: { _type: 'string', _mandatory: false }
                                    }
                                 }
                            }
                         },
                        myData: {
                            entityType: { _type: 'string' },
                            gridOptions: {
                                enableSelection: { _type: 'boolean', _mandatory: false },
                                columnDefs: {
                                    _type: 'array',
                                    _item: {
                                        title: { _type: 'string', _mandatory: false },
                                        field: { _type: 'string'  },
                                        cellTemplate: { _type: 'string', _mandatory: false },
                                        jpqlFilter: { _type: 'string', _mandatory: false },
                                        jpqlSort: { _type: 'string', _mandatory: false },
                                        link: { _type: 'boolean|string', _mandatory: false },
                                        where: { _type: 'string', _mandatory: false },
                                        excludeFuture: { _type: 'boolean', _mandatory: false },
                                        sort: {
                                            _mandatory: false,
                                            direction: { _type: 'string'  },
                                            priority: { _type: 'number', _mandatory: false }
                                        }
                                     }
                                }
                             }
                        },
                        browse: {}
                     }
                },
                plugins: {
                    _type: 'array',
                    _mandatory: false,
                    _item: {
                        _type: 'string'
                     }
               }
            };


            _.each(["instrument", "facilityCycle", "investigation", "proposal", "dataset", "datafile"], function(entityType){
                schema.facilities._item.browse[entityType] = {
                    _mandatory: false,
                    skipSingleEntities: { _type: 'boolean', _mandatory: false },
                    gridOptions: {
                        columnDefs: {
                            _type: 'array',
                            _item: {
                                title: { _type: 'string', _mandatory: false },
                                field: { _type: 'string'  },
                                cellTemplate: { _type: 'string', _mandatory: false },
                                jpqlFilter: { _type: 'string', _mandatory: false },
                                jpqlSort: { _type: 'string', _mandatory: false },
                                link: { _type: 'boolean|string', _mandatory: false },
                                where: { _type: 'string', _mandatory: false },
                                excludeFuture: { _type: 'boolean', _mandatory: false },
                                breadcrumb: { _type: 'boolean', _mandatory: false },
                                breadcrumbTemplate: { _type: 'string', _mandatory: false },
                                sort: {
                                    _mandatory: false,
                                    direction: { _type: 'string'  },
                                    priority: { _type: 'number', _mandatory: false }
                                }
                            }
                        }
                    },
                    metaTabs: {
                        _type: 'array',
                        _mandatory: false,
                        _item: {
                            title: { _type: 'string'  },
                            items: {
                                _type: 'array',
                                _item: {
                                    field: { _type: 'string'  },
                                    label: { _type: 'string', _mandatory: false },
                                    template: { _type: 'string', _mandatory: false }
                                }
                             }
                        }
                    }
                };

                if(entityType == 'investigation' || entityType == 'dataset' || entityType == 'datafile'){
                    schema.facilities._item.browse[entityType].gridOptions.enableSelection = { _type: 'boolean', _mandatory: false };
                }
                if(entityType == 'datafile'){
                    schema.facilities._item.browse[entityType].gridOptions.enableDownload = { _type: 'boolean', _mandatory: false };
                }

            });


            _.each(['investigation', 'dataset', 'datafile'], function(entityType){
                schema.site.search.gridOptions[entityType] = {
                    enableSelection: { _type: 'boolean', _mandatory: false },
                    columnDefs: {
                        _type: 'array',
                        _item: {
                            field: { _type: 'string'  },
                            link: { _type: 'boolean|string', _mandatory: false },
                            cellTemplate: { _type: 'string', _mandatory: false }
                        }
                     }
                };
            });

            _.each(pluginSchemas, function(pluginSchema){
                _.merge(schema, pluginSchema);
            });

            return this.create(schema);
        };
    });

})();




(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MetaPanelController', function($scope, $translate, tc, helpers, MetaDataManager){
        var that = this;
        var previousEntityHash;

        $scope.$on('rowclick', function(event, entity){
            var facility = tc.facility(entity.facilityName);
            var config = facility.config().metaTabs[entity.type];
            if(!config) return;

            var entityHash = entity.facilityName + ":" + entity.type + ":" + entity.id;
            if(entityHash == previousEntityHash){
                that.tabs = [];
                previousEntityHash = undefined;
                return;
            }
            previousEntityHash = entityHash;
            
            var queryBuilder = facility.icat().queryBuilder(entity.type).where(entity.type + ".id = " + entity.id);

            if(entity.type == 'instrument'){
                queryBuilder.include('instrumentScientistUser');
            }

            if(entity.type == 'investigation'){
                queryBuilder.include('user');
                queryBuilder.include('investigationParameterType');
                queryBuilder.include('sample');
                queryBuilder.include('publication');
            }

            if(entity.type == 'dataset'){
                queryBuilder.include('datasetParameterType');
                queryBuilder.include('sample');
                queryBuilder.include('datasetType');
            }

            if(entity.type == 'datafile'){
                queryBuilder.include('datafileParameterType');
            }

            queryBuilder.run().then(function(entity){
                entity = entity[0];

                var tabs = [];
                _.each(config, function(tabConfig){
                    var tab = {
                        title: $translate.instant(tabConfig.title),
                        items: []
                    };
                    _.each(tabConfig.items, function(itemConfig){
                        var find = itemConfig.find || helpers.uncapitalize(entity.entityType);
                        if(!find.match(/\]$/)) find = find + '[]'
                        _.each(entity.find(find), function(entity){
                            tab.items.push({
                                label: itemConfig.label ? $translate.instant(itemConfig.label) : null,
                                template: itemConfig.template,
                                value: itemConfig.value ? entity.find(itemConfig.value)[0] : null,
                                entity: entity
                            });
                        });
                    });

                    tabs.push(tab);
                });

                that.tabs = tabs;
            });
        });
    });

})();




(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MetaPanelController', function($scope, $translate, tc, helpers){
        var that = this;
        var previousEntityHash;

        $scope.$on('rowclick', function(event, entity){

            var facility = tc.facility(entity.facilityName);
            var config;
            if(entity.type == 'facility'){
                config = tc.config().browse.metaTabs;
            } else if(facility.config().browse[entity.type]) {
                config = facility.config().browse[entity.type].metaTabs;
            }

            if(!config) return;

            helpers.setupMetatabs(config, entity.type);

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
                queryBuilder.include('investigationUser');
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
                        var find = entity.entityType;
                        var field = itemConfig.field;
                        var matches;
                        if(matches = itemConfig.field.replace(/\|.+$/, '').match(/^(.*)?\.([^\.\[\]]+)$/)){
                            find = matches[1];
                            field = matches[2]
                        }
                        if(!find.match(/\]$/)) find = find + '[]';
                        _.each(entity.find(find), function(entity){
                            var value = entity.find(field)[0];
                            if(value !== undefined){
                                tab.items.push({
                                    label: itemConfig.label ? $translate.instant(itemConfig.label) : null,
                                    template: itemConfig.template,
                                    value: value,
                                    entity: entity
                                });
                            }
                        });
                    });

                    tabs.push(tab);
                });

                that.tabs = tabs;
            });
        });
    });

})();


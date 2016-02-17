

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MetaPanelController', function($scope, tc, MetaDataManager){
        var that = this;
        var previousEntityHash;

        $scope.$on('rowclick', function(event, entity){
            var entityHash = entity.facilityName + ":" + entity.type + ":" + entity.id;
            if(entityHash == previousEntityHash){
                that.tabs = [];
                previousEntityHash = undefined;
                return;
            }
            previousEntityHash = entityHash;

            var facility = tc.facility(entity.facilityName);
            var tabs;
            if(entity.type == 'facility'){
                tabs = tc.config().metaTabs[entity.type];
            } else {
                tabs = facility.config().metaTabs[entity.type];
            }

            var capitalizedEntityType = entity.type.replace(/^(.)/, function(s){ return s.toUpperCase(); });
            var query = [capitalizedEntityType];
            var includes = [];
            _.each(tabs, function(tab){

                if(tab.queryParams){
                    _.each(tab.queryParams, function(queryParam){
                        includes.push(queryParam);
                    });
                }
            });

            if(includes.length > 0){
                query.push("INCLUDE " + includes.join(','))
            }

            query.push(['[id=?]', entity.id]);

            facility.icat().query(query).then(function(data){
                that.tabs = MetaDataManager.updateTabs(data, tabs);
            });
            
        });
    });

})();


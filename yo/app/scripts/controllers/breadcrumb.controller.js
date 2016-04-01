
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BreadcrumbController', function($state, $q, $scope, $rootScope, $translate, $timeout, $templateCache, tc, helpers, uiGridConstants){
        var that = this; 
        var stateFromTo = $state.current.name.replace(/^.*?(\w+-\w+)$/, '$1');
        var entityInstanceName = stateFromTo.replace(/^.*-/, '');
        var facilityName = $state.params.facilityName;
        var facility = tc.facility(facilityName);
        var facilityId = facility.config().facilityId;
        var icat = tc.icat(facilityName);
        var breadcrumb = tc.config().breadcrumb;
        var maxBreadcrumbTitleLength = breadcrumb && breadcrumb.maxTitleLength ? breadcrumb.maxTitleLength : 1000000;
        var canceler = $q.defer();
        $scope.$on('$destroy', function(){
            canceler.resolve();
        });
        var breadcrumbTitleMap = {};
        _.each(facility.config().browse, function(config, entityType){
            var field = "";
            _.each(config.gridOptions.columnDefs, function(columnDef){
                if(columnDef.breadcrumb){
                    field = columnDef.field;
                    return false;
                }
            });
            breadcrumbTitleMap[entityType] = field;
        });

        this.maxBreadcrumbTitleLength = maxBreadcrumbTitleLength;
        this.breadcrumbItems = [];

        var breadcrumbTimeout = $timeout(function(){
            var path = window.location.hash.replace(/^#\/browse\/facility\/[^\/]*\//, '').replace(/\/[^\/]*$/, '').split(/\//);
            var pathPairs = _.chunk(path, 2);
            var breadcrumbEntities = {};       
            var breadcrumbPromises = [];

            _.each(pathPairs, function(pathPair){
                if(pathPair.length == 2){
                    var entityType = pathPair[0];
                    var uppercaseEntityType = entityType.replace(/^(.)/, function(s){ return s.toUpperCase(); });
                    var entityId = pathPair[1];
                    if(entityType == 'proposal'){
                        breadcrumbPromises.push(icat.entity("investigation", ["where investigation.name = ?", entityId, "limit 0, 1"], canceler).then(function(entity){
                            breadcrumbEntities[entityType] = entity;
                        }));
                    } else {
                        breadcrumbPromises.push(icat.entity(entityType, ["where ?.id = ?", entityType.safe(), entityId, "limit 0, 1"], canceler).then(function(entity){
                            breadcrumbEntities[entityType] = entity;
                        }));
                    }
                }
            });
            $q.all(breadcrumbPromises).then(function(){
                var currentHref = window.location.hash.replace(/\?.*$/, '');
                var path = window.location.hash.replace(/^#\/browse\/facility\/[^\/]*\//, '').replace(/\?.*$/, '').replace(/\/[^\/]*$/, '').split(/\//);
                
                if(path.length > 1){
                    var pathPairs = _.chunk(path, 2);
                    _.each(pathPairs.reverse(), function(pathPair){
                        var entityType = pathPair[0];
                        var entityId = pathPair[1];
                        var entity = breadcrumbEntities[entityType];
                        var title;
                        if(entity){
                            title = entity.find(breadcrumbTitleMap[entityType])[0] || entity.title || entity.name || 'untitled';
                        } else {
                            title = 'untitled';
                        }
                        that.breadcrumbItems.unshift({
                            title: title,
                            href: currentHref
                        });
                        currentHref = currentHref.replace(/\/[^\/]*\/[^\/]*$/, '');
                    });
                }

                that.breadcrumbItems.unshift({
                    title: facility.config().title,
                    href: currentHref
                });

                that.breadcrumbItems.unshift({
                    translate: "BROWSE.BREADCRUMB.ROOT.NAME",
                    href: '#/browse/facility'
                });

                that.breadcrumbItems.push({
                    translate: 'ENTITIES.' + window.location.hash.replace(/\?.*$/, '').replace(/^.*\//, '').toUpperCase() + '.NAME'
                });

            });

            canceler.promise.then(function(){ $timeout.cancel(breadcrumbTimeout); });

        });


    });
})();

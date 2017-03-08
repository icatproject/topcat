
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('BreadcrumbController', function($scope, $state, $q, $timeout, tc, helpers){
        var that = this;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){
            timeout.resolve();
        });
        var breadcrumbConfig = tc.config().breadcrumb;
        this.maxBreadcrumbTitleLength = breadcrumbConfig  && breadcrumbConfig.maxTitleLength ? breadcrumbConfig .maxTitleLength : 1000000;

        var facility = tc.facility($state.params.facilityName);
        var hierarchy = _.clone(facility.config().hierarchy);
        hierarchy.shift();
        while(hierarchy.length >0){
            var currentEntityType = hierarchy[hierarchy.length - 1];
            var currentEntityIdName = currentEntityType + "Id";
            if($state.params[currentEntityIdName]) break;
            hierarchy.pop();
        }

        
        var breadcrumbPromise = $timeout(function(){
            var promises = [];
            var currentHref = window.location.hash.replace(/\?.*$/, '');
            var items = _.map(hierarchy.reverse(), function(entityType){
                var out = {href: currentHref};
                currentHref = currentHref.replace(/\/[^\/]+\/[^\/]+$/, '');
                var entityId = $state.params[entityType + "Id"];

                var gridOptions = facility.config().browse[entityType].gridOptions;
                var columnDef =  _.select(gridOptions.columnDefs, function(columnDef){
                    return columnDef.breadcrumb;
                }).pop();
                if(!columnDef){
                    columnDef = _.select(gridOptions.columnDefs, function(columnDef){
                        return columnDef.field.replace(/^.*\./, '').replace(/\|.*$/, '') == 'title';
                    }).pop();
                }
                if(!columnDef){
                    columnDef =  _.select(gridOptions.columnDefs, function(columnDef){
                        return columnDef.field.replace(/^.*\./, '').replace(/\|.*$/, '') == 'name';
                    }).pop();
                }
                if(!columnDef){
                    columnDef = gridOptions.columnDefs[0];
                }

                var template = columnDef.breadcrumbTemplate;
                if(!template){
                    var fieldName = columnDef.field.replace(/^.*\./, '').replace(/\|.*$/, '');
                    template = [
                        '<span ng-if="item.entity.' + fieldName + '.length > breadcrumbController.maxBreadcrumbTitleLength" uib-tooltip="{{item.entity.' + fieldName + '}}" tooltip-placement="bottom">',
                            '{{item.entity.' + fieldName + ' | limitTo : breadcrumbController.maxBreadcrumbTitleLength}}...',
                        '</span>',
                        '<span ng-if="item.entity.' + fieldName + '.length <= breadcrumbController.maxBreadcrumbTitleLength">',
                            '{{item.entity.' + fieldName + '}}',
                        '</span>'
                    ].join('');
                }
                out.template = template;

                if(entityType == 'proposal'){
                    promises.push(facility.icat().query(["select investigation from Investigation investigation where investigation.name = ?", entityId, "limit 0, 1"], timeout).then(function(entities){
                        out.entity = entities[0];
                    }));
                } else {
                    promises.push(facility.icat().query(["select ? from ? ? where ?.id = ?", entityType.safe(), helpers.capitalize(entityType).safe(), entityType.safe(), entityType.safe(), entityId, "limit 0, 1"], timeout).then(function(entities){
                        out.entity = entities[0];
                    }));
                }

                return out;
            }).reverse();

            $q.all(promises).then(function(){

                items.unshift({
                    template: "<span>{{item.title}}</span>",
                    title: facility.config().title,
                    href: currentHref
                });

                items[items.length - 1].href = undefined;

                if(tc.userFacilities().length > 1){
                    items.unshift({
                        template: '<a href="{{item.href}}" translate="BROWSE.BREADCRUMB.ROOT.NAME"></a>',
                        href: '#/browse/facility'
                    });
                }

                items.push({
                    template: '<i translate="ENTITIES.' + window.location.hash.replace(/\?.*$/, '').replace(/^.*\//, '').toUpperCase() + '.NAME"></i>'
                });

                that.items = items;
            });
        });
        timeout.promise.then(function(){ $timeout.cancel(breadcrumbPromise); });

    });

})();


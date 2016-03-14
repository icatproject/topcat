

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BrowseFacilitiesController', function($state, $q, $scope, $rootScope, $timeout, tc, uiGridConstants){
        var pagingConfig = tc.config().paging;
        var isScroll = pagingConfig.pagingType == 'scroll';
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().browse.gridOptions);
        this.gridOptions = gridOptions;
        this.isScroll = isScroll;
        gridOptions.data = [];

        if(tc.userFacilities().length == 1){
            var facility = tc.userFacilities()[0];
            var hierarchy = facility.config().hierarchy;
            $state.go('home.browse.facility.' + _.slice(hierarchy, 0, 2).join('-'), {
                facilityName: facility.config().facilityName
            });
            return;
        }

        _.each(gridOptions.columnDefs, function(columnDef) {
            if(columnDef.filter && columnDef.filter.condition) {
                columnDef.filter.condition = uiGridConstants.filter[columnDef.filter.condition.toUpperCase()];
            }
            if(columnDef.translateDisplayName) {
                columnDef.displayName = columnDef.translateDisplayName;
                columnDef.headerCellFilter = 'translate';
            }
            if(columnDef.link === true) {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" href="{{grid.appScope.getNextRouteUrl(row.entity)}}">{{row.entity.' + columnDef.field + '}}</a></div>';
            }
        });

        _.each(tc.userFacilities(), function(facility){
            facility.icat().entity("Facility", ["where facility.id = ?", facility.config().facilityId]).then(function(_facility){
                gridOptions.data.push(_.merge(_facility, facility));
            });
        });


        this.getNextRouteUrl = function(facility){
            var hierarchy = facility.config().hierarchy;
            return $state.href('home.browse.facility.' + _.slice(hierarchy, 0, 2).join('-'), {
                facilityName: facility.config().facilityName
            });
        };


    });

})();

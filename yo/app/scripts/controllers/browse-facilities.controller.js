

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('BrowseFacilitiesController', function($state, $q, $scope, $rootScope, $timeout, tc, helpers, uiGridConstants){
        var pagingConfig = tc.config().paging;
        this.isScroll = pagingConfig.pagingType == 'scroll';
        var gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().browse.gridOptions);
        this.gridOptions = gridOptions;
        helpers.setupIcatGridOptions(gridOptions, 'facility');

        if(tc.userFacilities().length == 1){
            var facility = tc.userFacilities()[0];
            var hierarchy = facility.config().hierarchy;
            $state.go('home.browse.facility.' + _.slice(hierarchy, 0, 2).join('-'), {
                facilityName: facility.config().name
            });
            return;
        }
        
        _.each(tc.userFacilities(), function(facility){
            facility.icat().query(["select facility from Facility facility where facility.id = ?", facility.config().id]).then(function(facilities){
                facilities[0].facilityName = facility.config().name;
                gridOptions.data.push(facilities[0]);
            });
        });

        this.browse = function(row) {
            row.browse();
        };

        this.showTabs = function(row){
            $rootScope.$broadcast('rowclick', {
                'type': row.entity.entityType.toLowerCase(),
                'id' : row.entity.id,
                facilityName: row.entity.facilityName
            });
        };

    });

})();

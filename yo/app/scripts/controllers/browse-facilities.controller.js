

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BrowseFacilitiesController', function($state, $q, $scope, $rootScope, $timeout, tc, helpers, uiGridConstants){
        var pagingConfig = tc.config().paging;
        this.isScroll = pagingConfig.pagingType == 'scroll';
        var gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().browse.gridOptions);
        this.gridOptions = gridOptions;
        helpers.setupGridOptions(gridOptions, 'facility');

        if(tc.userFacilities().length == 1){
            var facility = tc.userFacilities()[0];
            var hierarchy = facility.config().hierarchy;
            $state.go('home.browse.facility.' + _.slice(hierarchy, 0, 2).join('-'), {
                facilityName: facility.config().facilityName
            });
            return;
        }
        
        _.each(tc.userFacilities(), function(facility){
            facility.icat().entity("facility", ["where facility.id = ?", facility.config().facilityId]).then(function(_facility){
                _facility.facilityName = facility.config().facilityName;
                gridOptions.data.push(_facility);
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

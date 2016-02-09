

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BrowseFacilitiesController', function($state, $q, $scope, $rootScope, $timeout, tc, uiGridConstants){
        var pagingConfig = tc.config().paging;
        var isScroll = pagingConfig.pagingType == 'scroll';
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().facilitiesGridOptions);
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


/*
(function() {
    'use strict';


    angular
        .module('angularApp')
        .controller('BrowseFacilitiesController', BrowseFacilitiesController);

    BrowseFacilitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseFacilitiesModel'];

    function BrowseFacilitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage, BrowseFacilitiesModel) {
        //can only be facility entity for browse facilities
        var currentEntityType = 'facility';

        //apply only to browse pages
        if ( $state.current.name === 'home.browse.facility') {
            //redirect if only one facility
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                var facilityName = ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG))[0];

                //get next entity in hierarchy
                var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facilityName);
                var nextRouteSegment = structure[0] + '-' + structure[1];

                $state.go('home.browse.facility.' + nextRouteSegment, {facilityName : facilityName});
                return;
            }
        }

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.currentEntityType = currentEntityType;
        $scope.isScroll = (pagingType === 'scroll') ? true : false;

        var facilities = Config.getFacilities(APP_CONFIG);

        BrowseFacilitiesModel.init(facilities, $scope);
        BrowseFacilitiesModel.getPage();

        $scope.getNextRouteSegment = function(row) {
            return BrowseFacilitiesModel.getNextRouteSegment(row, currentEntityType);
        };

        $scope.showTabs = function(row) {
            var data = {'type' : currentEntityType, 'id' : row.entity.id, facilityName: row.entity.name};

            $rootScope.$broadcast('rowclick', data);
        };
    }
})();
*/
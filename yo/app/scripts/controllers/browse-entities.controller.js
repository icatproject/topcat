(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseEntitiesController', BrowseEntitiesController);

    BrowseEntitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseEntitiesModel', '$log'];

    function BrowseEntitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage, BrowseEntitiesModel, $log) {
        var vm = this;
        var facilityName = $stateParams.facilityName;
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        var currentEntityType = RouteUtils.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
        var currentRouteSegment = RouteUtils.getCurrentRouteSegmentName($state);
        var sessions = $sessionStorage.sessions;

        vm.currentEntityType = currentEntityType;
        vm.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.isEmpty = false;
        $scope.mySelection = {};

        if (!angular.isDefined($rootScope.cart)) {
            $rootScope.cart = [];
            $rootScope.ref = [];
        }

        BrowseEntitiesModel.init(facility, $scope, currentEntityType, currentRouteSegment, sessions, $stateParams);

        vm.gridOptions = BrowseEntitiesModel.gridOptions;


        /**
         * Function required by view expression to get the next route segment
         *
         * Note: we have to use $scope here rather than vm (AS syntax) to make it work
         * with ui-grid cellTemplate grid.appScope
         *
         * @return {[type]}     [description]
         */
        $scope.getNextRouteUrl = function(row) {
            return BrowseEntitiesModel.getNextRouteUrl(row);
        };

        $scope.showTabs = function(row) {
            var data = {'type' : currentEntityType, 'id' : row.entity.id, facilityName: facilityName};
            $rootScope.$broadcast('rowclick', data);
        };
    }
})();
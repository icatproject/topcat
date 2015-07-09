(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseFacilitiesController', BrowseFacilitiesController);

    BrowseFacilitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseFacilitiesModel', '$log'];

    function BrowseFacilitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage, BrowseFacilitiesModel, $log) {
        var vm = this;
        var currentEntityType = RouteUtils.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile

        //redirect
        if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
            var facilityName = ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG))[0];
            $log.debug('is single', facilityName);

            //get next entity in hierarchy
            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facilityName);
            console.log('structure', structure);
            //var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, structure[1]);
            var nextRouteSegment = structure[0] + '-' + structure[1];

            $log.debug('nextRouteSegment', nextRouteSegment);
            $state.go('home.browse.facility.' + nextRouteSegment, {facilityName : facilityName});
        }


        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        vm.currentEntityType = currentEntityType;
        vm.isScroll = (pagingType === 'scroll') ? true : false;

        $log.debug('currentEntityType', currentEntityType);

        if (!angular.isDefined($rootScope.cart)) {
            $rootScope.cart = [];
            $rootScope.ref = [];
        }

        var facilities = Config.getFacilities(APP_CONFIG);

        BrowseFacilitiesModel.init(facilities, $scope);
        vm.gridOptions = BrowseFacilitiesModel.gridOptions;

        /**
         * Function required by view expression to get the next route segment
         *
         * @param  {[type]} row [description]
         * @return {[type]}     [description]
         */
        $scope.getNextRouteSegment = function(row) {
            return BrowseFacilitiesModel.getNextRouteSegment(row, currentEntityType);
        };

        $scope.showTabs = function(row) {
            var data = {'type' : currentEntityType, 'id' : row.entity.id, facilityName: row.entity.name};

            $rootScope.$broadcast('rowclick', data);
        };
    }
})();
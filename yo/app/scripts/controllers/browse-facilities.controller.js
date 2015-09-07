(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseFacilitiesController', BrowseFacilitiesController);

    BrowseFacilitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseFacilitiesModel', '$log'];

    function BrowseFacilitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage, BrowseFacilitiesModel, $log) { //jshint ignore: line
        var currentEntityType = RouteUtils.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile

        //apply only to browse pages
        if ( $state.current.name === 'home.browse.facility') {
            //redirect if only one facility
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                var facilityName = ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG))[0];

                //get next entity in hierarchy
                var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facilityName);
                //var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, structure[1]);
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

        /*if (!angular.isDefined($rootScope.cart)) {
            $rootScope.cart = [];
            $rootScope.ref = [];
        }*/

        var facilities = Config.getFacilities(APP_CONFIG);

        BrowseFacilitiesModel.init(facilities, $scope);
        BrowseFacilitiesModel.getPage();


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
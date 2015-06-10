(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('MyDataController', MyDataController);

    MyDataController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteService', 'DataManager', '$q', 'inform', '$sessionStorage', 'MyDataModel'];

    function MyDataController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteService, DataManager, $q, inform, $sessionStorage, MyDataModel) {
        var vm = this;

        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        var currentEntityType = RouteService.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile

        var facilities = Config.getFacilities(APP_CONFIG);

        var currentRouteSegment = RouteService.getCurrentRouteSegmentName($state);
        var sessions = $sessionStorage.sessions;

        vm.currentEntityType = currentEntityType;
        vm.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.isEmpty = false;
        MyDataModel.init(facilities, $scope, currentEntityType, currentRouteSegment, sessions, $stateParams);

        vm.gridOptions = MyDataModel.gridOptions;


        /**
         * Function required by view expression to get the next route segment
         *
         * Note: we have to use $scope here rather than vm (AS syntax) to make it work
         * with ui-grid cellTemplate grid.appScope
         *
         * @return {[type]}     [description]
         */
        $scope.getNextRouteUrl = function(row) {
            return MyDataModel.getNextRouteUrl(row);
        };

        $scope.showTabs = function(row) { //jshint ignore: line
            /*var data = {'type' : currentEntityType, 'id' : row.entity.id, facilityName: facilityName};
            $rootScope.$broadcast('rowclick', data);*/
        };
    }
})();
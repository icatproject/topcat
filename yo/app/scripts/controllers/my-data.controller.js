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
        //var currentEntityType = RouteService.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        var entityType = Config.getSiteMyDataGridEntityType(APP_CONFIG);
        var facilities = Config.getFacilities(APP_CONFIG);
        var currentRouteSegment = RouteService.getCurrentRouteSegmentName($state);
        var sessions = $sessionStorage.sessions;

        //vm.currentEntityType = currentEntityType;
        vm.isScroll = (pagingType === 'scroll') ? true : false;
        MyDataModel.init(facilities, $scope, entityType, currentRouteSegment, sessions, $stateParams);

        vm.gridOptions = MyDataModel.gridOptions;

        vm.items = MyDataModel.gridOptions.data;
        vm.isEmpty = false;

        $scope.$watchCollection(function() {
            return vm.items;
        }, function(newCol) {
            console.log('newCol', newCol);
            if (typeof newCol === 'undefined') {
                console.log('isEmpty', true);
                vm.isEmpty = true;
            } else {
                if(newCol.length === 0) {
                    vm.isEmpty = true;
                    console.log('isEmpty', true);
                } else {
                    vm.isEmpty = false;
                    console.log('isEmpty', false);
                }
            }
        });



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
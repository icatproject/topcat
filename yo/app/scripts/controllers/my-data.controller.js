(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('MyDataController', MyDataController);

    MyDataController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteService', 'DataManager', '$q', 'inform', '$sessionStorage', 'MyDataModel', '$templateCache', '$log'];

    function MyDataController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteService, DataManager, $q, inform, $sessionStorage, MyDataModel, $templateCache, $log) {
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        var entityType = Config.getSiteMyDataGridEntityType(APP_CONFIG);
        var facilities = Config.getFacilities(APP_CONFIG);
        var currentRouteSegment = RouteService.getCurrentRouteSegmentName($state);
        var sessions = $sessionStorage.sessions;

        $scope.isScroll = (pagingType === 'scroll') ? true : false;
        $scope.isEmpty = false;

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        MyDataModel.init(facilities, $scope, entityType, currentRouteSegment, sessions, $stateParams);

        $templateCache.put('ui-grid/selectionRowHeaderButtons',
            '<div class="ui-grid-selection-row-header-buttons ui-grid-icon-ok" ng-class="{\'ui-grid-row-selected\': row.isSelected}" ng-click="selectButtonClick(row, $event)" tooltip="' + $translate.instant('MY_DATA.SELECTOR.TOOLTIP.TEXT') + '" tooltip-placement="right" tooltip-append-to-body="true">&nbsp;</div>'
        );

        if (pagingType === 'page') {
            $scope.gridOptions.onRegisterApi = function(gridApi) {
                $scope.gridApi = gridApi;

                //sort callback
                $scope.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                    MyDataModel.sortChanged(grid, sortColumns);
                });

                //pagination callback
                $scope.gridApi.pagination.on.paginationChanged($scope, function(newPage, pageSize) {
                    MyDataModel.paginationChanged(newPage, pageSize);
                });

                $scope.gridApi.core.on.filterChanged($scope, function() {
                    MyDataModel.filterChanged(this.grid);
                });

                $scope.gridApi.selection.on.rowSelectionChanged($scope, function(row) {
                    MyDataModel.rowSelectionChanged(row);
                });

                $scope.gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
                    MyDataModel.rowSelectionChangedBatch(rows);
                });
            };

            MyDataModel.getPage();
        } else {
            $scope.firstPage = 1;
            $scope.lastPage = null;
            $scope.currentPage = 1;

            $scope.gridOptions.onRegisterApi = function(gridApi) {
                $log.debug('onRegisterApi called', gridApi);

                $scope.gridApi = gridApi;

                //sort callback
                $scope.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                    MyDataModel.sortChanged(grid, sortColumns);
                });

                $scope.gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                    MyDataModel.needLoadMoreData();
                });

                $scope.gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {
                    MyDataModel.needLoadMoreDataTop();
                });

                $scope.gridApi.core.on.filterChanged($scope, function () {
                    MyDataModel.filterChanged(this.grid);
                });

                $scope.gridApi.selection.on.rowSelectionChanged($scope, function(row){
                    MyDataModel.rowSelectionChanged(row);
                });

                $scope.gridApi.selection.on.rowSelectionChangedBatch ($scope, function(rows){
                    MyDataModel.rowSelectionChangedBatch(rows);
                });
            };

            MyDataModel.getPage();
        }

        $scope.$watchCollection(function() {
            return $scope.gridOptions.data;
        }, function(newCollection) {
            if (typeof newCollection === 'undefined') {
                $scope.isEmpty = true;
            } else {
                if(newCollection.length === 0) {
                    $scope.isEmpty = true;
                } else {
                    $scope.isEmpty = false;
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
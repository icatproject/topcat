(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseEntitiesController', BrowseEntitiesController);

    BrowseEntitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteService', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseEntitiesModel', 'uiGridConstants', '$templateCache', '$log'];

    function BrowseEntitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteService, DataManager, $q, inform, $sessionStorage, BrowseEntitiesModel, uiGridConstants, $templateCache, $log) { //jshint ignore: line
        var facilityName = $stateParams.facilityName;
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        var currentEntityType = RouteService.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
        var currentRouteSegment = RouteService.getCurrentRouteSegmentName($state);
        var sessions = $sessionStorage.sessions;

        $scope.currentEntityType = currentEntityType;
        $scope.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.isEmpty = false;

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        BrowseEntitiesModel.init(facility, $scope, currentEntityType, currentRouteSegment, sessions, $stateParams, $scope.gridOptions);

        /*$templateCache.put('ui-grid/selectionSelectAllButtons',
            '<div><span class="glyphicon glyphicon-shopping-cart" tooltip="Click [✓] in this column to add/remove items from cart" tooltip-append-to-body="true"></span></div>'
        );*/

        $templateCache.put('ui-grid/selectionRowHeaderButtons',
            '<div class="ui-grid-selection-row-header-buttons ui-grid-icon-ok" ng-class="{\'ui-grid-row-selected\': row.isSelected}" ng-click="selectButtonClick(row, $event)" tooltip="' + $translate.instant('BROWSE.SELECTOR.TOOLTIP.TEXT') + '" tooltip-placement="right" tooltip-append-to-body="true">&nbsp;</div>'
        );

        if (pagingType === 'page') {
            $scope.gridOptions.onRegisterApi = function(gridApi) {
                $log.warn('onRegisterApi called for page', gridApi);

                $scope.gridApi = gridApi;

                //sort change callback
                $scope.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                    BrowseEntitiesModel.sortChanged(grid, sortColumns);
                });

                //pagination callback
                $scope.gridApi.pagination.on.paginationChanged($scope, function(newPage, pageSize) {
                    BrowseEntitiesModel.paginationChanged(newPage, pageSize);
                });

                //filter change callback
                $scope.gridApi.core.on.filterChanged($scope, function() {
                    BrowseEntitiesModel.filterChanged(this.grid.columns);
                });

                //row single row selection callback
                $scope.gridApi.selection.on.rowSelectionChanged($scope, function(row) {
                    BrowseEntitiesModel.rowSelectionChanged(row);
                });

                //multiple rows selection callback
                $scope.gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
                    BrowseEntitiesModel.rowSelectionChangedBatch(rows);
                });
            };

            //BrowseEntitiesModel.getPage();
            BrowseEntitiesModel.applyFilterAndGetPage($scope.gridOptions.columnDefs);
        } else {
            $scope.firstPage = 1;
            $scope.lastPage = null;
            $scope.currentPage = 1;

            $scope.gridOptions.onRegisterApi = function(gridApi) {
                $log.warn('onRegisterApi called for scroll', gridApi);

                $scope.gridApi = gridApi;

                //sort change callback
                $scope.gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                    BrowseEntitiesModel.sortChanged(grid, sortColumns);
                });

                //scroll down more data callback (append data)
                $scope.gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                    BrowseEntitiesModel.needLoadMoreData();
                });

                //scoll up more data at top callback (prepend data)
                $scope.gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {
                    BrowseEntitiesModel.needLoadMoreDataTop();
                });

                //filter change calkback
                $scope.gridApi.core.on.filterChanged($scope, function () {
                    BrowseEntitiesModel.filterChanged(this.grid.columns);
                });

                //single row selection callback
                $scope.gridApi.selection.on.rowSelectionChanged($scope, function(row){
                    BrowseEntitiesModel.rowSelectionChanged(row);
                });

                //multiple rows selection callback
                $scope.gridApi.selection.on.rowSelectionChangedBatch ($scope, function(rows){
                    BrowseEntitiesModel.rowSelectionChangedBatch(rows);
                });
            };

            BrowseEntitiesModel.applyFilterAndGetPage($scope.gridOptions.columnDefs);
        }

        $rootScope.$on('Cart:itemRemoved', function(){
            BrowseEntitiesModel.refreshSelection($scope);
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
            return BrowseEntitiesModel.getNextRouteUrl(row);
        };

        $scope.showTabs = function(row) {
            var data = {'type' : currentEntityType, 'id' : row.entity.id, facilityName: facilityName};
            $rootScope.$broadcast('rowclick', data);
        };
    }
})();
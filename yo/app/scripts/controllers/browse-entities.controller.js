
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BrowseEntitiesController', function($state, $q, $scope, tc){

        //e.g. 'facility-instrument' i.e. from 'facility' to 'instrument'
        var stateFromTo = $state.current.name.replace(/^.*?(\w+-\w+)$/, '$1');
        var entityInstanceName = stateFromTo.replace(/^.*-/, '');
        var gridOptionsName = entityInstanceName;
        if(entityInstanceName == 'proposal') entityInstanceName = 'investigation';
        var facilityName = $state.params.facilityName;
        var facility = tc.facility(facilityName);
        var facilityId = facility.config().facilityId;
        var icat = tc.icat(facilityName);
        var gridOptions = _.merge({data: [], appScopeProvider: this}, facility.config().browseGridOptions[gridOptionsName]);
        var uiGridState = $state.params.uiGridState ? JSON.parse($state.params.uiGridState) : null;
        var pagingConfig = tc.config().paging;
        var isScroll = pagingConfig.pagingType == 'scroll';
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var page = 1;
        var canceler = $q.defer();
        $scope.$on('$destroy', function(){ canceler.resolve(); });
        var columnNames = _.map(gridOptions.columnDefs, function(columnDef){ return columnDef.field; });
        var isSize = _.includes(columnNames, 'size');
        var orderBy = columnNames[0];
        var orderByDirection = 'asc';

        this.gridOptions = gridOptions;
        this.isScroll = isScroll;

        var stateFromToQueries = {
            'facility-proposal': [
                'select DISTINCT investigation.name from Investigation investigation, investigation.facility facility',
                'where facility.id = ?', facilityId
            ],
            'facility-instrument': [
                'select instrument from Instrument instrument, instrument.facility facility',
                'where facility.id = ?', facilityId
            ],
            'instrument-facilityCycle': [
                'select facilityCycle from',
                'FacilityCycle facilityCycle,',
                'facilityCycle.facility facility,',
                'facility.investigations investigation,',
                'investigation.investigationInstruments investigationInstrument,',
                'investigationInstrument.instrument instrument',
                'where facility.id = ?', facilityId,
                'and instrument.id = ?', $state.params.instrumentId,
                'and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate'
            ],
            'facilityCycle-proposal': [
                'select DISTINCT investigation.name from',
                'Investigation investigation,',
                'investigation.investigationInstruments investigationInstrument,',
                'investigationInstrument.instrument instrument,',
                'instrument.facility facility,',
                'facility.facilityCycles facilityCycle',
                'where facility.id = ?', facilityId,
                'and instrument.id = ?', $state.params.instrumentId,
                'and facilityCycle.id = ?', $state.params.facilityCycleId,
                'and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate'
            ],
            'proposal-investigation': [
                'select investigation from Investigation investigation',
                'where investigation.name = ?', $state.params.proposalId
            ],
            'investigation-dataset': [
                'select dataset from Dataset dataset, dataset.investigation investigation',
                'where investigation.id = ?', $state.params.investigationId
            ],
            'dataset-datafile': [
                'select datafile from Datafile datafile, datafile.dataset dataset',
                'where dataset.id = ?', $state.params.datasetId
            ]
        };

        var query = [stateFromToQueries[stateFromTo],
            'order by ' + entityInstanceName + '.' + orderBy + ' ' + orderByDirection,
            'limit ?, ?', function(){ return (page - 1) * pageSize; }, pageSize
        ];

        function getPage(orderBy, orderByDirection){
            var out = icat.query(canceler.promise, query);
            if(gridOptionsName == 'proposal'){
                var defered = $q.defer();
                out.then(function(names){
                    defered.resolve(_.map(names, function(name){ return {name: name}; }));
                }, function(response){
                    defered.reject(response);
                });
                out = defered.promise;
            }
            if(isSize){
                out.then(function(results){
                    _.each(results, function(result){ result.getSize(canceler.promise); });
                });
            }
            return out;
        }

        this.getNextRouteUrl = function(row){
            var hierarchy = facility.config().hierarchy
            var stateSuffixes = {};
            _.each(hierarchy, function(currentEntityType, i){
                stateSuffixes[currentEntityType] = _.slice(hierarchy, 0, i + 2).join('-');
            });
            var params = _.clone($state.params);
            params[gridOptionsName + 'Id'] = row.id || row.name;
            return $state.href('home.browse.facility.' + stateSuffixes[gridOptionsName], params);
        };

        _.each(gridOptions.columnDefs, function(columnDef){
            if(columnDef.link) {
                columnDef.cellTemplate = '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" href="{{grid.appScope.getNextRouteUrl(row.entity)}}">{{row.entity.' + columnDef.field + '}}</a></div>';
            }
            if(columnDef.field == 'size'){
                columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
            }
        });

        gridOptions.onRegisterApi = function(gridApi) {
            getPage().then(function(results){
                gridOptions.data = results;
            });

            if(isScroll){
                //scroll down more data callback (append data)
                gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                    page++;
                    getPage().then(function(results){
                        _.each(results, function(result){ gridOptions.data.push(result); });
                        if(results.length == 0) page--;
                    });
                });

                //scoll up more data at top callback (prepend data)
                gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {
                    page--;
                    getPage().then(function(results){
                        _.each(results.reverse(), function(result){ gridOptions.data.unshift(result); });
                        if(results.length == 0) page++;
                    });
                });
            }

        };

    });
})();

if(false){
    (function() {
        'use strict';

        /*jshint -W083 */
        angular
            .module('angularApp')
            .controller('BrowseEntitiesController', BrowseEntitiesController);

        BrowseEntitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteService', 'DataManager', '$q', 'inform', '$sessionStorage', 'BrowseEntitiesModel', 'uiGridConstants', 'Utils', '$templateCache'];

        function BrowseEntitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, APP_CONFIG, Config, $translate, ConfigUtils, RouteService, DataManager, $q, inform, $sessionStorage, BrowseEntitiesModel, uiGridConstants, Utils, $templateCache) {
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

            $scope.dateOptions = {
                'dateformat' : 'yyyy-MM-dd',
                'show-weeks' : false
            };

            $scope.open = function(event){ //jshint ignore: line
                //event.preventDefault();
                //event.stopPropagation();
                $scope.status.opened = true;
            };

            /*$scope.clear = function () {
                $scope.ngModel = null;
            };*/

            $scope.status = {
                opened: false
            };

            /*$templateCache.put('ui-grid/selectionSelectAllButtons',
                '<div><span class="glyphicon glyphicon-shopping-cart" tooltip="Click [✓] in this column to add/remove items from cart" tooltip-append-to-body="true"></span></div>'
            );*/

            $templateCache.put('ui-grid/selectionRowHeaderButtons',
                '<div class="ui-grid-selection-row-header-buttons ui-grid-icon-ok" ng-class="{\'ui-grid-row-selected\': row.isSelected}" ng-click="selectButtonClick(row, $event)" tooltip="' + $translate.instant('BROWSE.SELECTOR.TOOLTIP.TEXT') + '" tooltip-placement="right" tooltip-append-to-body="true">&nbsp;</div>'
            );


            if (pagingType === 'page') {
                $scope.gridOptions.onRegisterApi = function(gridApi) {
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

                    BrowseEntitiesModel.init(facility, $scope, currentEntityType, currentRouteSegment, sessions, $stateParams);
                };
            } else {
                $scope.firstPage = 1;
                $scope.lastPage = null;
                $scope.currentPage = 1;

                $scope.gridOptions.onRegisterApi = function(gridApi) {
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

                    BrowseEntitiesModel.init(facility, $scope, currentEntityType, currentRouteSegment, sessions, $stateParams);
                };

               
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

            $scope.getFieldValuesAsHtmlList = function(row, field) {
                return Utils.getFieldValuesAsHtmlList(row, field);
            };
        }
    })();
}
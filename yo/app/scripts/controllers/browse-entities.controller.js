
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('BrowseEntitiesController', function($state, $q, $scope, $rootScope, $timeout, tc, Cart){

        //e.g. 'facility-instrument' i.e. from 'facility' to 'instrument'
        var stateFromTo = $state.current.name.replace(/^.*?(\w+-\w+)$/, '$1');
        var entityInstanceName = stateFromTo.replace(/^.*-/, '');
        var realEntityInstanceName = entityInstanceName;
        if(realEntityInstanceName == 'proposal') realEntityInstanceName = 'investigation';
        var facilityName = $state.params.facilityName;
        var facility = tc.facility(facilityName);
        var facilityId = facility.config().facilityId;
        var icat = tc.icat(facilityName);
        var gridOptions = _.merge({data: [], appScopeProvider: this}, facility.config().browseGridOptions[entityInstanceName]);
        var uiGridState = $state.params.uiGridState ? JSON.parse($state.params.uiGridState) : null;
        var pagingConfig = tc.config().paging;
        var isScroll = pagingConfig.pagingType == 'scroll';
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var page = 1;
        var canceler = $q.defer();
        $scope.$on('$destroy', function(){ canceler.resolve(); });
        var columnNames = _.map(gridOptions.columnDefs, function(columnDef){ return columnDef.field; });
        var isSize = _.includes(columnNames, 'size');
        var sortQuery = [];
        var filterQuery = [];
        var totalItems;
        var gridApi;

        this.gridOptions = gridOptions;
        this.isScroll = isScroll;

        function generateQuery(stateFromTo, isCount){
            var queries = {
                'facility-proposal': [
                    'select ' + item('DISTINCT investigation.name') + ' from Investigation investigation, investigation.facility facility',
                    'where facility.id = ?', facilityId
                ],
                'facility-instrument': [
                    'select ' + item('instrument') + ' from Instrument instrument, instrument.facility facility',
                    'where facility.id = ?', facilityId
                ],
                'instrument-facilityCycle': [
                    'select ' + item('facilityCycle') + ' from',
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
                    'select ' + item('DISTINCT investigation.name') + ' from',
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
                    'select ' + item('investigation') + ' from Investigation investigation',
                    'where investigation.name = ?', $state.params.proposalId
                ],
                'investigation-dataset': [
                    'select ' + item('dataset') + ' from Dataset dataset, dataset.investigation investigation',
                    'where investigation.id = ?', $state.params.investigationId
                ],
                'dataset-datafile': [
                    'select ' + item('datafile') + ' from Datafile datafile, datafile.dataset dataset',
                    'where dataset.id = ?', $state.params.datasetId
                ]
            };

            //maybe use some sort of safe string feature instead.
            function item(defaultExpression){
                var out = defaultExpression;
                if(isCount) out = "count(" + out + ")";
                return out;
            }

            var out = [queries[stateFromTo], filterQuery, sortQuery];

            if(!isCount) out.push('limit ?, ?', function(){ return (page - 1) * pageSize; }, function(){ return pageSize; });
            
            var includes = gridOptions.includes;
            if(includes) out.push('include ' + includes.join(', '))

            return out;
        }

        function getPage(){
            var out = icat.query(canceler.promise, generateQuery(stateFromTo));
            if(entityInstanceName == 'proposal'){
                var defered = $q.defer();
                out.then(function(names){
                    defered.resolve(_.map(names, function(name){ return {name: name, entityType: 'proposal'}; }));
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

        function updateScroll(resultCount){
            if(isScroll){
                $timeout(function(){
                    var isMore = resultCount == pageSize;
                    if(page == 1) gridApi.infiniteScroll.resetScroll(false, isMore);
                    gridApi.infiniteScroll.dataLoaded(false, isMore);
                });
            }
        }

        function updateTotalItems(){
            if(!isScroll){
                icat.query(canceler.promise, generateQuery(stateFromTo, true)).then(function(_totalItems){
                    gridOptions.totalItems = _totalItems;
                    totalItems = _totalItems;
                });
            }
        }

        function isAncestorInCart(){
            var out = false;
            _.each(Cart.getItems(), function(item){
                _.each(['investigation', 'dataset'], function(entityType){
                    var entityId = $state.params[entityType + "Id"];
                    if(item.facilityName == facilityName && item.entityType == entityType && item.entityId == entityId){
                        out = true;
                        return false;
                    }
                    return !out;
                });
            });
            return out;
        }

        function updateSelections(){
            var timeout = $timeout(function(){
                _.each(gridOptions.data, function(row){
                    if (isAncestorInCart() || Cart.hasItem(facilityName, entityInstanceName.toLowerCase(), row.id)) {
                        gridApi.selection.selectRow(row);
                    } else {
                        gridApi.selection.unSelectRow(row);
                    }
                });
            });
            canceler.promise.then(function(){ $timeout.cancel(timeout); });
        }

        function removeRedundantItemsFromCart(){
            var itemsToRemove = [];
            _.each(Cart.getItems(), function(item){
                _.each(item.parentEntities, function(parentEntity){
                    var parentInCart = false;
                    _.each(Cart.getItems(), function(_item){
                        parentInCart = item.facilityName == _item.facilityName && parentEntity.entityType == _item.entityType && parentEntity.entityId == _item.entityId;
                        return !parentInCart;
                    });
                    if(parentInCart){
                        itemsToRemove.push(item);
                        return false;
                    }
                });
            });
            _.each(itemsToRemove, function(item){
                Cart.removeItem(item.facilityName, item.entityType, item.entityId);
            });
        }

        function saveState(){
            var uiGridState = JSON.stringify({
                columns: gridApi.saveState.save().columns,
                pageSize: pageSize,
                page: page
            });
            $state.go($state.current.name, {uiGridState: uiGridState}, {location: 'replace'});
        }

        function restoreState(){
            $timeout(function(){
                var uiGridState = $state.params.uiGridState ? JSON.parse($state.params.uiGridState) : null;
                if(uiGridState){
                    self.pageSize = uiGridState.pageSize;
                    pageSize = uiGridState.pageSize;
                    page = uiGridState.page;
                    delete uiGridState.pageSize;
                    delete uiGridState.page;
                    gridApi.saveState.restore($scope, uiGridState);
                }
            });
        }


        this.getNextRouteUrl = function(row){
            var hierarchy = facility.config().hierarchy
            var stateSuffixes = {};
            _.each(hierarchy, function(currentEntityType, i){
                stateSuffixes[currentEntityType] = _.slice(hierarchy, 0, i + 2).join('-');
            });
            var params = _.clone($state.params);
            delete params.uiGridState;
            params[entityInstanceName + 'Id'] = row.id || row.name;
            return $state.href('home.browse.facility.' + stateSuffixes[entityInstanceName], params);
        };

        gridOptions.rowTemplate = '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>',
        this.showTabs = function(row) {
            $rootScope.$broadcast('rowclick', {
                'type': row.entity.entityType.toLowerCase(),
                'id' : row.entity.id,
                facilityName: facilityName
            });
        };

        _.each(gridOptions.columnDefs, function(columnDef){
            if(columnDef.link) {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" href="{{grid.appScope.getNextRouteUrl(row.entity)}}">{{row.entity.' + columnDef.field + '}}</a></div>';
            }
            if(columnDef.field == 'size'){
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
                columnDef.enableSorting = false;
                columnDef.enableFiltering = false;
            }
            columnDef.jpqlExpression = columnDef.jpqlExpression || realEntityInstanceName + '.' + columnDef.field;
        });

        if(gridOptions.enableDownload){
            gridOptions.columnDefs.push({
                name : 'actions',
                visible: true,
                translateDisplayName: 'BROWSE.COLUMN.ACTIONS.NAME',
                enableFiltering: false,
                enable: false,
                enableColumnMenu: false,
                enableSorting: false,
                enableHiding: false,
                cellTemplate : '<div class="ui-grid-cell-contents"><download-datafile></download-datafile></div>'
            });
        }

        gridOptions.paginationPageSizes = pagingConfig.paginationPageSizes;
        gridOptions.paginationNumberOfRows = pagingConfig.paginationNumberOfRows;
        gridOptions.useExternalPagination = true;
        gridOptions.useExternalSorting = true;
        gridOptions.useExternalFiltering = true;


        gridOptions.onRegisterApi = function(_gridApi) {
            gridApi = _gridApi;
            restoreState();

            getPage().then(function(results){
                gridOptions.data = results;
                updateTotalItems();
                updateSelections();
                updateScroll(results.length);
                removeRedundantItemsFromCart();
            });

            //sort change callback
            gridApi.core.on.sortChanged($scope, function(grid, sortColumns){
                sortQuery = [];
                if(sortColumns.length > 0){
                    sortQuery.push('order by ' + _.map(sortColumns, function(sortColumn){
                        console.log(sortColumn);
                        return sortColumn.colDef.jpqlExpression + ' ' + sortColumn.sort.direction;
                    }).join(', '));
                }
                page = 1;
                getPage().then(function(results){
                    updateScroll(results.length);
                    gridOptions.data = results;
                    updateSelections();
                    saveState();
                });
            });

            //filter change calkback
            gridApi.core.on.filterChanged($scope, function() {
                canceler.resolve();
                canceler = $q.defer();
                filterQuery = [];
                _.each(gridOptions.columnDefs, function(columnDef){
                    if(columnDef.type == 'date' && columnDef.filters){
                        var from = columnDef.filters[0].term || '';
                        var to = columnDef.filters[1].term || '';
                        if(from.match(/^\d\d\d\d-\d\d-\d\d$/) && to.match(/^\d\d\d\d-\d\d-\d\d$/)){
                            filterQuery.push([
                                "and ? between {ts ? 00:00:00} and {ts ? 23:59:59}",
                                columnDef.jpqlExpression.safe(),
                                from.safe(),
                                to.safe()
                            ]);
                        }
                    } else if(columnDef.type == 'string' && columnDef.filter && columnDef.filter.term) {
                        filterQuery.push([
                            "and UPPER(?) like concat('%', ?, '%')", 
                            columnDef.jpqlExpression.safe(),
                            columnDef.filter.term.toUpperCase()
                        ]);
                    }
                });
                page = 1;
                
                gridOptions.data = [];
                getPage().then(function(results){
                    gridOptions.data = results;
                    updateSelections();
                    updateScroll(results.length);
                    updateTotalItems();
                    saveState();
                });
            });

            function addItem(row){
                if(!isAncestorInCart()){
                    var parentEntities = [];
                    _.each(['investigation', 'dataset', 'datafile'], function(entityType){
                        var id = entityType + "Id";
                        if($state.params[id]) parentEntities.push({
                            entityType: entityType,
                            entityId: $state.params[id]
                        });
                    });
                    Cart.addItem(facilityName, entityInstanceName.toLowerCase(), row.id, row.name, parentEntities);
                }
            }

            function removeItem(row){
                if(!isAncestorInCart()){
                    Cart.removeItem(facilityName, entityInstanceName.toLowerCase(), row.id);
                }
            }

            gridApi.selection.on.rowSelectionChanged($scope, function(row) {
                if(_.find(gridApi.selection.getSelectedRows(), _.pick(row.entity, ['facilityName', 'id']))){
                    addItem(row.entity);
                } else {
                    removeItem(row.entity);
                }
                updateSelections();
                removeRedundantItemsFromCart();
            });

            gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
                _.each(rows, function(row){
                    if(_.find(gridApi.selection.getSelectedRows(), _.pick(row.entity, ['facilityName', 'id']))){
                        addItem(row.entity.entity);
                    } else {
                        removeItem(row.entity);
                    }
                });
                updateSelections();
                removeRedundantItemsFromCart();
            });

            if(isScroll){
                //scroll down more data callback (append data)
                gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                    page++;
                    getPage().then(function(results){
                        _.each(results, function(result){ gridOptions.data.push(result); });
                        if(results.length == 0) page--;
                        updateSelections();
                        updateScroll(results.length);
                    });
                });

                //scoll up more data at top callback (prepend data)
                gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {
                    page--;
                    getPage().then(function(results){
                        _.each(results.reverse(), function(result){ gridOptions.data.unshift(result); });
                        if(results.length == 0) page++;
                        updateSelections();
                        updateScroll(results.length);
                    });
                });
            } else {
                //pagination callback
                gridApi.pagination.on.paginationChanged($scope, function(_page, _pageSize) {
                    page = _page;
                    pageSize = _pageSize;
                    getPage().then(function(results){
                        gridOptions.data = results;
                        updateSelections();
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
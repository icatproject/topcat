'use strict';

angular
    .module('angularApp')
    .factory('BrowseEntitiesModel', BrowseEntitiesModel);

BrowseEntitiesModel.$inject = ['APP_CONFIG', 'Config', 'RouteUtils', 'uiGridConstants', 'DataManager', '$timeout', '$log'];

//TODO infinite scroll not working as it should when results are filtered. This is because the last page is determined by total items
//rather than the filtered total. We need to make another query to get the filtered total in order to make it work
//
//TODO sorting need fixing, ui-grid sorting is additive only rather than sorting by a single column. Queries are
//unable to do this at the moment. Do we want single column sort or multiple column sort. ui-grid currently does not
//support single column soting but users have submitted is as a feature request
function BrowseEntitiesModel(APP_CONFIG, Config, RouteUtils, uiGridConstants, DataManager, $timeout, $log){
    return {
        gridOptions : {},
        nextRouteSegment: null,

        /**
         * This function transpose the site config file to settings used by ui-grid
         *
         * @return {[type]} [description]
         */
        configToUIGridOptions : function(facility, currentEntityType) {
            $log.debug('BrowseEntitiesModel configToUIGridOptions called');
            //$log.debug('BrowseEntitiesModel configToUIGridOptions currentEntityType', currentEntityType);

            var gridOptions = Config.getEntityBrowseOptionsByFacilityName(APP_CONFIG, facility.keyName, currentEntityType);

            //$log.debug('BrowseEntitiesModel gridOptions', gridOptions);

            //do the work of transposing
            _.mapValues(gridOptions.columnDefs, function(value) {
                //replace filter condition to one expected by ui-grid
                if (angular.isDefined(value.filter.condition) && angular.isString(value.filter.condition)) {
                    value.filter.condition = uiGridConstants.filter[value.filter.condition.toUpperCase()];
                }

                //replace translate text
                if (angular.isDefined(value.translateDisplayName) && angular.isString(value.translateDisplayName)) {
                    value.displayName = value.translateDisplayName;
                    delete value.translateDisplayName;

                    value.headerCellFilter = 'translate';
                }

                //default type to string if not defined
                if (! angular.isDefined(value.type)) {
                    value.type = 'string';
                }

                if (angular.isDefined(value.sort) && angular.isObject(value.sort)) {
                    if (angular.isDefined(value.sort.direction) && angular.isString(value.sort.direction)) {
                        value.sort.direction = uiGridConstants[value.sort.direction.toUpperCase()];
                    }
                }

                //replace links
                if (angular.isDefined(value.link) && value.link === true) {
                    //$log.debug('link value', value);
                    delete value.link;

                    value.cellTemplate = '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" ui-sref="home.browse.facilities.{{grid.appScope.getNextRouteSegment()}}({facilityName : \'' + facility.keyName + '\', id : {{ 0 + row.entity.id}}})">{{row.entity.' + value.field + '}}</a></div>';
                }

                //add suppress remove sort
                if (! angular.isDefined(value.suppressRemoveSort)) {
                    //value.suppressRemoveSort = true;
                }

                return value;
            });

            return gridOptions;
        },


        init : function(facility, scope, currentEntityType, currentRouteSegment, sessions, $stateParams) {
            var options = this.configToUIGridOptions(facility, currentEntityType);
            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facility.keyName);
            var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);
            var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
            var pageSize = Config.getSitePageSize(APP_CONFIG, pagingType); //the number of rows for grid
            var scrollRowFromEnd = Config.getSiteConfig(APP_CONFIG).scrollRowFromEnd;
            var paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid
            var gridOptions = {};

            $log.debug('scope', scope);

            var paginateParams = {
                start: 0,
                numRows: pageSize,
                sortField: 'name',
                order: 'asc'
            };

            var getPage = function() {
                $log.debug('getpage called', paginateParams);

                DataManager.getData(currentRouteSegment, facility.keyName, sessions, $stateParams, paginateParams).then(function(data){
                    gridOptions.data = data.data;
                    gridOptions.totalItems = data.totalItems;

                    if (data.totalItems === 0) {
                        scope.isEmpty = true;
                        $log.info('isEmpty = true');
                    } else {
                        scope.isEmpty = false;
                        $log.info('isEmpty = false');
                    }

                    if (pagingType === 'scroll') {
                        scope.lastPage = Math.ceil(data.totalItems/pageSize);
                        scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                    }

                    $timeout(function() {
                        var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                        angular.forEach(rows, function(row) {
                            if (_.has(scope.mySelection, row.entity.id)) {
                                scope.gridApi.selection.selectRow(row.entity);
                            }
                        });

                    }, 0);
                }, function(){

                });
            };

            var appendPage = function() {
                $log.debug('append called', paginateParams);

                DataManager.getData(currentRouteSegment, facility.keyName, sessions, $stateParams, paginateParams).then(function(data){
                    gridOptions.data = gridOptions.data.concat(data.data);
                    gridOptions.totalItems = data.totalItems;

                    $timeout(function() {
                        var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                        angular.forEach(rows, function(row) {
                            if (_.has(scope.mySelection, row.entity.id)) {
                                scope.gridApi.selection.selectRow(row.entity);
                            }
                        });

                    }, 0);


                }, function(){

                });
            };

            var prependPage = function() {
                DataManager.getData(currentRouteSegment, facility.keyName, sessions, $stateParams, paginateParams).then(function(data){
                    gridOptions.data = data.data.concat(gridOptions.data);
                    gridOptions.totalItems = data.totalItems;

                    $timeout(function() {
                        var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                        angular.forEach(rows, function(row) {
                            if (_.has(scope.mySelection, row.entity.id)) {
                                scope.gridApi.selection.selectRow(row.entity);
                            }
                        });

                    }, 0);
                }, function(){

                });
            };

            gridOptions = {
                enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                //primaryKey: 'id',
                columnDefs: options.columnDefs,
                enableFiltering: options.enableFiltering,
                appScopeProvider: scope,
                //showGridFooter:true,
                useExternalSorting: true,
                useExternalFiltering: true,
                enableRowSelection: true,
                enableRowHeaderSelection: true,
                //modifierKeysToMultiSelect: true,
                multiSelect: true,
                flatEntityAccess: true,
                rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
            };

            if (pagingType === 'page') {
                getPage(paginateParams);

                $log.debug('pageType page');
                gridOptions.paginationPageSizes = paginationPageSizes;
                gridOptions.paginationPageSize = pageSize;
                gridOptions.useExternalPagination = true;

                gridOptions.onRegisterApi = function(gridApi) {
                    scope.gridApi = gridApi;

                    $log.debug('onRegisterApi', scope);

                    //sort callback
                    scope.gridApi.core.on.sortChanged(scope, function(grid, sortColumns) {
                        if (sortColumns.length === 0) {
                            //paginationOptions.sort = null;
                        } else {
                            //$log.debug('sortColumns[0].field', sortColumns[0].field);
                            paginateParams.sortField = sortColumns[0].field;
                            paginateParams.order = sortColumns[0].sort.direction;
                        }

                        $log.debug('sortChanged paginateParams', paginateParams);
                        getPage();
                    });

                    //pagination callback
                    scope.gridApi.pagination.on.paginationChanged(scope, function (newPage, pageSize) {
                        paginateParams.pageNumber = newPage;
                        paginateParams.pageSize = pageSize;

                        paginateParams.start = (paginateParams.pageNumber - 1) * paginateParams.pageSize;
                        paginateParams.numRows = paginateParams.pageSize;
                        getPage(paginateParams);
                    });

                    scope.gridApi.core.on.filterChanged(scope, function () {
                        $log.debug('this.grid', this.grid);
                        $log.debug('filterChanged column', this.grid.columns);

                        var grid = this.grid;
                        var sortOptions = [];

                        _.each(grid.columns, function(value, index) {
                            sortOptions.push({
                                field: grid.columns[index].field,
                                search: grid.columns[index].filters[0].term,
                            });
                        });

                        paginateParams.search = sortOptions;

                        getPage(paginateParams);
                    });

                    scope.gridApi.selection.on.rowSelectionChanged(scope, function(row){
                        $log.debug('rowSelectionChanged row', row);

                        if (row.isSelected === true) {
                            scope.mySelection[row.entity.id] = row.entity.id;
                        } else {
                            delete scope.mySelection[row.entity.id];
                        }

                        $log.debug('$scope.mySelection', scope.mySelection);
                    });

                    scope.gridApi.selection.on.rowSelectionChangedBatch (scope, function(rows){
                        $log.debug('rowSelectionChangedBatch  row', rows);

                        _.each(rows, function(row) {
                            if (row.isSelected === true) {
                                scope.mySelection[row.entity.id] = row.entity.id;
                            } else {
                                //$log.debug('deleting key', row.entity.id);
                                delete scope.mySelection[row.entity.id];
                            }
                        });

                        $log.debug('$scope.mySelection', scope.mySelection);
                    });

                };
            } else {
                $log.debug('pageType scroll');

                //gridOptions.infiniteScrollRowsFromEnd = pageSize;
                gridOptions.infiniteScrollRowsFromEnd = scrollRowFromEnd;
                gridOptions.infiniteScrollUp = true;
                gridOptions.infiniteScrollDown = true;

                scope.firstPage = 1;
                scope.lastPage = null;
                scope.currentPage = 1;

                gridOptions.onRegisterApi = function(gridApi) {
                    scope.gridApi = gridApi;

                    //sort callback
                    scope.gridApi.core.on.sortChanged(scope, function(grid, sortColumns) {
                        $log.debug('sortChanged callback grid', grid);
                        $log.debug('sortChanged callback sortColumns', sortColumns);

                        if (sortColumns.length === 0) {
                            //paginationOptions.sort = null;
                        } else {
                            sortColumns = [sortColumns[0]];
                            $log.debug('sort Column  by', sortColumns[0].field);
                            paginateParams.sortField = sortColumns[0].field;
                            paginateParams.order = sortColumns[0].sort.direction;
                        }

                        $log.debug('sortChanged callback sortColumns after', sortColumns);

                        scope.firstPage = 1;
                        scope.currentPage = 1;
                        paginateParams.start = 0;

                        $timeout(function() {
                            scope.gridApi.infiniteScroll.resetScroll(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        });

                        getPage(paginateParams);

                        $log.debug('sortChanged paginateParams', paginateParams);
                    });

                    scope.gridApi.infiniteScroll.on.needLoadMoreData(scope, function() {
                        $log.debug('needLoadMoreData called');
                        $log.debug('curentPage: ' , scope.currentPage, 'lastPage: ', scope.lastPage);
                        paginateParams.start = paginateParams.start + pageSize;
                        scope.gridApi.infiniteScroll.saveScrollPercentage();
                        appendPage(paginateParams);

                        $log.debug ('scrollUp: ', scope.firstPage - 1 > 0);
                        $log.debug ('scrollDown: ', scope.currentPage + 1 < scope.lastPage);

                        scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        scope.currentPage++;
                    });


                    scope.gridApi.infiniteScroll.on.needLoadMoreDataTop(scope, function() {
                        $log.debug('needLoadMoreDataTop called');
                        $log.debug('curentPage: ' , scope.currentPage, 'lastPage: ', scope.lastPage);
                        paginateParams.start = paginateParams.start - pageSize;
                        scope.gridApi.infiniteScroll.saveScrollPercentage();
                        prependPage(paginateParams);

                        $log.debug ('scrollUp: ', scope.firstPage -1 > 0);
                        $log.debug ('scrollDown: ', scope.currentPage + 1 < scope.lastPage);

                        scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        scope.currentPage--;
                    });

                    scope.gridApi.core.on.filterChanged(scope, function () {
                        $log.debug('this.grid', this.grid);
                        $log.debug('filterChanged column', this.grid.columns);

                        var grid = this.grid;
                        var sortOptions = [];

                        _.each(grid.columns, function(value, index) {
                            $log.debug('column index', index);
                            sortOptions.push({
                                field: grid.columns[index].field,
                                search: grid.columns[index].filters[0].term,
                            });
                        });

                        paginateParams.search = sortOptions;

                        scope.firstPage = 1;
                        scope.currentPage = 1;
                        paginateParams.start = 0;

                        $timeout(function() {
                            scope.gridApi.infiniteScroll.resetScroll(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        });

                        getPage(paginateParams);
                    });

                    scope.gridApi.selection.on.rowSelectionChanged(scope, function(row){
                        $log.debug('rowSelectionChanged row', row);

                        if (row.isSelected === true) {
                            scope.mySelection[row.entity.id] = row.entity.id;
                        } else {
                            $log.debug('deleting key', row.entity.id);
                            delete scope.mySelection[row.entity.id];
                        }

                        $log.debug('$scope.mySelection', scope.mySelection);
                    });

                    scope.gridApi.selection.on.rowSelectionChangedBatch (scope, function(rows){
                        $log.debug('rowSelectionChangedBatch  row', rows);

                        _.each(rows, function(row) {
                            if (row.isSelected === true) {
                                scope.mySelection[row.entity.id] = row.entity.id;
                            } else {
                                $log.debug('deleting key', row.entity.id);
                                delete scope.mySelection[row.entity.id];
                            }
                        });
                    });

                };

                getPage(paginateParams);
            }

            this.gridOptions = gridOptions;
            this.nextRouteSegment = nextRouteSegment;
        },


        getNextRouteSegment: function() {
            return this.nextRouteSegment;
        }

    };
}


'use strict';

angular
    .module('angularApp')
    .factory('MyDataModel', MyDataModel);

MyDataModel.$inject = ['$rootScope', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteService', 'uiGridConstants', 'DataManager', 'IdsManager', '$timeout', '$state', 'Cart', '$sessionStorage', 'usSpinnerService', 'moment', '$log'];

//TODO infinite scroll not working as it should when results are filtered. This is because the last page is determined by total items
//rather than the filtered total. We need to make another query to get the filtered total in order to make it work
//
//TODO sorting need fixing, ui-grid sorting is additive only rather than sorting by a single column. Queries are
//unable to do this at the moment. Do we want single column sort or multiple column sort. ui-grid currently does not
//support single column soting but users have submitted is as a feature request
function MyDataModel($rootScope, APP_CONFIG, Config, ConfigUtils, RouteService, uiGridConstants, DataManager, IdsManager, $timeout, $state, Cart, $sessionStorage, usSpinnerService, moment, $log){  //jshint ignore: line
    function hasField(options, field) {
        var result = false;
        //determine if field size has been defined
        _.each(options.columnDefs, function(col) {
            if (typeof col.field !== 'undefined' && col.field === field) {
                result = true;
                return false;
            }
        });

        return result;
    }

    function getDataPromise(entityType, sessions, facility, options) {
        var promise;

        switch(entityType) {
            case 'investigation':
                promise = DataManager.getMyInvestigations(sessions, facility, options);
                break;
            case 'dataset':
                promise = DataManager.getMyDatasets(sessions, facility, options);
            break;
        }

        return promise;
    }

    function getIncludesForRoutes(params, entityType, nextRouteSegment) {
        if (typeof params.includes === 'undefined') {
            params.includes = [];
        }

        if (entityType === 'investigation') {
            if (nextRouteSegment.indexOf('instrument') > -1) {
                if (params.includes.indexOf('investigation.investigationInstruments.instrument') === -1) {
                    params.includes.push('investigation.investigationInstruments.instrument');
                }
            }

            if (nextRouteSegment.indexOf('facilityCycle') > -1) {
                if (params.includes.indexOf('investigation.facility.facilityCycles') === -1) {
                    params.includes.push('investigation.facility.facilityCycles');
                }
            }
        }

        if (entityType === 'dataset') {
            //INCLUDE ds.investigation inv, inv.investigationInstruments.instrument, inv.facility.facilityCycles
            if (nextRouteSegment.indexOf('instrument') > -1) {
                if (params.includes.indexOf('dataset.investigation.investigationInstruments.instrument') === -1) {
                    params.includes.push('dataset.investigation inv');
                    params.includes.push('investigation.investigationInstruments.instrument');
                }
            }

            if (nextRouteSegment.indexOf('facilityCycle') > -1) {
                if (params.includes.indexOf('dataset.investigation inv') === -1) {
                    params.includes.push('dataset.investigation inv');
                }

                if (params.includes.indexOf('dataset.investigation.facility.facilityCycles') === -1) {
                    params.includes.push('investigation.facility.facilityCycles');
                }
            }
        }

        return params;
    }


    function getNextRouteStateParam(row, entityType, stateParams) {
        var params = {
            facilityName : row.entity.facilityName,
        };

        params[entityType + 'Id'] = row.entity.id;

        _.each(stateParams, function(value, key){
            params[key] = value;
        });

        if (typeof row.entity.nextRouteSegment !== 'undefined') {
            if (entityType === 'investigation') {
                if (row.entity.nextRouteSegment.indexOf('instrument') > -1) {
                    params.instrumentId = row.entity.investigationInstruments[0].instrument.id;
                }

                if (row.entity.nextRouteSegment.indexOf('facilityCycle') > -1) {
                    var investigationFacilityCycle = null;

                    _.each(row.entity.facility.facilityCycles, function(fc) {
                        if (moment(row.entity.startDate).isBetween(fc.startDate, fc.endDate) === true) {
                            investigationFacilityCycle = fc;
                            return false;
                        }
                    });

                    if (typeof investigationFacilityCycle !== 'undefined' && investigationFacilityCycle !== null) {
                        params.facilityCycleId = investigationFacilityCycle.id;
                    }

                }

                if (row.entity.nextRouteSegment.indexOf('proposal') > -1) {
                    params.proposalId = row.entity.name;
                }
            }

            if (entityType === 'dataset') {
                if (row.entity.nextRouteSegment.indexOf('instrument') > -1) {
                    params.instrumentId = row.entity.investigation.investigationInstruments[0].instrument.id;
                }

                if (row.entity.nextRouteSegment.indexOf('facilityCycle') > -1) {
                    var datasetFacilityCycle = null;

                    _.each(row.entity.investigation.facility.facilityCycles, function(fc) {
                        if (moment(row.entity.investigation.startDate).isBetween(fc.startDate, fc.endDate) === true) {
                            datasetFacilityCycle = fc;
                            return false;
                        }
                    });

                    if (typeof datasetFacilityCycle !== 'undefined' && datasetFacilityCycle !== null) {
                        params.facilityCycleId = datasetFacilityCycle.id;
                    }
                }

                if (row.entity.nextRouteSegment.indexOf('proposal') > -1) {
                    params.proposalId = row.entity.investigation.name;
                }

                if (row.entity.nextRouteSegment.indexOf('investigation') > -1) {
                    params.investigationId = row.entity.investigation.id;
                }
            }
        }

        return params;
    }

    return {
        gridOptions : {},
        nextRouteSegment: null,
        facility: null,
        stateParams: null,
        entityType: null,
        loggedInFacilitites: null,

        /**
         * This function transpose the site config file to settings used by ui-grid
         *
         * @return {[type]} [description]
         */
        configToUIGridOptions : function(entityType) {
            var gridOptions = Config.getSiteMyDataGridOptions(APP_CONFIG)[entityType];

            //do the work of transposing
            _.mapValues(gridOptions.columnDefs, function(value) {
                //replace filter condition to one expected by ui-grid
                if (angular.isDefined(value.filter)) {
                    if (angular.isDefined(value.filter.condition) && angular.isString(value.filter.condition)) {
                        value.filter.condition = uiGridConstants.filter[value.filter.condition.toUpperCase()];
                    }
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
                    delete value.link;
                    value.cellTemplate = '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" href="{{grid.appScope.getNextRouteUrl(row)}}">{{row.entity.' + value.field + '}}</a></div>';
                }

                //add suppress remove sort
                if (! angular.isDefined(value.suppressRemoveSort)) {
                    //value.suppressRemoveSort = true;
                }

                //size column
                //make sure for only investigation and dataset
                if (entityType === 'investigation' || entityType === 'dataset') {
                    if(angular.isDefined(value.field) && value.field === 'size') {
                        value.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}" spinner-key="spinner-size-{{row.uid}}" class="grid-cell-spinner"></span><span>{{ row.entity.size | bytes }}</span></div>';
                        value.enableSorting = false;
                        value.enableFiltering = false;
                    }
                }

                if(angular.isDefined(value.field) && value.field === 'facilityTitle') {
                    value.cellTemplate = '<div class="ui-grid-cell-contents"><span>{{ row.entity.facilityTitle }}</span></div>';
                    value.enableSorting = false;
                    value.enableFiltering = false;
                }

                return value;
            });

            return gridOptions;
        },


        init : function(facilityObjs, scope, entityType, currentRouteSegment, sessions, $stateParams) {
            var options = this.configToUIGridOptions(entityType);
            var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
            var pageSize = Config.getSitePageSize(APP_CONFIG, pagingType); //the number of rows for grid
            var scrollRowFromEnd = Config.getSiteConfig(APP_CONFIG).scrollRowFromEnd;
            var paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid
            var gridOptions = {};
            var hasSizeField = hasField(options, 'size');

            var enableSelection = function() {
                if (angular.isDefined(options.enableSelection) && options.enableSelection === true) {
                    return true;
                } else {
                    return false;
                }
            };

            var paginateParams = {
                start: 0,
                numRows: pageSize,
                sortField: 'name',
                order: 'asc',
                includes: options.includes
            };

            gridOptions.totalItems = 0; //initiate totalItems

            /**
             * Loads data for both pagination and infinte scroll. This method is called by ui-grid to load the first page of data
             * for infinite scroll and to load next page data for paginated pages
             * @return {[type]} [description]
             */
            var getPage = function() {
                ConfigUtils.getLoggedInFacilities(facilityObjs, sessions).then(function (data){
                        _.each(data, function(facility) {
                            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facility.facilityName);
                            var nextRouteSegment = RouteService.getNextRouteSegmentName(structure, entityType);
                            paginateParams = getIncludesForRoutes(paginateParams, entityType, nextRouteSegment);

                            var options = _.extend($stateParams, paginateParams);
                            options.user = true;

                            getDataPromise(entityType, sessions, facility, options).then(function(data){
                                gridOptions.totalItems = gridOptions.totalItems || 0;
                                //$log.log('gridOptions.totalItems', gridOptions.totalItems);
                                gridOptions.data = gridOptions.data.concat(data.data);
                                gridOptions.totalItems = gridOptions.totalItems + data.totalItems;

                                if (pagingType === 'scroll') {
                                    scope.lastPage = Math.ceil(gridOptions.totalItems/pageSize);
                                    scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                                }

                                $timeout(preSelectAndGetSize, 0);




                                function preSelectAndGetSize() {
                                    var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                                    //pre-select items in cart here
                                    _.each(rows, function(row) {
                                        //fill size data
                                        if (hasSizeField) {
                                            if (entityType === 'investigation' || entityType === 'dataset') {
                                                //inject information into row data
                                                if (typeof row.entity.facilityName === 'undefined') {
                                                    row.entity.nextRouteSegment = nextRouteSegment;
                                                    row.entity.facilityTitle = facility.title;
                                                    row.entity.facilityName = facility.facilityName;
                                                    row.entity.nextRouteStateParam = getNextRouteStateParam(row, entityType, $stateParams);
                                                }

                                                if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                                    var params = {};
                                                    params[entityType  + 'Ids'] = row.entity.id;

                                                    usSpinnerService.spin('spinner-size-' + row.uid);

                                                    IdsManager.getSize(sessions, facility, params).then(function(data){
                                                        row.entity.size = parseInt(data);
                                                        usSpinnerService.stop('spinner-size-' + row.uid);
                                                    }, function() {
                                                        row.entity.size = -1;
                                                    });
                                                }


                                            }
                                        }

                                        if (Cart.hasItem(facility.facilityName, entityType, row.entity.id)) {
                                           scope.gridApi.selection.selectRow(row.entity);
                                        }
                                    });
                                }
                            }, function(){

                            });
                        });

                    }, function(){
                        throw new Error('Unable to retrieve logged in facilitites');
                    }
                );
            };

            /**
             * Loads data for infinite scroll. This method is call by ui-grid when user scrolls up
             * @return {[type]} [description]
             */
            var appendPage = function() {
                ConfigUtils.getLoggedInFacilities(facilityObjs, sessions).then(function (data){
                    _.each(data, function(facility) {
                        var options = _.extend($stateParams, paginateParams);
                        options.user = true;

                        getDataPromise(entityType, sessions, facility, options).then(function(data){
                            gridOptions.data = gridOptions.data.concat(data.data);
                            gridOptions.totalItems = data.totalItems;

                            $timeout(function() {
                                var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                                //pre-select items in cart here
                                _.each(rows, function(row) {
                                    //fill size data
                                    if (hasSizeField) {
                                        if (entityType === 'investigation' || entityType === 'dataset') {
                                            if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                                var params = {};
                                                params[entityType  + 'Ids'] = row.entity.id;

                                                //disable until icat GC problem is fixed
                                                IdsManager.getSize(sessions, facility, params).then(function(data){
                                                    row.entity.size = parseInt(data);
                                                }, function() {
                                                    row.entity.size = -1;
                                                });
                                            }

                                            //inject facilityTitle and facilityName to row data
                                            if (typeof row.entity.facilityName === 'undefined') {
                                                row.entity.facilityTitle = facility.title;
                                                row.entity.facilityName = facility.facilityName;
                                            }
                                        }
                                    }

                                    if (Cart.hasItem(facility.facilityName, entityType, row.entity.id)) {
                                       scope.gridApi.selection.selectRow(row.entity);
                                    }
                                });

                            }, 0);


                        }, function(){

                        });

                    });
                });
            };

            /**
             * Loads data for infinite scroll. This method is call by ui-grid when user scrolls down
             * @return {[type]} [description]
             */
            var prependPage = function() {
                ConfigUtils.getLoggedInFacilities(facilityObjs, sessions).then(function (data){
                    _.each(data, function(facility) {
                        var options = _.extend($stateParams, paginateParams);
                        options.user = true;

                        getDataPromise(entityType, sessions, facility, options).then(function(data){
                            gridOptions.data = gridOptions.data.concat(data.data);
                            gridOptions.totalItems = data.totalItems;

                            $timeout(function() {
                                var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                                //pre-select items in cart here
                                _.each(rows, function(row) {
                                    //fill size data
                                    if (hasSizeField) {
                                        if (entityType === 'investigation' || entityType === 'dataset') {
                                            if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                                var params = {};
                                                params[entityType  + 'Ids'] = row.entity.id;

                                                //disable until icat GC problem is fixed
                                                IdsManager.getSize(sessions, facility, params).then(function(data){
                                                    row.entity.size = parseInt(data);
                                                }, function() {
                                                    row.entity.size = -1;
                                                });
                                            }

                                            //inject facilityTitle and facilityName to row data
                                            if (typeof row.entity.facilityName === 'undefined') {
                                                row.entity.facilityTitle = facility.title;
                                                row.entity.facilityName = facility.facilityName;
                                            }
                                        }
                                    }

                                    if (Cart.hasItem(facility.facilityName, entityType, row.entity.id)) {
                                       scope.gridApi.selection.selectRow(row.entity);
                                    }
                                });

                            }, 0);


                        }, function(){

                        });

                    });
                });
            };

            var refreshSelection = function() {
                $timeout(function() {
                    var rows = scope.gridApi.core.getVisibleRows(scope.gridApi.grid);

                    //pre-select items in cart here
                    _.each(rows, function(row) {
                        if (Cart.hasItem(row.entity.facilityName, entityType, row.entity.id)) {
                           scope.gridApi.selection.selectRow(row.entity);
                        } else {
                            scope.gridApi.selection.unSelectRow(row.entity);
                        }
                    });

                }, 0);
            };

            $rootScope.$on('Cart:itemRemoved', function(){
                refreshSelection(scope);
            });

            gridOptions = {
                enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                //primaryKey: 'id',
                columnDefs: options.columnDefs,
                enableFiltering: options.enableFiltering,
                appScopeProvider: scope,
                //showGridFooter:true, //TODO
                useExternalSorting: true,
                useExternalFiltering: true,
                enableRowSelection: enableSelection(),
                enableRowHeaderSelection: enableSelection(),
                enableSelectAll: false,
                //modifierKeysToMultiSelect: true,
                multiSelect: true,
                //flatEntityAccess: true,
                rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
            };

            if (pagingType === 'page') {
                getPage(paginateParams);

                gridOptions.paginationPageSizes = paginationPageSizes;
                gridOptions.paginationPageSize = pageSize;
                gridOptions.useExternalPagination = true;

                gridOptions.onRegisterApi = function(gridApi) {
                    scope.gridApi = gridApi;

                    //sort callback
                    scope.gridApi.core.on.sortChanged(scope, function(grid, sortColumns) {
                        if (sortColumns.length === 0) {
                            //paginationOptions.sort = null;
                        } else {
                            paginateParams.sortField = sortColumns[0].field;
                            paginateParams.order = sortColumns[0].sort.direction;
                        }

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

                    scope.gridApi.selection.on.rowSelectionChanged(scope, function(row){ //jshint ignore: line
                        if (row.isSelected === true) {
                            Cart.addItem(row.entity.facilityName, entityType, row.entity.id, row.entity.name);
                        } else {
                            Cart.removeItem(row.entity.facilityName, entityType, row.entity.id);
                        }
                    });

                    scope.gridApi.selection.on.rowSelectionChangedBatch (scope, function(rows){ //jshint ignore: line
                        var addedItems = [];
                        var removedItems = [];

                        _.each(rows, function(row) {
                            var item = {
                                facilityName: row.entity.facilityName,
                                entityType: entityType,
                                id: row.entity.id,
                                name: row.entity.name
                            };

                            if (row.isSelected === true) {
                                addedItems.push(item);
                            } else {
                                removedItems.push(item);
                            }
                        });

                        if (addedItems.length !== 0) {
                            Cart.addItems(addedItems);
                        }

                        if (removedItems.length !== 0) {
                            Cart.removeItems(removedItems);
                        }
                    });

                };
            } else {
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
                        if (sortColumns.length === 0) {
                            //paginationOptions.sort = null;
                        } else {
                            sortColumns = [sortColumns[0]];
                            paginateParams.sortField = sortColumns[0].field;
                            paginateParams.order = sortColumns[0].sort.direction;
                        }

                        scope.firstPage = 1;
                        scope.currentPage = 1;
                        paginateParams.start = 0;

                        $timeout(function() {
                            scope.gridApi.infiniteScroll.resetScroll(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        });

                        getPage(paginateParams);

                        //$log.debug('sortChanged paginateParams', paginateParams);
                    });

                    scope.gridApi.infiniteScroll.on.needLoadMoreData(scope, function() {
                        //$log.debug('needLoadMoreData called');
                        //$log.debug('curentPage: ' , scope.currentPage, 'lastPage: ', scope.lastPage);
                        paginateParams.start = paginateParams.start + pageSize;
                        scope.gridApi.infiniteScroll.saveScrollPercentage();
                        appendPage(paginateParams);

                        //$log.debug ('scrollUp: ', scope.firstPage - 1 > 0);
                        //$log.debug ('scrollDown: ', scope.currentPage + 1 < scope.lastPage);

                        scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        scope.currentPage++;
                    });


                    scope.gridApi.infiniteScroll.on.needLoadMoreDataTop(scope, function() {
                        //$log.debug('needLoadMoreDataTop called');
                        //$log.debug('curentPage: ' , scope.currentPage, 'lastPage: ', scope.lastPage);
                        paginateParams.start = paginateParams.start - pageSize;
                        scope.gridApi.infiniteScroll.saveScrollPercentage();
                        prependPage(paginateParams);

                        //$log.debug ('scrollUp: ', scope.firstPage -1 > 0);
                        //$log.debug ('scrollDown: ', scope.currentPage + 1 < scope.lastPage);

                        scope.gridApi.infiniteScroll.dataLoaded(scope.firstPage - 1 > 0, scope.currentPage + 1 < scope.lastPage);
                        scope.currentPage--;
                    });

                    scope.gridApi.core.on.filterChanged(scope, function () {
                        //$log.debug('this.grid', this.grid);
                        //$log.debug('filterChanged column', this.grid.columns);

                        var grid = this.grid;
                        var sortOptions = [];

                        _.each(grid.columns, function(value, index) {
                            //$log.debug('column index', index);
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

                    scope.gridApi.selection.on.rowSelectionChanged(scope, function(row){ //jshint ignore: line
                        if (row.isSelected === true) {
                            Cart.addItem(row.entity.facilityName, entityType, row.entity.id, row.entity.name);
                        } else {
                            Cart.removeItem(row.entity.facilityName, entityType, row.entity.id);
                        }
                    });

                    scope.gridApi.selection.on.rowSelectionChangedBatch (scope, function(rows){ //jshint ignore: line
                        var addedItems = [];
                        var removedItems = [];

                        _.each(rows, function(row) {
                            var item = {
                                facilityName: row.entity.facilityName,
                                entityType: entityType,
                                id: row.entity.id,
                                name: row.entity.name
                            };

                            if (row.isSelected === true) {
                                addedItems.push(item);
                            } else {
                                removedItems.push(item);
                            }
                        });

                        if (addedItems.length !== 0) {
                            Cart.addItems(addedItems);
                        }

                        if (removedItems.length !== 0) {
                            Cart.removeItems(removedItems);
                        }
                    });

                };

                getPage(paginateParams);
            }

            this.gridOptions = gridOptions;
            //this.nextRouteSegment = nextRouteSegment;
            //this.facility = facility;
            this.entityType = entityType;
            this.stateParams = angular.copy($stateParams);
        },

        getNextRouteUrl: function (row) {
            //$log.log('row', row);
            return $state.href('home.browse.facility.' + row.entity.nextRouteSegment, row.entity.nextRouteStateParam);
        }
    };
}


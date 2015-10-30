'use strict';

angular
    .module('angularApp')
    .service('MyDataModel', MyDataModel);

MyDataModel.$inject = ['$rootScope', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteService', 'uiGridConstants', 'DataManager', 'IdsManager', '$timeout', '$state', 'Cart', '$sessionStorage', 'usSpinnerService', 'moment'];

//TODO infinite scroll not working as it should when results are filtered. This is because the last page is determined by total items
//rather than the filtered total. We need to make another query to get the filtered total in order to make it work
//
//TODO sorting need fixing, ui-grid sorting is additive only rather than sorting by a single column. Queries are
//unable to do this at the moment. Do we want single column sort or multiple column sort. ui-grid currently does not
//support single column soting but users have submitted is as a feature request
function MyDataModel($rootScope, APP_CONFIG, Config, ConfigUtils, RouteService, uiGridConstants, DataManager, IdsManager, $timeout, $state, Cart, $sessionStorage, usSpinnerService, moment){
    var self = this;

    /**
     * Check if column has a particular field
     * @param  {[type]}  options [description]
     * @param  {[type]}  field   [description]
     * @return {Boolean}         [description]
     */
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

    /**
     * Return a promise with the results
     * @param  {[type]} entityType [description]
     * @param  {[type]} sessions   [description]
     * @param  {[type]} facility   [description]
     * @param  {[type]} options    [description]
     * @return {[type]}            [description]
     */
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

    /**
     * Add the nessessary includes in order to build next routes
     * @param  {[type]} params           [description]
     * @param  {[type]} entityType       [description]
     * @param  {[type]} nextRouteSegment [description]
     * @return {[type]}                  [description]
     */
    function getIncludesForRoutes(params, entityType, nextRouteSegment) {
        //if (typeof params.includes === 'undefined') {
            params.includes = params.includes || [];
        //}

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

    /**
     * Get the next router parameters required to build a url link
     * @param  {[type]} row         [description]
     * @param  {[type]} entityType  [description]
     * @param  {[type]} stateParams [description]
     * @return {[type]}             [description]
     */
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

    /**
     * Return the first column with a sort.direction key
     * @param  {[type]} columnDefs [description]
     * @return {[type]}            [description]
     */
    function getDefaultSort(columnDefs) {
        var hasDefaultSortColumn = _.filter(columnDefs, function(columnDef) {
            if (typeof columnDef.sort !== 'undefined' && angular.isObject(columnDef.sort)) {
                if (typeof columnDef.sort.direction !== 'undefined' && angular.isString(columnDef.sort.direction)) {
                    return true;
                }
            }

            return false;
        });

        return hasDefaultSortColumn;
    }



    /**
     * Converts the site config file to settings used by ui-grid
     *
     * @return {[type]} [description]
     */
    function configToUIGridOptions(entityType) {
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

            if (/\[\d+\]/g.test(value.field)) {
                var split = value.field.split(/\[\d+\]/);
                var field = split[0];

                if (typeof value.cellTemplate !== 'undefined' && value.cellTemplate.indexOf('glyphicon') === -1) {
                    value.cellTemplate = value.cellTemplate.replace(/<\/div>$/, ' <span ng-if="row.entity.' + field + '.length > 1" class="glyphicon glyphicon-list" tooltip-html-unsafe="{{grid.appScope.getFieldValuesAsHtmlList(row.entity, \'' + value.field + '\')}}" tooltip-placement="right" tooltip-append-to-body="true"></span></div>');
                } else {
                    value.cellTemplate = '<div class="ui-grid-cell-contents" title="TOOLTIP">{{COL_FIELD CUSTOM_FILTERS}} <span ng-if="row.entity.' + field + '.length > 1" class="glyphicon glyphicon-list" tooltip-html-unsafe="{{grid.appScope.getFieldValuesAsHtmlList(row.entity, \'' + value.field + '\')}}" tooltip-placement="right" tooltip-append-to-body="true"></span></div>';
                }
            }

            return value;
        });

        return gridOptions;
    }

    /**
     * Determine if items in a grid are selectable
     * @return {[type]} [description]
     */
    function enableSelection() {
        if (angular.isDefined(self.options.enableSelection) && self.options.enableSelection === true) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the grid options
     * @param {[type]} gridOptions [description]
     */
    function setGridOptions(gridOptions) {
        self.gridOptions = _.extend(gridOptions, {
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            columnDefs: self.options.columnDefs,
            enableFiltering: self.options.enableFiltering,
            useExternalSorting: true,
            useExternalFiltering: true,
            enableRowSelection: enableSelection(),
            enableRowHeaderSelection: enableSelection(),
            enableSelectAll: false,
            multiSelect: true,
            rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
        });

        if (self.pagingType === 'page') {
            self.gridOptions.paginationPageSizes = self.paginationPageSizes;
            self.gridOptions.paginationPageSize = self.pageSize;
            self.gridOptions.useExternalPagination = true;
        } else {
            self.gridOptions.infiniteScrollRowsFromEnd = self.scrollRowFromEnd;
            self.gridOptions.infiniteScrollUp = true;
            self.gridOptions.infiniteScrollDown = true;
        }
    }

    this.init = function(facilityObjs, scope, entityType, currentRouteSegment, sessions, stateParams) {
        self.facilityObjs = facilityObjs;
        self.scope = scope;
        self.entityType = entityType;
        self.currentRouteSegment = currentRouteSegment;
        self.sessions = sessions;
        self.stateParams = angular.copy(stateParams);

        self.options = configToUIGridOptions(entityType);
        self.pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        self.pageSize = Config.getSitePageSize(APP_CONFIG, self.pagingType); //the number of rows for grid
        self.scrollRowFromEnd = Config.getSiteScrollRowFromEnd(APP_CONFIG, self.pagingType);
        self.paginationPageSizes = Config.getPaginationPageSizes(APP_CONFIG, self.pagingType); //the number of rows for grid
        self.hasSizeField = hasField(self.options, 'size');

        setGridOptions(self.scope.gridOptions);

        //get the default sort columnDef
        var defaultSortColumn = getDefaultSort(self.gridOptions.columnDefs);

        //set default column if no default column set
        if (defaultSortColumn.length === 0) {
            defaultSortColumn.push({
                field : 'startDate',
                sort : {
                    direction : 'desc'
                }
            });
        }

        self.paginateParams = {
            start: 0,
            numRows: self.pageSize,
            sortField: defaultSortColumn[0].field,
            order: defaultSortColumn[0].sort.direction,
            includes: self.options.includes
        };

        self.gridOptions.totalItems = 0; //initiate totalItems

        /**
         * Loads data for both pagination and infinte scroll. This method is called by ui-grid to load the first page of data
         * for infinite scroll and to load next page data for paginated pages
         * @return {[type]} [description]
         */

    };

    //@TODO applyFilterAndGetPage() and filterChanged() should be combined and
    //refactored as they work pretty much the same. filterChanged() uses
    //gridApi.grid.colDef while applyFilterAndGetPage() uses gridOptions.columnDefs
    //to build the search terms. There should be no reason why filterChanged() cannot
    //use gridOptions.columnDefs. applyFilterAndGetPage() however, cannot use
    //grid.colDef as doesn't seem possible to retrieve it.
    this.applyFilterAndGetPage = function (columnDefs) {
        var sortOptions = [];

        _.each(columnDefs, function(value, index) {
            var searchTerms = [];
            var isValid = true;

            var columnType = 'string';
            if (typeof columnDefs[index].type !== 'undefined') {
                columnType = columnDefs[index].type;
            }

            if (typeof columnDefs[index].filter !== 'undefined' || typeof columnDefs[index].filters !== 'undefined') {
                if (columnType === 'string') {
                    if (typeof columnDefs[index].filter !== 'undefined') {
                        searchTerms.push(columnDefs[index].filter.term);
                    }
                }

                if (columnType === 'date') {
                    //filter or filters?
                    if (typeof columnDefs[index].filters !== 'undefined') {
                        var filterCount = columnDefs[index].filters.length;

                        if (filterCount === 1) {
                            //validate term entered is a valid date before requesting page
                            _.each(columnDefs[index].filters, function(filter) {
                                if (typeof filter.term !== 'undefined' && filter.term !== null && filter.term.trim() !== '') {
                                    if (filter.term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                        searchTerms.push(columnDefs[index].filters[0].term);
                                    }
                                }
                            });
                        } else if (filterCount > 1) {
                            //validate term entered is a valid date before requesting page
                            if ((typeof columnDefs[index].filters[0].term !== 'undefined') && (typeof columnDefs[index].filters[1].term !== 'undefined')) {
                                if (typeof columnDefs[index].filters[0].term !== 'undefined' && columnDefs[index].filters[0].term !== null && columnDefs[index].filters[0].term.trim() !== '') {
                                    if (columnDefs[index].filters[0].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                        isValid = false;
                                    }
                                }

                                if (typeof columnDefs[index].filters[1].term !== 'undefined' && columnDefs[index].filters[1].term !== null && columnDefs[index].filters[1].term.trim() !== '') {
                                    if (columnDefs[index].filters[1].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                        isValid = false;
                                    }
                                }
                            } else if (! ((typeof columnDefs[index].filters[0].term === 'undefined') && (typeof columnDefs[index].filters[1].term === 'undefined'))) {
                                isValid = false;
                            }

                            if (isValid) {
                                searchTerms.push(columnDefs[index].filters[0].term);
                                searchTerms.push(columnDefs[index].filters[1].term);
                            }
                        }
                    } else if (typeof columnDefs[index].filter !== 'undefined') {
                        if (typeof columnDefs[index].filter.term !== 'undefined' && columnDefs[index].filter.term !== null && columnDefs[index].filter.term.trim() !== '') {
                            if (columnDefs[index].filter.term.match(/\d{4}\-\d{2}\-\d{2}/) !== null ) {
                                searchTerms.push(columnDefs[index].filter.term);
                            }
                        }

                    }
                }

                sortOptions.push({
                    field: columnDefs[index].field,
                    search: searchTerms,
                    type: columnType,
                    isValid: isValid
                });
            }
        });

        self.paginateParams.search = sortOptions;

        //set parameters to go back to the first page if page type is scroll
        if (self.pagingType === 'scroll') {
            self.scope.firstPage = 1;
            self.scope.currentPage = 1;
            self.paginateParams.start = 0;

            $timeout(function() {
                self.scope.gridApi.infiniteScroll.resetScroll(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
            });
        }

        self.getPage();
    };


    this.getPage = function() {
        ConfigUtils.getLoggedInFacilities(self.facilityObjs, self.sessions).then(function (data){

            //var totalItemsFromLoggedInServers = 0;

            _.each(data, function(facility) {
                var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facility.facilityName);
                var nextRouteSegment = RouteService.getNextRouteSegmentName(structure, self.entityType);

                self.paginateParams = getIncludesForRoutes(self.paginateParams, self.entityType, nextRouteSegment);

                var options = _.extend(self.stateParams, self.paginateParams);
                options.user = true;

                //reset data
                self.gridOptions.data = [];

                getDataPromise(self.entityType, self.sessions, facility, options).then(function(data){
                    self.gridOptions.totalItems = 0;
                    self.gridOptions.data = self.gridOptions.data.concat(data.data);
                    self.gridOptions.totalItems = self.gridOptions.totalItems + data.totalItems;

                    if (self.pagingType === 'scroll') {
                        self.scope.lastPage = Math.ceil(self.gridOptions.totalItems / self.pageSize);
                        self.scope.gridApi.infiniteScroll.dataLoaded(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
                    }

                    $timeout(preSelectAndGetSize, 0);

                    function preSelectAndGetSize() {
                        var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                        //pre-select items in cart here
                        _.each(rows, function(row) {
                            //fill size data
                            if (self.hasSizeField) {
                                if (self.entityType === 'investigation' || self.entityType === 'dataset') {
                                    //inject information into row data
                                    if (typeof row.entity.facilityName === 'undefined') {
                                        row.entity.nextRouteSegment = nextRouteSegment;
                                        row.entity.facilityTitle = facility.title;
                                        row.entity.facilityName = facility.facilityName;
                                        row.entity.nextRouteStateParam = getNextRouteStateParam(row, self.entityType, self.stateParams);
                                    }

                                    if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                        var params = {};
                                        params[self.entityType  + 'Ids'] = row.entity.id;

                                        usSpinnerService.spin('spinner-size-' + row.uid);

                                        IdsManager.getSize(self.sessions, facility, params).then(function(data){
                                            row.entity.size = parseInt(data);
                                            usSpinnerService.stop('spinner-size-' + row.uid);
                                        }, function() {
                                            row.entity.size = -1;
                                        });
                                    }
                                }
                            }

                            if (Cart.hasItem(facility.facilityName, self.entityType, row.entity.id)) {
                                //the below selectRow api call fires the rowSelectionChanged callback which in turn calls
                                //Cart.addItem(). The Cart.addItem() broadcast a Cart::Change which fires a Cart.save().
                                //Since we only need to mark the row as selected, we do not want to preform a save to the
                                //backend. To get around this, we add a fromEvent property to the row which can be picked up
                                //by rowSelectionChanged and act accordingly.
                                row.fromEvent = 'preSelection';
                                self.scope.gridApi.selection.selectRow(row.entity);
                            }
                        });
                    }

                }, function(){

                });
            });
        }, function(){
            throw new Error('Unable to retrieve logged in facilitites');
        });
    };

    /**
     * Loads data for infinite scroll. This method is call by ui-grid when user scrolls up
     * @return {[type]} [description]
     */
    this.appendPage = function() {
        ConfigUtils.getLoggedInFacilities(self.facilityObjs, self.sessions).then(function (data){
            _.each(data, function(facility) {
                var options = _.extend(self.stateParams, self.paginateParams);
                options.user = true;

                getDataPromise(self.entityType, self.sessions, facility, options).then(function(data){
                    self.gridOptions.data = self.gridOptions.data.concat(data.data);
                    //self.gridOptions.totalItems = data.totalItems;

                    $timeout(function() {
                        var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                        //pre-select items in cart here
                        _.each(rows, function(row) {
                            //fill size data
                            if (self.hasSizeField) {
                                if (self.entityType === 'investigation' || self.entityType === 'dataset') {
                                    if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                        var params = {};
                                        params[self.entityType  + 'Ids'] = row.entity.id;

                                        //disable until icat GC problem is fixed
                                        IdsManager.getSize(self.sessions, facility, params).then(function(data){
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

                            if (Cart.hasItem(facility.facilityName, self.entityType, row.entity.id)) {
                               row.fromEvent = 'preSelection';
                               self.scope.gridApi.selection.selectRow(row.entity);
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
    this.prependPage = function() {
        ConfigUtils.getLoggedInFacilities(self.facilityObjs, self.sessions).then(function (data){
            _.each(data, function(facility) {
                var options = _.extend(self.stateParams, self.paginateParams);
                options.user = true;

                getDataPromise(self.entityType, self.sessions, facility, options).then(function(data){
                    self.gridOptions.data = self.gridOptions.data.concat(data.data);
                    //self.gridOptions.totalItems = data.totalItems;

                    $timeout(function() {
                        var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                        //pre-select items in cart here
                        _.each(rows, function(row) {
                            //fill size data
                            if (self.hasSizeField) {
                                if (self.entityType === 'investigation' || self.entityType === 'dataset') {
                                    if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                        var params = {};
                                        params[self.entityType  + 'Ids'] = row.entity.id;

                                        //disable until icat GC problem is fixed
                                        IdsManager.getSize(self.sessions, facility, params).then(function(data){
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

                            if (Cart.hasItem(facility.facilityName, self.entityType, row.entity.id)) {
                               row.fromEvent = 'preSelection';
                               self.scope.gridApi.selection.selectRow(row.entity);
                            }
                        });

                    }, 0);


                }, function(){

                });

            });
        });
    };

    this.refreshSelection = function() {
        $timeout(function() {
            var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

            //pre-select items in cart here
            //
            _.each(rows, function(row) {
                row.fromEvent = 'preSelection';

                if (Cart.hasItem(row.entity.facilityName, self.entityType, row.entity.id)) {
                    self.scope.gridApi.selection.selectRow(row.entity);
                } else {
                    self.scope.gridApi.selection.unSelectRow(row.entity);
                }
            });

        }, 0);
    };


    this.sortChanged = function(grid, sortColumns) {
        if (sortColumns.length === 0) {
            //paginationOptions.sort = null;
        } else {
            self.paginateParams.sortField = sortColumns[0].field;
            self.paginateParams.order = sortColumns[0].sort.direction;
        }

        self.getPage();
    };


    this.paginationChanged = function (newPage, pageSize) {
        self.paginateParams.pageNumber = newPage;
        self.paginateParams.pageSize = pageSize;

        self.paginateParams.start = (self.paginateParams.pageNumber - 1) * self.paginateParams.pageSize;
        self.paginateParams.numRows = self.paginateParams.pageSize;
        self.getPage(self.paginateParams);
    };

    this.sortChangedForScroll = function(grid, sortColumns) {
        if (sortColumns.length === 0) {
            //paginationOptions.sort = null;
        } else {
            sortColumns = [sortColumns[0]];
            self.paginateParams.sortField = sortColumns[0].field;
            self.paginateParams.order = sortColumns[0].sort.direction;
        }

        //set parameters to go back to the first page if page type is scroll
        if (self.pagingType === 'scroll') {
            self.scope.firstPage = 1;
            self.scope.currentPage = 1;
            self.paginateParams.start = 0;

            $timeout(function() {
                self.scope.gridApi.infiniteScroll.resetScroll(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
            });
        }

        self.getPage();
    };


    this.needLoadMoreData = function() {
        self.paginateParams.start = self.paginateParams.start + self.pageSize;
        self.scope.gridApi.infiniteScroll.saveScrollPercentage();
        self.appendPage(self.paginateParams);

        self.scope.gridApi.infiniteScroll.dataLoaded(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
        self.scope.currentPage++;
    };


    this.needLoadMoreDataTop = function() {
        self.paginateParams.start = self.paginateParams.start - self.pageSize;
        self.scope.gridApi.infiniteScroll.saveScrollPercentage();
        self.prependPage(self.paginateParams);

        self.scope.gridApi.infiniteScroll.dataLoaded(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
        self.scope.currentPage--;
    };

    this.filterChanged = function (columns) {
        var sortOptions = [];

        _.each(columns, function(value, index) {
            var searchTerms = [];
            var isValid = true;

            var columnType = 'string';
            if (typeof columns[index].colDef.type !== 'undefined') {
                columnType = columns[index].colDef.type;
            }

            if (typeof columns[index].filters !== 'undefined') {
                if (columnType === 'string') {
                    searchTerms.push(columns[index].filters[0].term);
                }

                if (columnType === 'date') {
                    //determine if 2 filters was configured
                    var filterCount = columns[index].filters.length;

                    if (filterCount === 1) {
                        searchTerms.push(columns[index].filters[0].term);

                        //validate term entered is a valid date before requesting page
                        _.each(columns[index].filters, function(filter) {
                            if (typeof filter.term !== 'undefined' && filter.term !== null && filter.term.trim() !== '') {
                                if (filter.term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                    isValid = false;
                                }
                            }
                        });
                    } else if (filterCount > 1) {
                        //only allow 2 filters and ignore the rest if defined
                        searchTerms.push(columns[index].filters[0].term);
                        searchTerms.push(columns[index].filters[1].term);

                        //validate term entered is a valid date before requesting page
                        if ((typeof columns[index].filters[0].term !== 'undefined') && (typeof columns[index].filters[1].term !== 'undefined')) {
                            if (typeof columns[index].filters[0].term !== 'undefined' && columns[index].filters[0].term !== null && columns[index].filters[0].term.trim() !== '') {
                                if (columns[index].filters[0].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                    isValid = false;
                                }
                            }

                            if (typeof columns[index].filters[1].term !== 'undefined' && columns[index].filters[1].term !== null && columns[index].filters[1].term.trim() !== '') {
                                if (columns[index].filters[1].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                    isValid = false;
                                }
                            }
                        } else

                        if (! ((typeof columns[index].filters[0].term === 'undefined') && (typeof columns[index].filters[1].term === 'undefined'))) {
                            isValid = false;
                        }
                    }
                }

                sortOptions.push({
                    field: columns[index].field,
                    search: searchTerms,
                    type: columnType,
                    isValid: isValid
                });
            }
        });

        self.paginateParams.search = sortOptions;

        //set parameters to go back to the first page if page type is scroll
        if (self.pagingType === 'scroll') {
            self.scope.firstPage = 1;
            self.scope.currentPage = 1;
            self.paginateParams.start = 0;

            $timeout(function() {
                self.scope.gridApi.infiniteScroll.resetScroll(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
            });
        }

        //only make get page call if all filters are valid
        var isAllValid = true;
        _.each(sortOptions, function(sortOption) {
            if (sortOption.isValid === false) {
                isAllValid = false;
                return false;
            }
        });

        if (isAllValid === true) {
            self.getPage();
        }
    };


    this.rowSelectionChanged = function(row){ //jshint ignore: line
        var parentEntities = [];

        //get the parent entities if type is dataset
        if (self.entityType === 'dataset') {
            if (typeof row.entity.investigation !== 'undefined' && ! _.isEmpty(row.entity.investigation)) {
                parentEntities.push({
                    entityId: row.entity.investigation.id,
                    entityType: 'investigation'
                });
            } else {
                //should not reach this point as query should be covered by the routes
                throw new Error('Dataset entity must include investigation');
            }
        }

        if (row.isSelected === true) {
            if(typeof row.fromEvent !== 'undefined' && row.fromEvent === 'preSelection') {
                Cart.restoreItem(row.entity.facilityName, self.entityType, row.entity.id, row.entity.name, parentEntities);
            } else {
                Cart.addItem(row.entity.facilityName, self.entityType, row.entity.id, row.entity.name, parentEntities);
            }


        } else {
            Cart.removeItem(row.entity.facilityName, self.entityType, row.entity.id);
        }


    };

    this.rowSelectionChangedBatch = function(rows){ //jshint ignore: line
        _.each(rows, function(row) {
            self.rowSelectionChanged(row);
        });
    };

    this.getNextRouteUrl = function (row) {
        return $state.href('home.browse.facility.' + row.entity.nextRouteSegment, row.entity.nextRouteStateParam);
    };
}


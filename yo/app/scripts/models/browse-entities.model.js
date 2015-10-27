'use strict';

angular
    .module('angularApp')
    .service('BrowseEntitiesModel', BrowseEntitiesModel);

BrowseEntitiesModel.$inject = ['$rootScope', '$translate', '$q', 'APP_CONFIG', 'Config', 'RouteService', 'uiGridConstants', 'DataManager', '$timeout', '$state', 'Cart', 'IdsManager', 'usSpinnerService', 'inform'];

//TODO infinite scroll not working as it should when results are filtered. This is because the last page is determined by total items
//rather than the filtered total. We need to make another query to get the filtered total in order to make it work
//
//TODO sorting need fixing, ui-grid sorting is additive only rather than sorting by a single column. Queries are
//unable to do this at the moment. Do we want single column sort or multiple column sort. ui-grid currently does not
//support single column soting but users have submitted is as a feature request
function BrowseEntitiesModel($rootScope,  $translate, $q, APP_CONFIG, Config, RouteService, uiGridConstants, DataManager, $timeout, $state, Cart, IdsManager, usSpinnerService, inform){
    var self = this;

    
    this.init = function(facility, scope, currentEntityType, currentRouteSegment, sessions, $stateParams) {
            self.facility = facility;
            self.scope = scope;
            self.currentEntityType = currentEntityType;
            self.currentRouteSegment = currentRouteSegment;
            self.sessions = sessions;
            self.stateParams = $stateParams;

            self.options = configToUIGridOptions(facility, currentEntityType);
            self.structure = Config.getHierarchyByFacilityName(APP_CONFIG, facility.facilityName);
            self.nextRouteSegment = RouteService.getNextRouteSegmentName(self.structure, currentEntityType);
            self.pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
            self.pageSize = Config.getSitePageSize(APP_CONFIG, self.pagingType); //the number of rows for grid
            self.scrollRowFromEnd = Config.getSiteScrollRowFromEnd(APP_CONFIG, self.pagingType);
            self.paginationPageSizes = Config.getPaginationPageSizes(APP_CONFIG, self.pagingType); //the number of rows for grid
            self.gridOptions = scope.gridOptions;
            self.hasSizeField = hasField(self.options, 'size');
            self.pagePromiseCanceler = null;

            self.setGridOptions(self.scope.gridOptions);

            //get the default sort columnDef
            var defaultSortColumn = getDefaultSort(self.gridOptions.columnDefs);

            //set default column if no default column set
            if (defaultSortColumn.length === 0) {
                defaultSortColumn.push({
                    field : 'name',
                    sort : {
                        direction : 'asc'
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

            makeGridNoUnselect(self.facility, self.currentEntityType, self.structure, self.stateParams, self.gridOptions);

            var columnDefs = scope.gridOptions.columnDefs;
            restoreState().then(function(){
                saveState();
                applyFilters(columnDefs);
                applySorters(extractSortColumns(columnDefs));
                self.getPage();
            });
            
    };


    this.setGridOptions = function(gridOptions) {
        self.gridOptions = _.extend(gridOptions, {
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            columnDefs: self.options.columnDefs,
            enableFiltering: self.options.enableFiltering,
            useExternalSorting: true,
            useExternalFiltering: true,
            enableRowSelection: self.enableSelection(),
            enableRowHeaderSelection: self.enableSelection(),
            enableSelectAll: false,
            multiSelect: true,
            rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>',
            saveWidths: false,
            saveOrder: false,
            saveScroll: false,
            saveFocus: false,
            saveVisible: false,
            saveSort: true,
            saveFilter: true,
            savePinning: false,
            saveGrouping: false,
            saveGroupingExpandedStates: false,
            saveTreeView: false,
            saveSelection: false

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
    };

    /**
     * Loads data for both pagination and infinte scroll. This method is called by ui-grid to load the first page of data
     * for infinite scroll and to load next page data for paginated pages
     * @return {[type]} [description]
     */
    this.getPage = function() {
        if(self.pagePromiseCanceler){
            self.pagePromiseCanceler.resolve();
        }

        self.pagePromiseCanceler = $q.defer();

        var params = _.merge(self.paginateParams, {
            canceler: self.pagePromiseCanceler.promise
        });

        DataManager.getData(self.currentRouteSegment, self.facility.facilityName, self.sessions, self.stateParams, params).then(function(data){
            self.pagePromiseCanceler = null;
            if(!data.data){
                return;
            }

            self.gridOptions.data = data.data;
            self.gridOptions.totalItems = data.totalItems;

            if (data.totalItems === 0) {
                self.scope.isEmpty = true;
            } else {
                self.scope.isEmpty = false;
            }

            if (self.pagingType === 'scroll') {
                self.scope.lastPage = Math.ceil(data.totalItems / self.pageSize);
                self.scope.gridApi.infiniteScroll.dataLoaded(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
            }

            $timeout(function() {
                var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                //pre-select items in cart here
                _.each(rows, function(row) {
                    //fill size data
                    if (self.hasSizeField) {
                        if (self.currentEntityType === 'investigation' || self.currentEntityType === 'dataset') {
                            if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                var params = {};
                                params[self.currentEntityType  + 'Ids'] = row.entity.id;

                                usSpinnerService.spin('spinner-size-' + row.uid);

                                IdsManager.getSize(self.sessions, self.facility, params).then(function(data){
                                    row.entity.size = parseInt(data);
                                    usSpinnerService.stop('spinner-size-' + row.uid);
                                }, function(error) {
                                    row.entity.size = -1;
                                    usSpinnerService.stop('spinner-size-' + row.uid);

                                    inform.add(error, {
                                        'ttl': 4000,
                                        'type': 'danger'
                                    });
                                });
                            }
                        }
                    }

                    //select the row if item is in the cart
                    if (Cart.hasItem(self.facility.facilityName, self.currentEntityType, row.entity.id)) {
                        //the below selectRow api call fires the rowSelectionChanged callback which in turn calls
                        //Cart.addItem(). The Cart.addItem() broadcast a Cart::Change which fires a Cart.save().
                        //Since we only need to mark the row as selected, we do not want to preform a save to the
                        //backend. To get around this, we add a fromEvent property to the row which can be picked up
                        //by rowSelectionChanged and act accordingly.
                        row.fromEvent = 'preSelection';
                        self.scope.gridApi.selection.selectRow(row.entity);
                    }
                });

            }, 0);
        }, function(error){
            //TODO
            inform.add(error, {
                'ttl': 4000,
                'type': 'danger',

            });
        });
    };


    this.appendPage = function() {
        DataManager.getData(self.currentRouteSegment, self.facility.facilityName, self.sessions, self.stateParams, self.paginateParams).then(function(data){
            self.gridOptions.data = self.gridOptions.data.concat(data.data);
            self.gridOptions.totalItems = data.totalItems;

            $timeout(function() {
                var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                //pre-select items in cart here
                _.each(rows, function(row) {
                    //file size data
                    if (self.hasSizeField) {
                        if (self.currentEntityType === 'investigation' || self.currentEntityType === 'dataset') {
                            if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                                var params = {};
                                params[self.currentEntityType  + 'Ids'] = row.entity.id;

                                IdsManager.getSize(self.sessions, self.facility, params).then(function(data){
                                    row.entity.size = parseInt(data);
                                }, function() {
                                    row.entity.size = -1;
                                });
                            }
                        }
                    }

                    if (Cart.hasItem(self.facility.facilityName, self.currentEntityType, row.entity.id)) {
                       row.fromEvent = 'preSelection';
                       self.scope.gridApi.selection.selectRow(row.entity);
                    }
                });

            }, 0);


        }, function(){

        });
    };

    /**
     * Loads data for infinite scroll. This method is call by ui-grid when user scrolls down
     * @return {[type]} [description]
     */
    this.prependPage = function() {
        DataManager.getData(self.urrentRouteSegment, self.facility.facilityName, self.sessions, self.stateParams, self.paginateParams).then(function(data){
            self.gridOptions.data = data.data.concat(self.gridOptions.data);
            self.gridOptions.totalItems = data.totalItems;

            $timeout(function() {
                var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

                //pre-select items in cart here
                _.each(rows, function(row) {
                    //file size data
                    if (self.currentEntityType === 'investigation' || self.currentEntityType === 'dataset') {
                        if (typeof row.entity.size === 'undefined' || row.entity.size === null) {
                            var params = {};
                            params[self.currentEntityType  + 'Ids'] = row.entity.id;

                            IdsManager.getSize(self.sessions, self.facility, params).then(function(data){
                                row.entity.size = parseInt(data);
                            }, function() {
                                row.entity.size = -1;
                            });
                        }
                    }

                    if (Cart.hasItem(self.facility.facilityName, self.currentEntityType, row.entity.id)) {
                       row.fromEvent = 'preSelection';
                       self.scope.gridApi.selection.selectRow(row.entity);
                    }
                });
            }, 0);
        }, function(){

        });
    };

    this.refreshSelection = function() {
        $timeout(function() {
            var rows = self.scope.gridApi.core.getVisibleRows(self.scope.gridApi.grid);

            //pre-select items in cart here
            _.each(rows, function(row) {
                row.fromEvent = 'preSelection';

                if (Cart.hasItem(self.facility.facilityName, self.currentEntityType, row.entity.id)) {
                   self.scope.gridApi.selection.selectRow(row.entity);
                } else {
                    self.scope.gridApi.selection.unSelectRow(row.entity);
                }
            });

        }, 0);
    };


    this.enableSelection = function() {
        if (angular.isDefined(self.options.enableSelection) && self.options.enableSelection === true) {
            return true;
        } else {
            return false;
        }
    };


    this.getNextRouteUrl = function (row) {
        var params = {
            facilityName : self.facility.facilityName,
            //id : row.entity.id
        };

        params[self.currentEntityType + 'Id'] = row.entity.id;

        _.each(self.stateParams, function(value, key){
            params[key] = value;
        });

        var route = $state.href('home.browse.facility.' + self.nextRouteSegment, params);

        return route;
    };

    //pagination callback
    this.paginationChanged = function(newPage, pageSize) {
        self.paginateParams.pageNumber = newPage;
        self.paginateParams.pageSize = pageSize;

        self.paginateParams.start = (self.paginateParams.pageNumber - 1) * self.paginateParams.pageSize;
        self.paginateParams.numRows = self.paginateParams.pageSize;
        saveState();
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

    //sort callback
    this.sortChanged = function(grid, sortColumns) {
        applySorters(sortColumns);
        saveState();
        this.getPage();
    };

    this.filterChanged = function (columns) {
        applyFilters(columns);
        saveState();
        if(isAllFiltersValid(columns)){
            this.getPage();
        }
    };


    this.rowSelectionChanged = function(row){
        var parentEntities = [];

        //add item parentEntities
        if (self.currentEntityType === 'dataset' || self.currentEntityType === 'datafile') {
            if (_.has(self.stateParams, 'investigationId')) {
                parentEntities.push({
                    entityId: parseInt(self.stateParams.investigationId),
                    entityType: 'investigation'
                });
            }

            if (_.has(self.stateParams, 'datasetId')) {
                parentEntities.push({
                    entityId: parseInt(self.stateParams.datasetId),
                    entityType: 'dataset'
                });
            }
        }

        if (row.isSelected === true) {
            if(typeof row.fromEvent !== 'undefined' && row.fromEvent === 'preSelection') {
                Cart.restoreItem(self.facility.facilityName, self.currentEntityType, row.entity.id, row.entity.name, parentEntities);
            } else {
                Cart.addItem(self.facility.facilityName, self.currentEntityType, row.entity.id, row.entity.name, parentEntities);
            }
        } else {
            Cart.removeItem(self.facility.facilityName, self.currentEntityType, row.entity.id);
        }
    };

    this.rowSelectionChangedBatch = function(rows){
        _.each(rows, function(row) {
            self.rowSelectionChanged(row);
        });
    };



    // PRIVATE

    /**
     * Get the parent entities of the current entity type
     * @param  {[type]} facility          [description]
     * @param  {[type]} currentEntityType [description]
     * @param  {[type]} hierarchy         [description]
     * @return {[type]}                   [description]
     */
    function getSelectableParentEntities(facility, currentEntityType, hierarchy) {
        var h = hierarchy.slice(0);
        var index = h.indexOf(currentEntityType);

        //current entity not in hierarchy!! should never happen but just in case
        if (index === -1) {
            return false;
        }

        var previousEntities = h.splice(0, index);

        //only interested in investigation or dataset
        var selectableEntities = [];

        _.each(previousEntities, function(entityType) {
            if (entityType === 'investigation' || entityType === 'dataset') {
                selectableEntities.push(entityType);
            }
        });

        //return false as there are no selectable entities as no point carry on
        if (selectableEntities.length === 0) {
            return [];
        }

        var gridOptions = Config.getBrowseGridOptionsByFacilityName(APP_CONFIG, facility.facilityName);
        var parentEntities = [];

        //check column def to see if investigation or dataset is selectable
        _.each(selectableEntities, function(entityType) {
            if (gridOptions[entityType].enableSelection === true) {
                parentEntities.push(entityType);
            }
        });

        return parentEntities;
    }

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
     * Converts the site config file to settings used by ui-grid
     * @param  {[type]} facility          [description]
     * @param  {[type]} currentEntityType [description]
     * @return {[type]}                   [description]
     */
    function configToUIGridOptions(facility, currentEntityType) {
        var entityGridOptions = Config.getEntityBrowseGridOptionsByFacilityName(APP_CONFIG, facility.facilityName, currentEntityType);

        var downloadColumn = {
            name : 'actions',
            visible: false,
            translateDisplayName: 'BROWSE.COLUMN.ACTIONS.NAME',
            enableFiltering: false,
            enable: false,
            enableColumnMenu: false,
            enableSorting: false,
            enableHiding: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><download-datafile></download-datafile></div>'
        };
        entityGridOptions.columnDefs.push(downloadColumn);

        //do the work of transposing
        _.mapValues(entityGridOptions.columnDefs, transposeColumnDef);

        //add a Download column if enableDownload
        if(entityGridOptions.enableDownload){
            IdsManager.isTwoLevel(facility).then(function(isTwoLevel){
                if(!isTwoLevel){
                    downloadColumn.visible = true;
                }
            });
        }

        function transposeColumnDef(value){
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

            if (angular.isDefined(value.type) && value.type === 'date') {
                //value.filterHeaderTemplate = '<div class="ui-grid-filter-container" ng-repeat="colFilter in col.filters" filter-datepicker ng-model="colFilter.term" /></div>';
                //value.filterHeaderTemplate = "<div class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\" ng-model=\"colFilter.term\" ng-click=\"grid.appScope.open($event)\" datepicker-popup is-open=\"grid.appScope.status.opened\" datepicker-options=\"grid.appScope.dateOptions\" close-text=\"Close\" datepicker-append-to-body=\"true\" ng-class=\"{'ui-grid-filter-cancel-button-hidden' : colFilter.disableCancelFilterButton === true }\"><div ng-if=\"colFilter.type !== 'select'\"><input type=\"text\" class=\"ui-grid-filter-input ui-grid-filter-input-{{$index}}\" ng-model=\"colFilter.term\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\" aria-label=\"{{colFilter.ariaLabel || aria.defaultFilterLabel}}\"><div role=\"button\" class=\"ui-grid-filter-button\" ng-click=\"removeFilter(colFilter, $index)\" ng-if=\"!colFilter.disableCancelFilterButton\" ng-disabled=\"colFilter.term === undefined || colFilter.term === null || colFilter.term === ''\" ng-show=\"colFilter.term !== undefined && colFilter.term !== null && colFilter.term !== ''\"><i class=\"ui-grid-icon-cancel\" ui-grid-one-bind-aria-label=\"aria.removeFilter\">&nbsp;</i></div></div><div ng-if=\"colFilter.type === 'select'\"><select class=\"ui-grid-filter-select ui-grid-filter-input-{{$index}}\" ng-model=\"colFilter.term\" ng-attr-placeholder=\"{{colFilter.placeholder || aria.defaultFilterLabel}}\" aria-label=\"{{colFilter.ariaLabel || ''}}\" ng-options=\"option.value as option.label for option in colFilter.selectOptions\"><option value=\"\"></option></select><div role=\"button\" class=\"ui-grid-filter-button-select\" ng-click=\"removeFilter(colFilter, $index)\" ng-if=\"!colFilter.disableCancelFilterButton\" ng-disabled=\"colFilter.term === undefined || colFilter.term === null || colFilter.term === ''\" ng-show=\"colFilter.term !== undefined && colFilter.term != null\"><i class=\"ui-grid-icon-cancel\" ui-grid-one-bind-aria-label=\"aria.removeFilter\">&nbsp;</i></div></div></div>" //jshint ignore: line
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
            //apply only to investigations and datasets
            if(currentEntityType === 'investigation' || currentEntityType === 'dataset') {
                if(angular.isDefined(value.field) && value.field === 'size') {
                    value.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}" spinner-key="spinner-size-{{row.uid}}" class="grid-cell-spinner"></span><span>{{ row.entity.size | bytes }}</span></div>';
                    value.enableSorting = false;
                    value.enableFiltering = false;
                }
            }
            return value;
        }

        return entityGridOptions;
    }

    /**
     * Disable the selection and unselection of grid items
     * @param  {[type]} facility          [description]
     * @param  {[type]} currentEntityType [description]
     * @param  {[type]} structure         [description]
     * @param  {[type]} $stateParams      [description]
     * @param  {[type]} gridOptions       [description]
     * @return {[type]}                   [description]
     */
    function makeGridNoUnselect(facility, currentEntityType, structure, $stateParams, gridOptions) {
        var selectableEntities = getSelectableParentEntities(facility, currentEntityType, structure);

        if (selectableEntities.length !== 0) {
            var isInCart = false;

            //deal with investigation parent
            _.each(selectableEntities, function(entityType) {
                var id = $stateParams[entityType + 'Id'];

                if(typeof id === 'string') {
                    id = parseInt(id);
                }

                var item = Cart.getItem(facility.facilityName, entityType, id);

                if (item !== false) {
                    isInCart = true;
                }
            });

            if (isInCart === true) {
                gridOptions.noUnselect = true;

                gridOptions.isRowSelectable = function(row) {
                    //preselect the row
                    row.isSelected = true;
                    return false;
                };
            }
        }
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

    function extractColumnFilters(column){
        var out = [];
        if(column.filter && column.filter.term && column.filter.term !== ''){
            out.push(column.filter);
        } else if(column.filters){
            _.each(column.filters, function(filter){
                if(filter.term && filter.term !== ''){
                    out.push(filter);
                }
            });
        }
        return out;
    }

    function extractColumnSearchOption(column){
        var searchTerms = [];
        var isValid = true;

        var columnType = 'string';
        if (column.colDef && column.colDef.type) {
            columnType = column.colDef.type;
        }

        var filters = extractColumnFilters(column);

        if (filters.length > 0){
            if (columnType === 'string') {
                searchTerms.push(filters[0].term);
            }

            if (columnType === 'date') {
                //determine if 2 filters was configured
                var filterCount = filters.length;

                if (filterCount === 1) {
                    searchTerms.push(filters[0].term);

                    //validate term entered is a valid date before requesting page
                    _.each(filters, function(filter) {
                        if (typeof filter.term !== 'undefined' && filter.term !== null && filter.term.trim() !== '') {
                            if (filter.term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                isValid = false;
                            }
                        }
                    });
                } else if (filterCount > 1) {
                    //only allow 2 filters and ignore the rest if defined
                    searchTerms.push(filters[0].term);
                    searchTerms.push(filters[1].term);

                    //validate term entered is a valid date before requesting page
                    if ((typeof filters[0].term !== 'undefined') && (typeof filters[1].term !== 'undefined')) {
                        if (typeof filters[0].term !== 'undefined' && filters[0].term !== null && filters[0].term.trim() !== '') {
                            if (filters[0].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                isValid = false;
                            }
                        }

                        if (typeof filters[1].term !== 'undefined' && filters[1].term !== null && filters[1].term.trim() !== '') {
                            if (filters[1].term.match(/\d{4}\-\d{2}\-\d{2}/) === null ) {
                                isValid = false;
                            }
                        }
                    } else

                    if (! ((typeof filters[0].term === 'undefined') && (typeof filters[1].term === 'undefined'))) {
                        isValid = false;
                    }
                }
            }

            return {
                field: column.field,
                search: searchTerms,
                type: columnType,
                isValid: isValid
            };
        }
        
        return {};   
    }

    function searchOptions(columns){
        return _.select(_.map(columns, extractColumnSearchOption), function(option){ return option.field; });
    }

    function isAllFiltersValid(columns){
        var out = true;
        _.each(searchOptions(columns), function(searchOption) {
            if (searchOption.isValid === false) {
                out = false;
                return false;
            }
        });
        return out;
    }

    function applySorters(sortColumns){
        if (sortColumns.length !== 0) {
            sortColumns = [sortColumns[0]];
            self.paginateParams.sortField = sortColumns[0].field;
            self.paginateParams.order = sortColumns[0].sort.direction;
        }
        applyPaging();
    }

    function applyFilters(columns){
        self.paginateParams.search = searchOptions(columns);
        applyPaging();
    }

    function applyPaging(){
        //set parameters to go back to the first page if page type is scroll
        if (self.pagingType === 'scroll') {
            self.scope.firstPage = 1;
            self.scope.currentPage = 1;
            self.paginateParams.start = 0;

            $timeout(function() {
                self.scope.gridApi.infiniteScroll.resetScroll(self.scope.firstPage - 1 > 0, self.scope.currentPage + 1 < self.scope.lastPage);
            });
        }
    }

    /*
        #todo

        Both saveState and restoreState could do with some refactoring.

            * pageSize is referenced in multiple places - ideally this should be one.
            * pageNumber gets referenced in multiple places.
            * maybe we should get rid of the paginate params feature and just reference gridOptions?
            

    */
    function saveState(){
        var uiGridState = JSON.stringify(_.merge(self.scope.gridApi.saveState.save(), {
            pageSize: self.paginateParams.pageSize,
            pageNumber: self.paginateParams.pageNumber ? self.paginateParams.pageNumber : 1
        }));

        console.log('self.scope', self.scope);

        $state.go($state.current.name, {uiGridState: uiGridState}, {location: 'replace'});
    }

    function restoreState(){
        return $timeout(function() {
            var uiGridState = self.stateParams.uiGridState;
            if(uiGridState && uiGridState !== ''){
                uiGridState = JSON.parse(uiGridState);
                self.pageSize = uiGridState.pageSize;
                self.paginateParams.pageSize = uiGridState.pageSize;
                self.paginateParams.pageNumber = uiGridState.pageNumber;
                self.paginateParams.start = (self.paginateParams.pageNumber - 1) * self.paginateParams.pageSize;
                self.paginateParams.numRows = self.paginateParams.pageSize;
                self.gridOptions.paginationCurrentPage = self.paginateParams.pageNumber;
                self.gridOptions.paginationPageSize = self.paginateParams.pageSize;
                delete uiGridState.pageSize;
                delete uiGridState.pageNumber;
                self.scope.gridApi.saveState.restore(self.scope, uiGridState);
            }
        });
    }

    function extractSortColumns(columns){
        return _.select(columns, function(column){
            return column.sort && column.sort.direction;
        });
    }

}


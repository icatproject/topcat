
(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MyDataController', function($translate, $q, $scope, $rootScope, $timeout, $templateCache, $state, tc, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
        var entityType = tc.config().myDataGridOptions.entityType;
        var pagingConfig = tc.config().paging;
        var isScroll = pagingConfig.pagingType == 'scroll';
        var page = 1;
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var sortQuery = [];
        var filterQuery = [];
        var totalItems;
        var gridApi;
        var canceler = $q.defer();
        var stopListeningForCartChanges =  $rootScope.$on('cart:change', function(){
            updateSelections();
        });
        $scope.$on('$destroy', function(){
            canceler.resolve();
            stopListeningForCartChanges();
        });

        this.isScroll = isScroll;
        var gridOptions = _.merge({
            data: [],
            appScopeProvider: this,
            pageSize: !this.isScroll ? pagingConfig.paginationNumberOfRows : null,
            paginationPageSizes: pagingConfig.paginationPageSizes
        }, tc.config().myDataGridOptions[entityType]);
        gridOptions.useExternalPagination = true;
        gridOptions.useExternalSorting = true;
        gridOptions.useExternalFiltering = true;
        var enableSelection = gridOptions.enableSelection === true && entityInstanceName.match(/^investigation|dataset|datafile$/) !== null;
        gridOptions.enableSelectAll = false;
        gridOptions.enableRowSelection = enableSelection;
        gridOptions.enableRowHeaderSelection = enableSelection;

        var sortColumns = [];
        _.each(gridOptions.columnDefs, function(columnDef){
            
            if(columnDef.link) {
                if(typeof columnDef.link == "string"){
                    columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.browse(row.entity.' + columnDef.link + ')">{{row.entity.' + columnDef.field + '}}</a></div>';
                } else {
                    columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.browse(row.entity)">{{row.entity.' + columnDef.field + '}}</a></div>';
                }
            }


            if(columnDef.type == 'date'){
                if(columnDef.field && columnDef.field.match(/Date$/)){
                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[1].term" placeholder="To..."></div>';
                } else {
                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[1].term" placeholder="To..."></div>';
                }
            }

            if(columnDef.excludeFuture){
                var date = new Date();
                var day = date.getDate();
                var month = "" + (date.getMonth() + 1);
                if(month.length == 1) month = '0' + month;
                var year = date.getFullYear();
                var filter = year + '-' + month + '-' + day;
                $timeout(function(){
                    columnDef.filters[1].term = filter;
                });
            }

            if(columnDef.field == 'size'){
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
                columnDef.enableSorting = false;
                columnDef.enableFiltering = false;
            }

            if(columnDef.translateDisplayName){
                columnDef.displayName = columnDef.translateDisplayName;
                columnDef.headerCellFilter = 'translate';
            }

            if(columnDef.field == 'instrumentNames'){
                columnDef.cellTemplate = '<div class="ui-grid-cell-contents" ng-if="row.entity.investigationInstruments.length > 1"><span class="glyphicon glyphicon-th-list" tooltip="{{row.entity.instrumentNames}}" tooltip-placement="top" tooltip-append-to-body="true"></span> {{row.entity.firstInstrumentName}}</div><div class="ui-grid-cell-contents" ng-if="row.entity.investigationInstruments.length <= 1">{{row.entity.firstInstrumentName}}</div>';
            }

            if(columnDef.sort){
                if(columnDef.sort.direction.toLowerCase() == 'desc'){
                    columnDef.sort.direction = uiGridConstants.DESC;
                } else {
                    columnDef.sort.direction = uiGridConstants.ASC;
                }
            }

            columnDef.jpqlExpression = columnDef.jpqlExpression || entityType + '.' + columnDef.field;
            if(columnDef.sort) sortColumns.push(columnDef);
        });
        this.gridOptions = gridOptions;

        $templateCache.put('ui-grid/selectionRowHeaderButtons', '<div class="ui-grid-selection-row-header-buttons ui-grid-icon-ok" ng-class="{\'ui-grid-row-selected\': row.isSelected}" ng-click="selectButtonClick(row, $event)" tooltip="{{&quot;BROWSE.SELECTOR.ADD_REMOVE_TOOLTIP.TEXT&quot; | translate}}" tooltip-placement="right" tooltip-append-to-body="true">&nbsp;</div>');

        if(sortColumns.length > 0){
            sortQuery.push('order by ' + _.map(sortColumns, function(sortColumn){
                return sortColumn.jpqlExpression + ' ' + sortColumn.sort.direction;
            }).join(', '));
        }

        var includes = gridOptions.includes;
        this.facilities = tc.userFacilities();

        if($state.params.facilityName == ''){
          $state.go('home.my-data', {facilityName: this.facilities[0].config().facilityName});
          return;
        }

        var facility = tc.facility($state.params.facilityName);
        var icat = facility.icat();

       
        gridOptions.rowTemplate = '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>',
        this.showTabs = function(row) {
            $rootScope.$broadcast('rowclick', {
                'type': row.entity.entityType.toLowerCase(),
                'id' : row.entity.id,
                facilityName: facility.config().facilityName
            });
        };
        
        this.browse = function(row) {
            row.browse(canceler);
        };
        

        function generateQuery(){
            var out = [];

            if(entityType == "investigation"){
                out.push([
                    "SELECT investigation", 
                    "FROM Investigation investigation"
                ]);
            } else if(entityType == "dataset"){
                return out.push([
                    "SELECT dataset",
                    "from Dataset dataset, dataset.investigation investigation"
                ]);
            } else {
                throw "Entity type '" + entityType + "' is not supported";
            }

            out.push([
                ", investigation.facility facility, investigation.investigationUsers investigationUser, investigation.investigationInstruments investigationInstrument, investigationInstrument.instrument instrument",
                "WHERE facility.id = ?", facility.config().facilityId,
                "AND investigationUser.user.name = :user",
                filterQuery,
                sortQuery,
                includes && includes.length > 0 ? "INCLUDE " + includes.join(', ') : "",
                "LIMIT ?, ?", (page - 1) * pageSize, pageSize
            ]);
            return out;

        }

        function updateFilterQuery(){
            filterQuery = [];
            _.each(that.gridOptions.columnDefs, function(columnDef){
                if(columnDef.type == 'date' && columnDef.filters){
                    var from = columnDef.filters[0].term || '';
                    var to = columnDef.filters[1].term || '';
                    if(from != '' || to != ''){
                        from = completePartialFromDate(from);
                        to = completePartialToDate(to);
                        filterQuery.push([
                            "and ? between {ts ?} and {ts ?}",
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
        }

        function completePartialFromDate(date){
            var segments = date.split(/[-:\s]+/);
            var year = segments[0];
            var month = segments[1] || "01";
            var day = segments[2] || "01";
            var hours = segments[3] || "00";
            var minutes = segments[4] || "00";
            var seconds = segments[5] || "00";

            year = year + '0000'.slice(year.length, 4);
            month = month + '00'.slice(month.length, 2);
            day = day + '00'.slice(day.length, 2);
            hours = hours + '00'.slice(hours.length, 2);
            minutes = minutes + '00'.slice(minutes.length, 2);
            seconds = seconds + '00'.slice(seconds.length, 2);

            if(parseInt(month) == 0) month = '01';
            if(parseInt(day) == 0) day = '01';

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        }

        function completePartialToDate(date){
            var segments = date.split(/[-:\s]+/);
            var year = segments[0] || "";
            var month = segments[1] || "";
            var day = segments[2] || "";
            var hours = segments[3] || "23";
            var minutes = segments[4] || "59";
            var seconds = segments[5] || "59";
            year = year + '9999'.slice(year.length, 4);
            month = month + '99'.slice(month.length, 2);
            day = day + '99'.slice(day.length, 2);
            hours = hours + '33'.slice(hours.length, 2);
            minutes = minutes + '99'.slice(minutes.length, 2);
            seconds = seconds + '99'.slice(seconds.length, 2);

            if(parseInt(month) > 12) month = '12';
            var daysInMonth = new Date(year, day, 0).getDate();
            if(parseInt(day) > daysInMonth) day = daysInMonth;

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
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

        function updateSelections(){
            var timeout = $timeout(function(){
              _.each(gridOptions.data, function(row){
                  facility.user().cart(canceler.promise).then(function(cart){
                    if(gridApi){
                      if (cart.isCartItem(entityType, row.id)) {
                          gridApi.selection.selectRow(row);
                      } else {
                          gridApi.selection.unSelectRow(row);
                      }
                    }
                  });
              });
            });
            canceler.promise.then(function(){ $timeout.cancel(timeout); });
        }

        function getPage(){
            var out = icat.query(canceler.promise, generateQuery());
            out.then(function(results){
                _.each(results, function(result){ result.getSize(canceler.promise); });
            });
            return out;
        }

        gridOptions.onRegisterApi = function(_gridApi) {
            gridApi = _gridApi;

            getPage().then(function(results){
                gridOptions.data = results;
                updateTotalItems();
                updateSelections();
                updateScroll(results.length);
            });

            //sort change callback
            gridApi.core.on.sortChanged($scope, function(grid, sortColumns){
                sortQuery = [];
                if(sortColumns.length > 0){
                    sortQuery.push('order by ' + _.map(sortColumns, function(sortColumn){
                        return sortColumn.colDef.jpqlExpression + ' ' + sortColumn.sort.direction;
                    }).join(', '));
                }
                page = 1;
                getPage().then(function(results){
                    updateScroll(results.length);
                    gridOptions.data = results;
                    updateSelections();
                });
            });

            //filter change calkback
            gridApi.core.on.filterChanged($scope, function() {
                canceler.resolve();
                canceler = $q.defer();
                updateFilterQuery();
                page = 1;
                gridOptions.data = [];
                getPage().then(function(results){
                    gridOptions.data = results;
                    updateSelections();
                    updateScroll(results.length);
                    updateTotalItems();
                });
            });


            function updateRowSelection(row){
                var identity = _.pick(row.entity, ['facilityName', 'id']);
                if(_.find(gridApi.selection.getSelectedRows(), identity)){
                    row.entity.addToCart(canceler.promise);
                } else {
                    row.entity.deleteFromCart(canceler.promise);
                }
            }

            gridApi.selection.on.rowSelectionChanged($scope, function(row) {
                updateRowSelection(row);
            });

            gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows){
                _.each(rows, function(row){
                    updateRowSelection(row);
                });
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



(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MyDataController', function($translate, $q, $scope, $rootScope, $timeout, $templateCache, $state, tc, helpers, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
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


        this.facilities = tc.userFacilities();
        if($state.params.facilityName == ''){
          $state.go('home.my-data', {facilityName: this.facilities[0].config().facilityName});
          return;
        }


        var facility = tc.facility($state.params.facilityName);
        var icat = facility.icat();
        var entityType = facility.config().myData.entityType;

        this.isScroll = isScroll;
        var gridOptions = _.merge({
            data: [],
            appScopeProvider: this,
            pageSize: !this.isScroll ? pagingConfig.paginationNumberOfRows : null,
            paginationPageSizes: pagingConfig.paginationPageSizes
        }, facility.config().myData.gridOptions);
        helpers.setupGridOptions(gridOptions, entityType);
        this.gridOptions = gridOptions;
        var includes = gridOptions.includes;

        var sortColumns = [];
        _.each(gridOptions.columnDefs, function(columnDef){
            if(columnDef.sort){
                sortColumns.push({
                    colDef: columnDef,
                    sort: columnDef.sort
                });
            }
        });

        $templateCache.put('ui-grid/selectionRowHeaderButtons', '<div class="ui-grid-selection-row-header-buttons ui-grid-icon-ok" ng-class="{\'ui-grid-row-selected\': row.isSelected}" ng-click="selectButtonClick(row, $event)" uib-tooltip="{{&quot;BROWSE.SELECTOR.ADD_REMOVE_TOOLTIP.TEXT&quot; | translate}}" tooltip-placement="right" tooltip-append-to-body="true">&nbsp;</div>');

        if(sortColumns.length > 0){
            sortQuery.push('order by ' + _.map(sortColumns, function(sortColumn){
                return sortColumn.jpqlExpression + ' ' + sortColumn.sort.direction;
            }).join(', '));
        }

        
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

        function generateQueryBuilder(){
            var out = icat.queryBuilder(entityType);

            out.where("investigationUserUser.name = :user");

            _.each(gridOptions.columnDefs, function(columnDef){
                if(!columnDef.field) return;

                if(columnDef.type == 'date' && columnDef.filters){
                    var from = columnDef.filters[0].term || '';
                    var to = columnDef.filters[1].term || '';
                    if(from != '' || to != ''){
                        from = helpers.completePartialFromDate(from);
                        to = helpers.completePartialToDate(to);
                        out.where([
                            "? between {ts ?} and {ts ?}",
                            columnDef.jpqlExpression.safe(),
                            from.safe(),
                            to.safe()
                        ]);
                    }
                } if(columnDef.type == 'number' && columnDef.filters){
                    var from = columnDef.filters[0].term || '';
                    var to = columnDef.filters[1].term || '';
                    if(from != '' || to != ''){
                        from = parseInt(from || '0');
                        to = parseInt(to || '1000000000000');
                        out.where([
                            "? between ? and ?",
                            columnDef.jpqlExpression.safe(),
                            from,
                            to
                        ]);
                        if(columnDef.where) out.where(columnDef.where);
                    }
                } else if(columnDef.type == 'string' && columnDef.filter && columnDef.filter.term) {
                    out.where([
                        "UPPER(?) like concat('%', ?, '%')", 
                        columnDef.jpqlExpression.safe(),
                        columnDef.filter.term.toUpperCase()
                    ]);
                }

                if(columnDef.field.match(/\./) && !(columnDef.type == 'number' && columnDef.filters)){
                    var entityType =  columnDef.field.replace(/\[([^\.=>\[\]\s]+)/, function(match){ 
                        return helpers.capitalize(match.replace(/^\[/, ''));
                    }).replace(/^([^\.\[]+).*$/, '$1');
                    out.include(entityType);
                }
                
            });
        
            
            _.each(sortColumns, function(sortColumn){
                if(sortColumn.colDef){
                    out.orderBy(sortColumn.colDef.jpqlExpression, sortColumn.sort.direction);
                }
            });

            out.limit((page - 1) * pageSize, pageSize);

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
            that.totalItems = undefined;
            return generateQueryBuilder().count(canceler.promise).then(function(_totalItems){
                gridOptions.totalItems = _totalItems;
                totalItems = _totalItems;
                that.totalItems = _totalItems;
            });
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
            return generateQueryBuilder().run(canceler.promise).then(function(entities){
                _.each(entities, function(entity){
                    if(entity.getSize){
                        entity.getSize(canceler.promise);
                    }

                    _.each(gridOptions.columnDefs, function(columnDef){
                        if(columnDef.type == 'number' && columnDef.filters){
                            var pair = columnDef.jpqlExpression.split(/\./);
                            var entityType = pair[0];
                            var entityField = pair[1];

                            icat.queryBuilder(entityType).where([
                                "investigation.id = ?", entity.id,
                                "and datafileParameterType.name = 'run_number'"
                            ]).orderBy('datafileParameter.numericValue').limit(1).run(canceler.promise).then(function(results){
                                var fieldNameSuffix = helpers.capitalize(entityType) + entityField;
                                if(results.length > 0){
                                    entity['min' + fieldNameSuffix] = results[0].numericValue;
                                } else {
                                    entity['min' + fieldNameSuffix] = "";
                                }
                            });

                            icat.queryBuilder('datafileParameter').where([
                                "investigation.id = ?", entity.id,
                                "and datafileParameterType.name = 'run_number'"
                            ]).orderBy('datafileParameter.numericValue', 'desc').limit(1).run(canceler.promise).then(function(results){
                                var fieldNameSuffix = helpers.capitalize(entityType) + entityField;
                                if(results.length > 0){
                                    entity['max' + fieldNameSuffix]  = results[0].numericValue;
                                } else {
                                    entity['max' + fieldNameSuffix] = "";
                                }
                            });
                        }
                    });

                });
                return entities;
            });
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
            gridApi.core.on.sortChanged($scope, function(grid, _sortColumns){
                var sortColumns = _sortColumns;
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




(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('CartController', function($translate, $uibModalInstance, $uibModal, $q, $timeout, $scope, $rootScope, tc, uiGridConstants, helpers){
        var that = this;
        var pagingConfig = tc.config().paging;
        var timeout = $q.defer();
        var gridApi;
        $scope.$on('$destroy', function(){ timeout.resolve(); });
        var isScroll = pagingConfig.pagingType == 'scroll';
        this.isScroll = isScroll;
        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
        var gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().cart.gridOptions);
        var page = 1;
        var delay = $timeout(1000);
        var filter = function(){ return true; };
        var sorter = function(){ return true; };
        helpers.setupTopcatGridOptions(gridOptions, 'cartItem');
        gridOptions.columnDefs.push({
            name : 'actions',
            title: 'CART.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableSorting: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.remove(row.entity)" translate="CART.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('CART.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></div>'
        });

        this.gridOptions = gridOptions;
        this.totalSize = undefined;


        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        this.remove = function(cartItem){
            var data = [];
            _.each(that.gridOptions.data, function(currentCartItem){
                if(currentCartItem.id != cartItem.id) data.push(currentCartItem);
            });
            that.gridOptions.data = data;
            cartItem.delete().then(function(){
                if(that.gridOptions.data.length == 0){
                    $uibModalInstance.dismiss('cancel');
                } else {
                    cartItemsCache = null;
                    getTotalSize().then(function(totalSize){
                        that.totalSize = totalSize;
                    });
                }
            });
        };

        this.removeAll = function(){
            var cartPromises = [];
            _.each(tc.userFacilities(), function(facility){
                cartPromises.push(facility.user().deleteAllCartItems(timeout.promise));
            });
            $q.all(cartPromises).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

        this.download = function(){
            $uibModal.open({
                templateUrl : 'views/download-cart.html',
                controller: "DownloadCartController as downloadCartController",
                size : 'lg'
            })
        };

        function getCarts(){
            var defered = $q.defer();
            var out = [];
            var promises = [delay];
            _.each(tc.userFacilities(), function(facility){
                promises.push(facility.user().cart(timeout.promise).then(function(cart){
                    out.push(cart);
                }));
            });
            $q.all(promises).then(function(){
                defered.resolve(out);
            });
            return defered.promise;
        }

        var cartItemsCache = null;
        function getCartItems(){
            var defered = $q.defer();
            if(!cartItemsCache){
                getCarts().then(function(carts){
                    var out = [];
                    _.each(carts, function(cart){
                        out = _.flatten([out, cart.cartItems]);
                    });
                    cartItemsCache = out;
                    defered.resolve(out);
                });
            } else {
                defered.resolve(cartItemsCache)
            }
            return defered.promise;
        }

        var getTotalSizeTimeout;
        function getTotalSize(){
            if(getTotalSizeTimeout){
                getTotalSizeTimeout.resolve();
            }
            getTotalSizeTimeout = $q.defer();
            timeout.promise.then(function(){ getTotalSizeTimeout.resolve(); });
            var defered = $q.defer();
            getCarts().then(function(carts){
                var out = 0;
                var promises = [];
                _.each(carts, function(cart){
                    promises.push(cart.getSize(getTotalSizeTimeout.promise).then(function(size){
                        out = out + size
                    }));
                });
                $q.all(promises).then(function(){
                    defered.resolve(out);
                });
            });
            return defered.promise;
        }

        getTotalSize().then(function(totalSize){
            that.totalSize = totalSize;
        });

        function getPage(){
            var defered = $q.defer();
            $timeout(function(){
                getCartItems().then(function(cartItems){
                    var preparedCartItems = cartItems;
                    preparedCartItems = _.select(preparedCartItems, filter);
                    preparedCartItems.sort(sorter);

                    var pages = _.chunk(preparedCartItems, pageSize);
                    var out = pages[page - 1];
                    if(!out) out = [];
                    _.each(out, function(cartItem){
                        cartItem.getSize(timeout.promise);
                        cartItem.getStatus(timeout.promise);
                    });
                    defered.resolve(out);
                });
            });
            return defered.promise;
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

        gridOptions.onRegisterApi = function(_gridApi) {
            gridApi = _gridApi;

            getPage().then(function(results){
                gridOptions.data = results;
                updateScroll(results.length);
            });

            //sort change callback
            gridApi.core.on.sortChanged($scope, function(grid, sortColumns){
                timeout.resolve();
                timeout = $q.defer();
                var _timeout = $timeout(function(){
                    var sorters = [];
                    _.each(sortColumns, function(sortColumn){
                        if(sortColumn.colDef){
                            sorters.push(function(rowA, rowB){
                                var valueA = rowA[sortColumn.colDef.field];
                                var valueB = rowB[sortColumn.colDef.field];

                                var out = 0;
                                if(valueA < valueB){
                                    out = -1
                                } else if(valueA > valueB){
                                    out = 1
                                }

                                if(sortColumn.sort.direction == 'desc') out = out * -1;

                                return out;
                            });
                        }
                    });
                    sorter = function(rowA, rowB){
                        var out = 0;
                        _.each(sorters, function(sorter){
                            var current = sorter(rowA, rowB);
                            if(current != 0){
                                out = current;
                                return false;
                            }
                        });
                        return out;
                    };
                    page = 1;
                    getPage().then(function(page){
                        gridOptions.data = page;
                    });
                });
                timeout.promise.then(function(){ $timeout.cancel(_timeout); });
            });

            //filter change callback
            gridApi.core.on.filterChanged($scope, function(){
                timeout.resolve();
                timeout = $q.defer();
                var _timeout = $timeout(function(){
                    var conditions = [];
                    _.each(gridOptions.columnDefs, function(columnDef){
                        if(!columnDef.field) return;
                        if(columnDef.type == 'date' && columnDef.filters){

                        } else if(columnDef.type == 'string' && columnDef.filter){
                            conditions.push(function(row){
                                return columnDef.filter.term === undefined || columnDef.filter.term === null || row[columnDef.field].indexOf(columnDef.filter.term) >= 0;
                            });
                        }
                    });
                    filter = function(row){
                        var out = true;
                        _.each(conditions, function(condition){
                            if(!condition(row)){
                                out = false;
                                return false;
                            }
                        })
                        return out;
                    };
                    page = 1;
                    getPage().then(function(page){
                        gridOptions.data = page;
                    });
                });
                timeout.promise.then(function(){ $timeout.cancel(_timeout); });
            });


            if(isScroll){
                //scroll down more data callback (append data)
                gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                    page++;
                    getPage().then(function(results){
                        _.each(results, function(result){ gridOptions.data.push(result); });
                        if(results.length == 0) page--;
                        updateScroll(results.length);
                    });
                });

            } else {
                //pagination callback
                gridApi.pagination.on.paginationChanged($scope, function(_page, _pageSize) {
                    page = _page;
                    pageSize = pageSize;
                    getPage().then(function(results){
                        gridOptions.data = results;
                    });
                });
            }

        };

    });

})();

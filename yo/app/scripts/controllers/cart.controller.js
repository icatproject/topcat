

(function(){
    'use strict';

    var app = angular.module('topcat');

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

        $rootScope.$broadcast('cart:open');

        this.gridOptions = gridOptions;
        this.datafileCount = 0;
        this.totalSize = 0;
        this.isLoaded = false;
        this.isValid = false;
        this.enableLimits = tc.config().cart.enableLimits;
        this.maxDatafileCount = tc.config().cart.maxDatafileCount;
        this.maxTotalSize = tc.config().cart.maxTotalSize;

        var existingButtons = [
            {
                name: "remove-all",
                click: function(){
                    that.removeAll();
                },
                disabled: function(){ return false; },
                class: "btn btn-primary",
                translate: "CART.REMOVE_ALL_BUTTON.TEXT",
                translateTooltip: "CART.REMOVE_ALL_BUTTON.TOOLTIP.TEXT"
            },
            {
                name: "download-cart",
                click: function(){
                    that.download();
                },
                disabled: function(){
                    return that.enableLimits && !that.isValid;
                },
                class: "btn btn-primary",
                translate: "CART.DOWNLOAD_CART_BUTTON.TEXT",
                translateTooltip: "CART.DOWNLOAD_CART_BUTTON.TOOLTIP.TEXT"
            },
            {
                name: "cancel",
                click: function(){
                    that.cancel();
                },
                disabled: function(){ return false; },
                class: "btn btn-warning",
                translate: "CART.CANCEL_BUTTON.TEXT",
                translateTooltip: "CART.CANCEL_BUTTON.TOOLTIP.TEXT"
            }

        ];

        var otherButtons = _.map(tc.ui().cartButtons(), function(otherButton){
            return {
                name: otherButton.name,
                click: otherButton.click,
                disabled: otherButton.disabled || function(){ return false; },
                class: otherButton.options.class || "btn btn-primary",
                translate: "CART." + otherButton.name.toUpperCase().replace(/-/g, '_') + "_BUTTON.TEXT",
                translateValues: otherButton.options.translateValues,
                translateTooltip: "CART." + otherButton.name.toUpperCase().replace(/-/g, '_') + "_BUTTON.TOOLTIP.TEXT",
                insertBefore: otherButton.options.insertBefore,
                insertAfter: otherButton.options.insertAfter,
                show: otherButton.options.show
            };
        });

        this.buttons = helpers.mergeNamedObjectArrays(existingButtons, otherButtons);

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        this.remove = function(cartItem){
            resetGetTotalsTimeout();

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
                    getTotals();
                }
            });
        };

        this.removeAll = function(){
             resetGetTotalsTimeout();

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

        var getTotalsTimeout;
        function resetGetTotalsTimeout(){
            if(getTotalsTimeout){
                getTotalsTimeout.resolve();
            }
            getTotalsTimeout = $q.defer();
            timeout.promise.then(function(){ getTotalsTimeout.resolve(); });
        }

        function getDatafileCount(){
            var out = {};

            return getCarts().then(function(carts){
                var promises = [];

                var isTimedOut = false;
                getTotalsTimeout.promise.then(function(){
                    isTimedOut = true;
                });

                _.each(carts, function(cart){
                    if(isTimedOut) return false;

                    out[cart.id] = 0;

                    var promise = cart.getDatafileCount(getTotalsTimeout.promise);

                    promises.push(promise.then(function(){}, function(){}, function(fileCount){
                        out[cart.id] = fileCount;
                        that.datafileCount = _.sum(out);
                    }));
                });

                return $q.all(promises).then(function(){
                    return _.sum(out);
                });
            });
        };

        function getTotalSize(){
            var out = {};

            return getCarts().then(function(carts){
                var promises = [];

                var isTimedOut = false;
                getTotalsTimeout.promise.then(function(){
                    isTimedOut = true;
                });

                _.each(carts, function(cart){
                    if(isTimedOut) return false;

                    out[cart.id] = 0;

                    var promise = cart.getSize(getTotalsTimeout.promise);

                    promises.push(promise.then(function(){}, function(){}, function(size){
                        out[cart.id] = size;
                        that.totalSize = _.sum(out);
                    }));
                });

                return $q.all(promises).then(function(){
                    return _.sum(out);
                });
            });
        }

        function getTotals(){
            if(!that.enableLimits) return;

            resetGetTotalsTimeout();
            that.isLoaded = false;
            that.isValid = false;
            that.totalSize = 0;
            that.datafileCount = 0;

            return getDatafileCount().then(function(datafileCount){
                that.datafileCount = datafileCount;
                if(datafileCount <= that.maxDatafileCount){
                    return getTotalSize().then(function(totalSize){
                        that.totalSize = totalSize;
                        that.isLoaded = true;

                        if(totalSize <= that.maxTotalSize){
                            that.isValid = true;
                        } else {
                            return $q.reject("Total size too big");
                        }
                    });
                } else {
                    that.isLoaded = true;
                    return $q.reject("Too many files");
                }
            });

        }

        timeout.promise.then(function(){
            if(getTotalsTimeout) getTotalsTimeout.resolve();
        });

        getTotals();

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
                        cartItem.getDatafileCount(timeout.promise);
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
                    sorter = helpers.generateEntitySorter(sortColumns);
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
                    filter = helpers.generateEntityFilter(gridOptions);
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

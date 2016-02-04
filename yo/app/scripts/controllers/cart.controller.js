

(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('CartController', function($translate, $uibModalInstance, $uibModal, $q, $scope, tc, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });
        this.isScroll = pagingConfig.pagingType == 'scroll';
        this.gridOptions = _.merge({
            data: [],
            appScopeProvider: this,
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            enableRowSelection: false,
            enableRowHeaderSelection: false,
            gridMenuShowHideColumns: false,
            pageSize: !this.isScroll ? pagingConfig.paginationNumberOfRows : null,
            paginationPageSizes: pagingConfig.paginationPageSizes
        }, tc.config().cartGridOptions);
        _.each(this.gridOptions.columnDefs, function(columnDef){
            if (columnDef.filter.condition) {
                columnDef.filter.condition = uiGridConstants.filter[columnDef.filter.condition.toUpperCase()];
            }

            if(columnDef.field === 'size') {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
            }

            if(columnDef.field === 'availability') {
               columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.status === undefined" class="grid-cell-spinner"></span><span>{{row.entity.status}}</span></div>';
            }

        });
        this.gridOptions.columnDefs.push({
            name : 'actions',
            translateDisplayName: 'CART.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableSorting: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.remove(row.entity)" translate="CART.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('CART.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></div>'
        });
        this.totalSize = 0;

        _.each(tc.userFacilities(), function(facility){
            facility.user().cart(timeout.promise).then(function(cart){
                that.gridOptions.data = _.flatten([that.gridOptions.data, cart.cartItems]);
                _.each(cart.cartItems, function(cartItem){
                    cartItem.getSize(timeout.promise).then(function(size){
                        that.totalSize = that.totalSize + size;
                    });
                    cartItem.getStatus(timeout.promise);
                });
            });
        });

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
                }
            });
        };

        this.removeAll = function(){
            var promises = [];
            _.each(tc.userFacilities(), function(facility){
                promises.push(facility.user().deleteAllCartItems(timeout.promise));
            });
            $q.all(promises).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

        this.download = function(){
            $uibModal.open({
                templateUrl : 'views/download-cart.html',
                controller : function($uibModalInstance, $scope){
                    var that = this;
                    var timeout = $q.defer();
                    $scope.$on('$destroy', function(){ timeout.resolve(); });
                    this.hasArchive = false;
                    this.email = "";
                    this.downloads = [];

                    _.each(tc.userFacilities(), function(facility){
                        facility.user().cart(timeout).then(function(cart){
                            if(cart.cartItems.length > 0){
                                var transportTypes = [];
                                var transportType = "";

                                _.each(facility.config().downloadTransportType, function(current){
                                    transportTypes.push(current.type);
                                    if(current.default){
                                        transportType = current.type;
                                    }
                                });
                                
                                var date = new Date();
                                var year = date.getFullYear();
                                var month = date.getMonth() + 1;
                                var day = date.getDate();
                                if(day < 10) day = '0' + day;
                                var hour = date.getHours();
                                if(hour < 10) hour = '0' + hour;
                                var minute = date.getMinutes();
                                if(minute < 10) minute = '0' + minute;
                                var second = date.getSeconds();
                                if(second < 10) second = '0' + second;
                                var fileName = facility.config().facilityName + "_" + year + "-" + month + "-" + day + "_" + hour + "-" + minute + "-" + second;

                                var download = {
                                    fileName: fileName,
                                    facilityName: facility.config().facilityName,
                                    transportTypes: transportTypes,
                                    transportType: transportType
                                };

                                _.each(cart.cartItems, function(cartItem){
                                    cartItem.getSize(timeout).then(function(size){
                                        if(download.size === undefined) download.size = 0;
                                        download.size = download.size + size;
                                    });

                                    cartItem.getStatus(timeout).then(function(status){
                                        if(status == "ARCHIVED"){
                                            download.availability = "ARCHIVED";
                                            that.hasArchive = true;
                                        } else if(download.availability != "ARCHIVED") {
                                            download.availability = status;
                                        }
                                    });
                                });

                                that.downloads.push(download);
                            }
                        });
                    });

                    this.cancel = function() {
                        $uibModalInstance.dismiss('cancel');
                    };
                },
                controllerAs: "downloadCartController",
                size : 'lg'
            })
        };

    });

})();


/*
(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$rootScope', '$scope', 'APP_CONFIG', 'Config', 'Cart', 'CartModel'];

    function CartController($rootScope, $scope, APP_CONFIG, Config, Cart, CartModel) {
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        $scope.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        CartModel.init($scope);
        CartModel.setCart();

        //get reference to the items in the cart directly
        $scope.items = Cart._cart.items;

        $scope.$watchCollection(function() {
            $scope.totalSize = _.reduce(Cart.getItems(), function(total, item){ return total + item.size }, 0) || 0;
            return $scope.items;
        }, function(newCol) {
            if(newCol.length === 0) {
                $scope.isEmpty = true;
            } else {
                $scope.isEmpty = false;
            }
        });

        $scope.gridOptions.onRegisterApi = function(gridApi) {
            $scope.gridApi = gridApi;
        };

        //listen to when cart is displayed and refresh the cart data
        $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) { //jshint ignore: line
            if (toState.name === 'home.cart') {
                CartModel.refreshData();
            }
        });


        $scope.removeAllItems = function(row) {
            CartModel.removeAllItems(row);
        };

        $scope.removeItem = function(row) {
            CartModel.removeItem(row);
        };
    }
})();
*/

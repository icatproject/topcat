

(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('CartController', function($translate, $uibModalInstance, tc, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
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
        });
        this.gridOptions.columnDefs.push({
            name : 'actions',
            translateDisplayName: 'CART.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableSorting: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.removeItem(row)" translate="CART.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('CART.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></div>'
        });


        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
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

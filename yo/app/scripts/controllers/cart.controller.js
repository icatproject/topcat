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

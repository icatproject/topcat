(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$rootScope', '$scope', 'APP_CONFIG', 'Config', 'Cart', 'CartModel', '$sessionStorage', '$log'];

    function CartController($rootScope, $scope, APP_CONFIG, Config, Cart, CartModel, $sessionStorage, $log) { //jshint ignore: line
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        $scope.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        CartModel.init($scope);
        CartModel.setCart();

        $scope.items = Cart.getItems();

        $scope.$watchCollection(function() {
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

        $rootScope.$on('Cart:change', function(){
            Cart.save();
        });


        $rootScope.$on('Logout:success', function(){
            if (Cart.isRestorable()) {
                Cart.restore();
            }

        });

        $rootScope.$on('SESSION:EXPIRED', function(event, data){
            if (typeof data.facilityName !== 'undefined' && typeof data.userName !== 'undefined') {
                Cart.removeUserItems(data.facilityName, data.userName);
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

(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$rootScope', '$scope', 'APP_CONFIG', 'Config', 'Cart', 'CartModel', '$sessionStorage', '$log'];

    function CartController($rootScope, $scope, APP_CONFIG, Config, Cart, CartModel, $sessionStorage, $log) { //jshint ignore: line
        var ct = this;
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        ct.isScroll = (pagingType === 'scroll') ? true : false;

        CartModel.init($scope);
        ct.gridOptions = CartModel.gridOptions;

        if (ct.gridOptions.data.length === 0) {
            $scope.isEmpty = true;
        } else {
            $scope.isEmpty = false;
        }

        //listen to when cart is displayed and refresh the cart data
        $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) { //jshint ignore: line
            if (toState.name === 'home.cart') {
                CartModel.refreshData();
            }

            if (ct.gridOptions.data.length === 0) {
                $scope.isEmpty = true;
            } else {
                $scope.isEmpty = false;
            }
        });

        /*$rootScope.$on('Cart:change', function(){
            if (ct.gridOptions.data.length === 0) {
                $scope.isEmpty = true;
            } else {
                $scope.isEmpty = false;
            }
        });*/

        $scope.deleteRow = function(row) {
            CartModel.deleteRow(row);

            //display empty cart if no item in cart
            if (ct.gridOptions.data.length === 0) {
                $scope.isEmpty = true;
            } else {
                $scope.isEmpty = false;
            }

        };
    }
})();

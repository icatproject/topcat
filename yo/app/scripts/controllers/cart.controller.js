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

        ct.items = Cart.getItems();
        ct.isEmpty = false;

        $scope.$watchCollection(function() {
            return ct.items;
        }, function(newCol) {
            if(newCol.length === 0) {
                ct.isEmpty = true;
            } else {
                ct.isEmpty = false;
            }

        });

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


        ct.removeAllItems = function(row) {
            CartModel.removeAllItems(row);
        };

        $scope.removeItem = function(row) {
            CartModel.removeItem(row);
        };

        $scope.getSize = function(row) { //jshint ignore: line
            /*if (row.entity.size === null) {
                $log.debug('size is null');
                row.entity.size = _.random(0, 1000000);

                return row.entity.size;
            } else {
                $log.debug('size not null!!');
                return row.entity.size;
            }*/
        };
    }
})();

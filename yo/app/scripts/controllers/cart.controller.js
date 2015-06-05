(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$scope', 'Cart', '$sessionStorage', '$log'];

    function CartController($scope, Cart, $sessionStorage, $log) {
        var vm = this;
        var loggedInCartItems = Cart.getLoggedInItems($sessionStorage);

        $log.debug('cart', loggedInCartItems);

        vm.cart = loggedInCartItems;
    }
})();

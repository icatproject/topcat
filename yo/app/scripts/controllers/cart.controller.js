(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$scope', 'CartData'];

    function CartController($scope, CartData) {
        var vm = this;

        var random;

        if (!$scope.random) {
            $scope.random = Math.round(Math.random()*10000);
        }

        vm.random = random;
        vm.cart = CartData;
        console.log(CartData);
    }
})();

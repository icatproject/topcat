(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('CartController', CartController);

    CartController.$inject = ['$scope'];

    function CartController($scope) {
        var vm = this;

        var random;

        if (!$scope.random) {
            $scope.random = Math.round(Math.random()*10000);
        }

        vm.random = random;
        //vm.cart = CartData;
    }
})();

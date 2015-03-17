'use strict';

var CartCtrl = ['$scope', 'CartData', function($scope, CartData) {
    var vm = this;

    var random;

    if (!$scope.random) {
        $scope.random = Math.round(Math.random()*10000);
    }

    vm.random = random;

    vm.cart = CartData;

    console.log(CartData);

}];

angular.module('angularApp').controller('CartCtrl', CartCtrl);
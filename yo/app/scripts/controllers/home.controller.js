'use strict';

angular
    .module('angularApp')
    .controller('HomeController', HomeController);

HomeController.$inject = ['$scope'];

function HomeController($scope) {
    if (!$scope.random) {
        $scope.random = Math.round(Math.random()*10000);
    }
}


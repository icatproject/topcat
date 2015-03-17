'use strict';


var HomeCtrl = ['$scope', function($scope) {
    if (!$scope.random) {
        $scope.random = Math.round(Math.random()*10000);
    }
}];

angular.module('angularApp').controller('HomeCtrl', HomeCtrl);
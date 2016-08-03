(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('HomeController', function ($scope) {
        if (!$scope.random) {
            $scope.random = Math.round(Math.random()*10000);
        }
    });
    
})();

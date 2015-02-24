'use strict';

var HomeController = ['$rootScope', '$state', '$scope', function($rootScope, $state, $scope) {
    $scope.initialise = function() {

        $scope.go = function(state) {
            $state.go(state);
        };

        $scope.tabData = [
            {
                heading: 'Search',
                route: 'home.browse',
            },
            {
                heading: 'Cart',
                route: 'home.cart'
            }
        ];
    };

    $scope.initialise();
}];

angular.module('angularApp').controller('HomeController', HomeController);
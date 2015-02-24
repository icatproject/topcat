'use strict';

var BrowseController = ['$rootScope', '$state', '$scope', function($rootScope, $state, $scope) {
    $scope.initialise = function() {

        $scope.go = function(state) {
            //$state.go(state);
        };


        $scope.tabData = [
            {
                heading: 'Meta Info 1',
                route: 'home.browse.meta-1',
            },
            {
                heading: 'Meta Info 2',
                route: 'home.browse.meta-2'
            },
            {
                heading: 'Meta Info 3',
                route: 'home.browse.meta-3'
            }
        ];
    };

    $scope.initialise();
}];

angular.module('angularApp').controller('BrowseController', BrowseController);
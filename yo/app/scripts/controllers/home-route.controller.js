(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('HomeRouteController', HomeRouteController);

    HomeRouteController.$inject = ['$state', 'RouteUtils'];

    function HomeRouteController($state, RouteUtils) {
        $state.go(RouteUtils.getHomeRouteName());
    }
})();

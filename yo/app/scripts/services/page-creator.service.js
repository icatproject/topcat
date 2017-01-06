(function() {
    'use strict';

    angular.
        module('topcat').factory('PageCreatorService', PageCreatorService);

    PageCreatorService.$inject = ['RouteService', 'RuntimeStatesProvider', 'tc', 'helpers', '$translate'];

    function PageCreatorService(RouteService, RuntimeStatesProvider, tc, helpers, $translate) {
        var route = {};

        route.createStates = function() {
            _.each(tc.config().pages, function(page) {
                RuntimeStatesProvider.addState(page.name, {
                    url: "/" + page.name,
                    templateUrl: "/pages/" + page.name + ".html"
                });
            });
        };

        return route;
    }
})();
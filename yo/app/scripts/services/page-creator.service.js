(function() {
    'use strict';

    angular.
        module('angularApp').factory('PageCreatorService', PageCreatorService);

    PageCreatorService.$inject = ['RouteService', 'RuntimeStatesProvider', 'tc', '$translate'];

    function PageCreatorService(RouteService, RuntimeStatesProvider, tc, $translate) {
        var route = {};

        route.createStates = function() {
            var pages = tc.config().pages;

            _.each(pages, function(page) {
                var state = {
                    url: page.url,
                    template: $translate.instant(page.templateTranslateName)
                };

                RuntimeStatesProvider.addState(page.stateName, state);
            });

        };

        return route;
    }
})();
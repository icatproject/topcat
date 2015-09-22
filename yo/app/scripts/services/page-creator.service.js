(function() {
    'use strict';

    angular.
        module('angularApp').factory('PageCreatorService', PageCreatorService);

    PageCreatorService.$inject = ['APP_CONFIG', 'Config', 'RouteService', 'RuntimeStatesProvider', '$translate'];

    function PageCreatorService(APP_CONFIG, Config, RouteService, RuntimeStatesProvider, $translate) {
        var route = {};

        route.createStates = function() {
            var pages = Config.getPages(APP_CONFIG);

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
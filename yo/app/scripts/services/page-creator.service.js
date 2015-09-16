(function() {
    'use strict';

    angular.
        module('angularApp').factory('PageCreatorService', PageCreatorService);

    PageCreatorService.$inject = ['APP_CONFIG', 'Config', 'RouteService', 'RuntimeStatesProvider', '$translate', '$log'];

    function PageCreatorService(APP_CONFIG, Config, RouteService, RuntimeStatesProvider, $translate, $log) {
        var route = {};

        route.createStates = function() {
            var pages = Config.getPages(APP_CONFIG);

            $log.debug('pages', pages);

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
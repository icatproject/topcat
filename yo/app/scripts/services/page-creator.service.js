(function() {
    'use strict';

    angular.
        module('angularApp').factory('PageCreatorService', PageCreatorService);

    PageCreatorService.$inject = ['RouteService', 'RuntimeStatesProvider', 'tc', 'helpers', '$translate'];

    function PageCreatorService(RouteService, RuntimeStatesProvider, tc, helpers, $translate) {
        var route = {};

        route.createStates = function() {
            var pages = tc.config().pages;

            _.each(pages, function(page) {
                if(!page.contents){
                    page.contents = 'PAGE.' + helpers.constantify(page.stateName) + '.HTML';
                }

                var state = {
                    url: page.url,
                    template: $translate.instant(page.contents)
                };

                RuntimeStatesProvider.addState(page.stateName, state);
            });

        };

        return route;
    }
})();
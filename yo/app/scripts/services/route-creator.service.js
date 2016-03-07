(function() {
    'use strict';

    angular.
        module('angularApp').factory('RouteCreatorService', RouteCreatorService);

    RouteCreatorService.$inject = ['APP_CONFIG', 'RouteService', 'RuntimeStatesProvider'];

    /*jshint -W098 */
    function RouteCreatorService(APP_CONFIG, RouteService, RuntimeStatesProvider) {
        var route = {};

        route.createStates = function() {
            var routes = RouteService.getAllRoutes(APP_CONFIG);

            _.each(routes, function(route) {
                var stateName = 'home.browse.facility.' + route.route;
                var state = {
                    url: route.url,
                    views: {
                        '@home.browse' : {
                            templateUrl: 'views/browse-entities.html',
                            controller: 'BrowseEntitiesController as browseEntitiesController'
                        },
                        'meta-view@home.browse' : {
                            templateUrl: 'views/partial-meta-panel.html',
                            controller: 'MetaPanelController as meta'
                        }
                    },
                    param: {
                        entityType : route.entity
                    },
                    reloadOnSearch: false
            };

                RuntimeStatesProvider.addState(stateName, state);
            });

        };

        return route;
    }
})();
(function() {
    'use strict';

    angular.
        module('angularApp').factory('RouteCreatorService', RouteCreatorService);

    RouteCreatorService.$inject = ['APP_CONFIG', 'Config', 'RouteService', 'RuntimeStatesProvider'];

    /*jshint -W098 */
    function RouteCreatorService(APP_CONFIG, Config, RouteService, RuntimeStatesProvider) {
        var route = {};

        route.createStates = function() {
            var hierarchy = [
                'facility',
                'instrument',
                'facilityCycle',
                'proposal',
                'investigation',
                'dataset',
                'datafile'
            ];

            var routes = RouteService.getAllRoutes(hierarchy);

            _.each(routes, function(route) {
                var stateName = 'home.browse.facility.' + route.route;
                var state = {
                    url: route.url,
                        views: {
                            '@home.browse' : {
                                templateUrl: 'views/partial-browse-panel.html',
                                controller: 'BrowseEntitiesController as vm'
                            },
                            'meta-view@home.browse' : {
                                templateUrl: 'views/partial-meta-panel.html',
                                controller: 'MetaPanelController as meta'
                            }
                        },
                        param: {
                            entityType : route.entity
                        }
                };

                RuntimeStatesProvider.addState(stateName, state);
            });

        };

        return route;
    }
})();
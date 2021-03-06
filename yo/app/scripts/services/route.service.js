(function() {
    'use strict';

    angular.
        module('topcat').service('RouteService', RouteService);

    RouteService.$inject = [];

    /*jshint -W098 */
    function RouteService() {

        function getRouteSegments(items) {
            return items.join('-');
        }

        function getPossibleRouteNames(APP_CONFIG) {
            var routes= [];

            _.each(APP_CONFIG.facilities, function(facility){
                //clone the array
                var hierarchy = facility.hierarchy.slice(0);
                var len = hierarchy.length;

                for (var i = 0; i < len - 1; i++) {
                    //if (len > 1) {
                        routes.push(hierarchy.join('-'));
                    //}
                    hierarchy.pop();
                }

            });

            return _.uniq(routes);
        }

        this.getAllRoutes = function(APP_CONFIG) {
            var routeNames = getPossibleRouteNames(APP_CONFIG);
            //set = setFilter(set, 'facility');

            var routes = [];

            _.each(routeNames, function(routeName) {
                var val = routeName.split('-');

                var url = '/{facilityName}';
                var len = val.length;
                var entity = _.last(val);
                _.each(val, function(v, index) {
                    //skip facility
                    if (v === 'facility') {
                        return;
                    }

                    url = url + '/' + v;

                    if (index !== len - 1) {
                        url = url + '/{' +v  + 'Id}';
                    }
                });

                url += '?uiGridState';

                routes.push({
                    route: getRouteSegments(val),
                    url: url,
                    entity: entity
                });

            });

            return routes;
        };

        this.getRoutes = function(hierarchy) {
            var clone = hierarchy.slice(0);
            var routes = [];
            var items = [];

            _.each(clone, function(val) {
                items.push(val);

                routes.push({
                    route: getRouteSegments(items),
                    entity: val
                });

            });

            return routes;
        };

        this.getNextRouteSegmentName = function(hierarchy, currentEntityType) {
            var index = _.indexOf(hierarchy, currentEntityType);

            if (index !== -1) {
                var items = hierarchy.slice(0, (index + 1) + 1);

                return getRouteSegments(items);
            } else {
                throw new Error('unable to determine next route');
            }
        };

        this.getCurrentEntityType = function($state) {
            if (angular.isDefined($state.current.param)) {
                return $state.current.param.entityType || 'facility';
            }

            return 'facility';
        };

        this.getCurrentRouteSegmentName = function($state){
            var routeName = $state.current.name;

            return routeName.substr(routeName.lastIndexOf('.') + 1);
        };

        this.getLastTwoSegment = function (currentRouteSegment) {
            var segments = currentRouteSegment.split('-');

            segments = segments.slice(-2);

            return getRouteSegments(segments);
        };

        this.getPreviousRoutes = function($state) {
            var currentRouteSegment = this.getCurrentRouteSegmentName($state);
            var segments = currentRouteSegment.split('-');
            var routes = this.getRoutes(segments);

            return routes;
        };
    }
})();






(function() {
    'use strict';

    angular.
        module('angularApp').service('RouteService', RouteService);

    RouteService.$inject = [];

    /*jshint -W098 */
    function RouteService() {
        /**
         * Join the array elements to route name string
         * @param  {[type]} items [description]
         * @return {[type]}       [description]
         */
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

        /**
         * Get the unique possible route names from the configured hierarchies
         * @param  {[type]} APP_CONFIG [description]
         * @return {[type]}            [description]
         */
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

                routes.push({
                    route: getRouteSegments(val),
                    url: url,
                    entity: entity
                });

            });

            return routes;
        };

        /**
         * get the available routes names for a particular hierarchy
         * @param  {[type]} hierarchy [description]
         * @return {[type]}           [description]
         */
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

        /**
         * get the next route segement
         * @param  {[type]} hierarchy         [description]
         * @param  {[type]} currentEntityType [description]
         * @return {[type]}                   [description]
         */
        this.getNextRouteSegmentName = function(hierarchy, currentEntityType) {
            var index = _.indexOf(hierarchy, currentEntityType);

            if (index !== -1) {
                var items = hierarchy.slice(0, (index + 1) + 1);

                return getRouteSegments(items);
            } else {
                throw new Error('unable to determine next route');
            }
        };

        /**
         * get the current entity type from the state
         * @param  {[type]} $state [description]
         * @return {[type]}        [description]
         */
        this.getCurrentEntityType = function($state) {
            if (angular.isDefined($state.current.param)) {
                return $state.current.param.entityType || 'facility';
            }

            return 'facility';
        };

        /**
         * get the current route segment name
         * @param  {[type]} $state [description]
         * @return {[type]}        [description]
         */
        this.getCurrentRouteSegmentName = function($state){
            var routeName = $state.current.name;

            return routeName.substr(routeName.lastIndexOf('.') + 1);
        };

        /**
         * get the last 2 parts of a route
         * @param  {[type]} currentRouteSegment [description]
         * @return {[type]}                     [description]
         */
        this.getLastTwoSegment = function (currentRouteSegment) {
            var segments = currentRouteSegment.split('-');

            segments = segments.slice(-2);

            return getRouteSegments(segments);
        };

        /**
         * get the previous route
         * @param  {[type]} $state [description]
         * @return {[type]}        [description]
         */
        this.getPreviousRoutes = function($state) {
            var currentRouteSegment = this.getCurrentRouteSegmentName($state);
            var segments = currentRouteSegment.split('-');
            var routes = this.getRoutes(segments);

            return routes;
        };
    }
})();






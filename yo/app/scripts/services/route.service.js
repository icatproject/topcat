(function() {
    'use strict';

    angular.
        module('angularApp').factory('RouteService', RouteService);

    RouteService.$inject = [];

    /*jshint -W098 */
    function RouteService() {
        var route = {};

        /**
         * function to get the poser set of an array
         * @param  {[type]} items [description]
         * @return {[type]}      [description]
         */
        function powerset(items) {
            var ps = [[]];
            for (var i=0; i < items.length; i++) {
                for (var j = 0, len = ps.length; j < len; j++) {
                    ps.push(ps[j].concat(items[i]));
                }
            }

            return ps;
        }

        /**
         * This function returna only the arrays where the
         * first element value equals the filter value and the array length is greater
         * than one.
         *
         * The array are all the possible routes for a given an hierarchy.
         * The size of the array is equal to 2 (power of hierarchy size - 1) - 1
         *
         * @param  {[type]} items   [description]
         * @param  {[type]} filter [description]
         * @return {[type]}        [description]
         */
        function setFilter(items, filter) {
            var set = [];

            _.each(items, function(item){
                if (item.length > 1) {
                    if (_.first(item) === filter) {
                        set.push(item);
                    }
                }
            });

            return set;
        }

        /**
         * Join the array elements to route name string
         * @param  {[type]} items [description]
         * @return {[type]}       [description]
         */
        function getRouteSegments(items) {
            return items.join('-');
        }


        /*function getNextEntityType(hierarchy, currentEntityType) {
            if (hierarchy[hierarchy.length] === currentEntityType) {
                return false;
            }

            var index;
            for (index = 0; index < hierarchy.length; ++index) {
                if (hierarchy[index] === currentEntityType) {
                    break;
                }
            }

            return hierarchy[index + 1];
        }*/

        function sortEntities(items) {
            var order = [
                'facility',
                'instrument',
                'facilityCycle',
                'proposal',
                'investigation',
                'dataset',
                'datafile'
            ];

            return items.sort(function(a,b){
                return order.indexOf(a) - order.indexOf(b);
            });
        }


        /*route.getRouteSegmentPairs = function (hierarchy) {
            var list = pairwise(hierarchy);

            return _.map(list, function(pair) {
                return pair[0] + '-' + pair[1];
            });
        };*/

        route.getPossibleRoutes = function(APP_CONFIG) {
            var union = [];

            _.each(APP_CONFIG.facilities, function(facilities) {
                union = _.union(union, facilities.hierarchy);
            });

            return sortEntities(union);
        };



        route.getAllRoutes = function(hierarchy) {
            var set = powerset(hierarchy);
            set = setFilter(set, 'facility');

            var routes = [];

            _.each(set, function(val) {
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

            //$log.info('routes', JSON.stringify(routes, null, 2));

            return routes;
        };


        route.getRoutes = function(hierarchy) {
            //var set = powerset(hierarchy);
            //set = setFilter(set, 'facility');
            //
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

        route.getNextRouteSegmentName = function(hierarchy, currentEntityType) {
            var index = _.indexOf(hierarchy, currentEntityType);

            if (index !== -1) {
                var items = hierarchy.slice(0, (index + 1) + 1);
                return getRouteSegments(items);
            } else {
                throw new Error('unable to determine next route');
            }
        };

        route.getCurrentEntityType = function($state) {
            if (angular.isDefined($state.current.param)) {
                return $state.current.param.entityType || 'facility';
            }

            return 'facility';
        };

        route.getCurrentRouteSegmentName = function($state){
            var routeName = $state.current.name;

            return routeName.substr(routeName.lastIndexOf('.') + 1);
        };

        route.getLastTwoSegment = function (currentRouteSegment) {
            var segments = currentRouteSegment.split('-');

            segments = segments.slice(-2);

            return getRouteSegments(segments);
        };

        route.getPreviousRoutes = function($state) {
            var currentRouteSegment = this.getCurrentRouteSegmentName($state);
            var segments = currentRouteSegment.split('-');
            var routes = this.getRoutes(segments);

            return routes;
        };

        return route;
    }
})();






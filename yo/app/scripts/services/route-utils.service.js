'use strict';

angular
    .module('angularApp')
    .factory('RouteUtils', RouteUtils);

RouteUtils.$inject = [];

function RouteUtils(){
    return {
        /**
         * Returns the current entity type base on the current route param entityType.
         * Default to facility if non is specified
         *
         * @param  {Object} the $state object
         * @return {String} the current entity type
         */
        getCurrentEntityType : function($state) {
            if (angular.isDefined($state.current.param)) {
                return $state.current.param.entityType || 'facility';
            }

            return 'facility';
        },


        /**
         * [getNextEntityType description]
         * @param  {Array} the array structure of the facility
         * @param  {String} the current entity type
         * @return {String || false} Returns the next item in the array or false if already last item
         */
        getNextEntityType : function(structure, currentEntityType) {
            if (structure[structure.length] === currentEntityType) {
                return false;
            }

            var index;
            for (index = 0; index < structure.length; ++index) {
                if (structure[index] === currentEntityType) {
                    break;
                }
            }

            return structure[index + 1];
        },


        /**
         * Get the next ui-route name to go up the facility
         * structure
         *
         * @param  {[type]} structure         [description]
         * @param  {[type]} currentEntityType [description]
         * @return {[type]}                   [description]
         */
        getNextRouteSegmentName : function(structure, currentEntityType) {
            var next = this.getNextEntityType(structure, currentEntityType);
            if (angular.isDefined(next)) {
                return currentEntityType + '-' + next;
            } else {
                return currentEntityType;
            }
        },

        /**
         * Returns the last segment of the ui-route name.
         * This is use to determin which function to us to
         * get data
         *
         * @param  {[type]} $state [description]
         * @return {[type]}        [description]
         */
        getCurrentRouteSegmentName: function($state){
            var routeName = $state.current.name;

            routeName = routeName.substr(routeName.lastIndexOf('.') + 1);

            //this is a hack until we figure out how to do the meta tabs routing!!!
            if (routeName.indexOf('meta') >= 0) {
                routeName = 'facility';
            }

            return routeName;
        }


    };
}


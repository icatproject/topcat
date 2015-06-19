(function() {
    'use strict';

    /**
     * This is a wrapper filter for pretty bytes. The pretty bytes does not take a string
     * so we have to do a parseInt
     */
    angular.module('bytes', []).
        filter('bytes', ['$filter', function( $filter) {
            var prettyBytesFilter = $filter('prettyBytes');
            return function(value) {
                if (value === null || typeof value === 'undefined') {
                    return null;
                }

                //-1 is a failure
                if (value === -1) {
                    return value;
                }

                var bytes = parseInt(value);

                return prettyBytesFilter(bytes);
            };
        }]);
})();
//dataset 186059
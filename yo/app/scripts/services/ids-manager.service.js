(function() {
    'use strict';

    angular.
        module('angularApp').service('IdsManager', IdsManager);

    IdsManager.$inject = ['$http', '$q', 'IdsService'];

    function IdsManager($http, $q, IdsService) {
        function getErrorMessage(error) {
            var errorMessage = '';

            if (error.status === 0) {
                errorMessage = 'Unable to contact ids server';
            } else {
                if (error.data !== null) {
                    if (typeof error.data.message !== 'undefined') {
                        errorMessage = error.data.message;
                    } else {
                        errorMessage = 'Unable to retrieve data';
                    }
                } else {
                    errorMessage = 'Unknown error';
                }
            }

            return errorMessage;
        }

        /**
         * Get the session value for the facility that was passed
         * @param  {[type]} session  [description]
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        function getSessionValueForFacility(sessions, facility) {
            return sessions[facility.facilityName].sessionId;
        }


        /**
         * Get the size from the ids
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        this.getSize = function(sessions, facility, options) {
            var sessionId = getSessionValueForFacility(sessions, facility);
            var def = $q.defer();

            IdsService.getSize(sessionId, facility, options).then(function(data) {
                def.resolve(data.data);
            }, function(error) {
                def.reject('Failed to retrieve data for facility ' + error.config.info.facilityTitle + ': ' + getErrorMessage(error, status));
            });

            return def.promise;
        };


        /**
         * Get the size from the ids
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        this.isTwoLevel = function(facility, options) {
            var def = $q.defer();

            IdsService.isTwoLevel(facility, options).then(function(data) {
                if (data.data === 'true') {
                    def.resolve(true);
                } else {
                    def.resolve(false);
                }
            }, function(error) {
                def.reject('Failed to retrieve data for facility ' + error.config.info.facilityTitle + ': ' + getErrorMessage(error, status));
            });

            return def.promise;
        };

        /**
         * Get the status from the ids
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        this.getStatus = function(sessions, facility, options) {
            var sessionId = getSessionValueForFacility(sessions, facility);
            var def = $q.defer();

            IdsService.getStatus(sessionId, facility, options).then(function(data) {
                def.resolve(data.data);
            }, function(error) {
                def.reject('Failed to retrieve data for facility ' + error.config.info.facilityTitle + ': ' + getErrorMessage(error, status));
            });

            return def.promise;
        };
    }
})();
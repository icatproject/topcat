(function() {
    'use strict';

    angular.
        module('angularApp').factory('IdsManager', IdsManager);

    IdsManager.$inject = ['$http', '$q', 'IdsService', '$log'];

    function IdsManager($http, $q, IdsService, $log) { //jshint ignore: line
        var manager = {};

        /*function MyException(message) {
          this.name = name;
          this.message = message;
        }
        MyException.prototype = new Error();
        MyException.prototype.constructor = MyException;*/

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
        manager.getSize = function(sessions, facility, options) {
            $log.debug('IdsManager getSize called');
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
         * Get the status from the ids
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        manager.getStatus = function(sessions, facility, options) {
            var sessionId = getSessionValueForFacility(sessions, facility);
            var def = $q.defer();

            IdsService.getStatus(sessionId, facility, options).then(function(data) {
                def.resolve(data.data);
            }, function(error) {
                def.reject('Failed to retrieve data for facility ' + error.config.info.facilityTitle + ': ' + getErrorMessage(error, status));
            });

            return def.promise;
        };

        return manager;
    }
})();
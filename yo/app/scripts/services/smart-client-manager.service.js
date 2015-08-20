(function() {
    'use strict';

    angular.
        module('angularApp').factory('SmartClientManager', SmartClientManager);

    SmartClientManager.$inject = ['$http', '$q', 'SmartClientService', '$log'];

    function SmartClientManager($http, $q, SmartClientService, $log) { //jshint ignore: line
        var manager = {};

        function getErrorMessage(error) {
            var errorMessage = '';

            if (error.status === 0) {
                errorMessage = 'Unable to contact the Smartclient';
            } else {
                if (error.data !== null) {
                    if (typeof error.data.message !== 'undefined') {
                        errorMessage = error.data.message;
                    } else {
                        errorMessage = 'Unable to retrieve data from the Smartclient';
                    }
                } else {
                    errorMessage = 'Unknown error';
                }
            }

            return errorMessage;
        }


        /**
         * Get the size from the smartclient
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        manager.ping = function() {
            $log.debug('SmartClientManager ping called');
            var def = $q.defer();

            SmartClientService.ping().then(function() {
                def.resolve({ping: 'online'});
            }, function(error) { //jshint ignore: line
                def.resolve({ping: 'offline'});
                //def.reject('Failed to ping the Smartclient: ' + getErrorMessage(error, status));
            });

            $log.debug('def.promise', def.promise);

            return def.promise;
        };

        manager.getData = function(mySessionId, facility, preparedId) {
            $log.debug('SmartClientManager getData called');
            var def = $q.defer();

            SmartClientService.login(mySessionId, facility).then(function() {
                SmartClientService.getData(facility, preparedId).then(function() {
                    def.resolve({preparedId: preparedId});
                }, function(error) {
                    def.reject('Failed to submit data to the Smartclient: ' + getErrorMessage(error, status));
                });
            }, function(error) {
                def.reject('Failed to login to the Smartclient: ' + getErrorMessage(error, status));
            });

            return def.promise;
        };

        /**
         * Get the status from the smartclient
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        manager.getStatus = function() {
            var def = $q.defer();

            SmartClientService.getStatus().then(function(data) {
                def.resolve(data.data);
            }, function(error) {
                def.reject('Failed to retrieve data form the Smartclient :' + getErrorMessage(error, status));
            });

            return def.promise;
        };

        return manager;
    }
})();
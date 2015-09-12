(function() {
    'use strict';

    angular.
        module('angularApp').service('SmartClientManager', SmartClientManager);

    SmartClientManager.$inject = ['$http', '$q', 'SmartClientService'];

    function SmartClientManager($http, $q, SmartClientService) {
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
        this.ping = function() {
            var def = $q.defer();

            SmartClientService.ping().then(function() {
                def.resolve({ping: 'online'});
            }, function(error) { //jshint ignore: line
                def.resolve({ping: 'offline'});
                //def.reject('Failed to ping the Smartclient: ' + getErrorMessage(error, status));
            });

            return def.promise;
        };

        this.getData = function(mySessionId, facility, preparedId) {
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

        this.connect = function(mySessionId, facility) {
            var def = $q.defer();

            SmartClientService.login(mySessionId, facility).then(function() {
                def.resolve({connected: 'ok'});
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
        this.getStatus = function() {
            var def = $q.defer();

            SmartClientService.getStatus().then(function(data) {
                def.resolve(data.data);
            }, function(error) {
                def.reject('Failed to retrieve data form the Smartclient :' + getErrorMessage(error, status));
            });

            return def.promise;
        };
    }
})();
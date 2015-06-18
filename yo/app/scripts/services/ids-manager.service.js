(function() {
    'use strict';

    angular.
        module('angularApp').factory('IdsManager', IdsManager);

    IdsManager.$inject = ['$http', '$q', 'IdsService', '$log'];

    function IdsManager($http, $q, IdsService, $log) {
        var manager = {};

        function MyException(message) {
          this.name = name;
          this.message = message;
        }
        MyException.prototype = new Error();
        MyException.prototype.constructor = MyException;

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
            var sessionId = getSessionValueForFacility(sessions, facility);
            var def = $q.defer();

            IdsService.getSize(sessionId, facility, options).then(function(data) {
                def.resolve(data.data);
            }, function(){
                def.reject('Failed to retrieve data');
                throw new MyException('Failed to retrieve size from the ids server');
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
            }, function(){
                def.reject('Failed to retrieve data');
                throw new MyException('Failed to retrieve staus from the ids server');
            });

            return def.promise;
        };

        return manager;
    }
})();
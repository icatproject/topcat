(function() {
    'use strict';

    angular.
        module('angularApp').service('SessionManager', SessionManager);

    SessionManager.$inject = ['APP_CONFIG', 'Config', '$sessionStorage'];

    function SessionManager(APP_CONFIG, Config, $sessionStorage) {
        /**
         * this clean up the user sessionStorage to remove
         * any facility that no longer exists in the topcat configuration
         * @return {[type]} [description]
         */
        this.cleanup = function() {
            var facilitiesInSession = _.keys($sessionStorage.sessions);
            var facilitiesInConfig = _.keys(Config.getFacilities(APP_CONFIG));
            var nonExistfacilities = _.difference(facilitiesInSession, facilitiesInConfig);

            _.each(nonExistfacilities, function(facility) {
                delete $sessionStorage.sessions[facility];
            });
        };
    }
})();
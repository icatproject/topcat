(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LogoutController', LogoutController);

    LogoutController.$inject = ['$state', '$stateParams', 'APP_CONFIG', 'Config', '$translate', 'DataManager', '$sessionStorage', 'inform'];

    function LogoutController($state, $stateParams, APP_CONFIG, Config, $translate, DataManager, $sessionStorage, inform) {
        if(angular.isDefined($state.params.facilityName)) {
            //logout of single facility
            var facility = Config.getFacilityByName(APP_CONFIG, $state.params.facilityName);
            DataManager.logout($sessionStorage.sessions, facility);

            //delete session from sessionStorage
            delete $sessionStorage.sessions[$state.params.facilityName];

            $translate('INFO.LOGOUT.LOGOUT', {facilityName: $state.params.facilityName}).then(function (translation) {
                inform.add(translation, {
                    'ttl': 1500,
                    'type': 'info'
                });
            });
        } else {
            //loop sessions in sessionStorage and logout of of each facility
            _.each(_.keys($sessionStorage.sessions), function(keyName) {
                var facility = Config.getFacilityByName(APP_CONFIG, keyName);
                DataManager.logout($sessionStorage.sessions, facility);
            });

            //logout of all facility
            $sessionStorage.sessions = {};

            $translate('INFO.LOGOUT.LOGOUT_ALL').then(function (translation) {
                inform.add(translation, {
                    'ttl': 1500,
                    'type': 'info'
                });
            });
        }

        //Go to login page if not logged into any facility. Else go to facilities list
        if (_.isEmpty($sessionStorage.sessions)) {
            $state.go('login');
        } else {
            $state.go('home.browse.facilities');
        }
    }
})();

(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LogoutController', LogoutController);

    LogoutController.$inject = ['$state', '$stateParams', 'APP_CONFIG', 'Config', '$translate', 'DataManager', '$sessionStorage', 'inform'];

    function LogoutController($state, $stateParams, APP_CONFIG, Config, $translate, DataManager, $sessionStorage, inform) {
        if(angular.isDefined($state.params.facilityName)) {
            //logout of single facility
            delete $sessionStorage.sessions[$state.params.facilityName];

            $translate('INFO.LOGOUT.LOGOUT', {facilityName: $state.params.facilityName}).then(function (translation) {
                inform.add(translation, {
                    'ttl': 3000,
                    'type': 'info'
                });
            });
        } else {
            //logout of all facility
            $sessionStorage.sessions = {};

            $translate('INFO.LOGOUT.LOGOUT_ALL').then(function (translation) {
                inform.add(translation, {
                    'ttl': 3000,
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

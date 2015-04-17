(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LogoutController', LogoutController);

    LogoutController.$inject = ['$state', 'APP_CONFIG', 'Config', 'DataManager', '$sessionStorage', 'inform'];

    function LogoutController($state, APP_CONFIG, Config, DataManager, $sessionStorage, inform) {
        if(angular.isDefined($sessionStorage.sessions)) {
            $sessionStorage.sessions = {};
        }

        var message = 'You are logged out of all facilities';

        inform.add(message, {
                            'ttl': 3000,
                            'type': 'danger'
                        });

        $state.go('login');
    }
})();

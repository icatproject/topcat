(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LogoutController', LogoutController);

    LogoutController.$inject = ['$rootScope', '$state', '$stateParams', 'APP_CONFIG', 'Config', 'RouteUtils', '$translate', 'DataManager', '$sessionStorage', 'inform', '$log'];

    function LogoutController($rootScope, $state, $stateParams, APP_CONFIG, Config, RouteUtils, $translate, DataManager, $sessionStorage, inform, $log) {
        if(angular.isDefined($state.params.facilityName)) {
            //logout of single facility
            var facility = Config.getFacilityByName(APP_CONFIG, $state.params.facilityName);
            DataManager.logout($sessionStorage.sessions, facility).then(function() {
                //delete session from sessionStorage
                $log.debug('$state.params.facilityName', $state.params.facilityName);

                delete $sessionStorage.sessions[$state.params.facilityName];

                $rootScope.$broadcast('Logout:success', {facility: $state.params.facilityName});

                inform.add($translate.instant('INFO.LOGOUT.LOGOUT', {facilityName: facility.title}), {
                    'ttl': 4000,
                    'type': 'info'
                });

                //Go to login page if not logged into any facility. Else go to facilities list
                if (_.isEmpty($sessionStorage.sessions)) {
                    $state.go('login');
                } else {
                    $state.go(RouteUtils.getHomeRouteName());
                }
            }, function(error) {
                inform.add(error, {
                    'ttl': 4000,
                    'type': 'danger'
                });
            });
        } else {
            //loop sessions in sessionStorage and logout of of each facility
            _.each(_.keys($sessionStorage.sessions), function(facilityName) {
                var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
                DataManager.logout($sessionStorage.sessions, facility);
            });

            //logout of all facility
            $sessionStorage.sessions = {};

            $rootScope.$broadcast('Logout:success', {facility: 'all'});

            inform.add($translate.instant('INFO.LOGOUT.LOGOUT_ALL'), {
                'ttl': 4000,
                'type': 'info'
            });

            //go to login page
            $state.go('login');
        }
    }
})();

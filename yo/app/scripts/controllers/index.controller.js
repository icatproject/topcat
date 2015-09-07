(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', '$translate', 'APP_CONFIG', 'Config', '$sessionStorage'];

    function IndexController($scope, $translate, APP_CONFIG, Config, $sessionStorage) {
        var vm = this;

        vm.changeLanguage = function (langKey) {
            $translate.use(langKey);
        };

        vm.facilitiesToLogout = function(){
            return _.keys($sessionStorage.sessions);
        };

        vm.isLoggedIn = function(){
            return ! (_.isEmpty($sessionStorage.sessions));
        };

        vm.facilities = function() {
            Config.getFacilities(APP_CONFIG);
        };

        vm.getUserNameByFacilityName = function(facilityName) {
            return $sessionStorage.sessions[facilityName].userName;
        };

        vm.getFacilityTitleByFacilityName = function(facilityName) {
            return Config.getFacilityTitleByFacilityName(APP_CONFIG, facilityName);
        };
    }
})();

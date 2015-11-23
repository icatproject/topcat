(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', '$translate', 'APP_CONFIG', 'Config', '$sessionStorage', '$state'];

    function IndexController($scope, $translate, APP_CONFIG, Config, $sessionStorage, $state) {
        var vm = this;

        var pages = Config.getPages(APP_CONFIG);

        var leftLinks = [];
        var rightLinks = [];

        _.each(pages, function(page) {
            if (typeof page.addToNavBar !== 'undefined') {
                if (typeof page.addToNavBar.align === 'undefined' || page.addToNavBar.align === 'left') {
                    leftLinks.push(page);
                } else if (page.addToNavBar.align === 'right'){
                    rightLinks.push(page);
                }
            }
        });

        var facilities = Config.getFacilities(APP_CONFIG);

        vm.facilities = facilities;

        vm.euCookieLaw = Config.getEuCookieLaw(APP_CONFIG);

        var maintenanceMode = APP_CONFIG.site.maintenanceMode;
        if(maintenanceMode){
            this.showMaintenanceMode = maintenanceMode.show;
        }

        vm.changeLanguage = function (langKey) {
            $translate.use(langKey);
        };

        vm.facilitiesToLogout = function(){
            return _.keys($sessionStorage.sessions);
        };

        vm.isLoggedIn = function(){
            return ! (_.isEmpty($sessionStorage.sessions));
        };

        vm.getUserNameByFacilityName = function(facilityName) {
            if (typeof $sessionStorage.sessions[facilityName] !== 'undefined') {
                return $sessionStorage.sessions[facilityName].userName;
            }
        };

        vm.getFacilityTitleByFacilityName = function(facilityName) {
            return Config.getFacilityTitleByFacilityName(APP_CONFIG, facilityName);
        };

        vm.leftLinks = leftLinks;
        vm.rightLinks = rightLinks;

        vm.isActive = function(page) {
            return $state.includes(page.stateName);
        };

        vm.isSingleFacility = function () {
            if (_.size(facilities) === 1) {
                return true;
            }

            return false;
        };

        vm.getSingleFacility = function () {
            return facilities[Object.keys(facilities)[0]];
        };
    }
})();

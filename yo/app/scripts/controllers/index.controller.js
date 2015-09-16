(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', '$translate', 'APP_CONFIG', 'Config', '$sessionStorage', '$state', '$log'];

    function IndexController($scope, $translate, APP_CONFIG, Config, $sessionStorage, $state, $log) {
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

        $log.debug('leftLinks', leftLinks);
        $log.debug('rightLinks', rightLinks);

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

        vm.leftLinks = leftLinks;
        vm.rightLinks = rightLinks;

        vm.isActive = function(page) {
            return $state.includes(page.stateName);
        };
    }
})();

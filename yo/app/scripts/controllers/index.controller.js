(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', '$rootScope', '$translate', '$uibModal', 'APP_CONFIG', 'Config', '$sessionStorage', '$state', 'tc', 'Cart'];

    function IndexController($scope, $rootScope, $translate, $uibModal, APP_CONFIG, Config, $sessionStorage, $state, tc, Cart) {
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

        vm.cartItemCount = 0;
        $rootScope.$on('cart:change', function(){
            vm.cartItemCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().cart().then(function(cart){
                    vm.cartItemCount = vm.cartItemCount + cart.cartItems.length;
                });
            });
        });

        $rootScope.$broadcast('cart:change');

        vm.downloadCount = 0;
        $rootScope.$on('download:change', function(){
            vm.downloadCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().downloads("where download.isDeleted = false").then(function(downloads){
                    vm.downloadCount = downloads.length;
                });
            });
        });

        $rootScope.$broadcast('download:change');

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

        vm.isAdmin = function(){
            return tc.adminFacilities().length > 0;
        };

        vm.getSingleFacility = function () {
            return facilities[Object.keys(facilities)[0]];
        };

        vm.showCart = function() {
            $uibModal.open({
                templateUrl: 'views/cart.html',
                controller: 'CartController as cartController',
                size : 'lg'
            }).opened.catch(function (error) {
                inform.add(error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });
        };

        vm.showDownloads = function() {
            $uibModal.open({
                templateUrl: 'views/downloads.html',
                controller: 'DownloadsController as downloadsController',
                size : 'lg'
            }).opened.catch(function (error) {
                inform.add(error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });
        };


        

    }
})();


(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('IndexController', function($rootScope, $q, $scope, $translate, $state, $uibModal, $timeout, $sessionStorage, tc, ipCookie){
        var that = this;

        this.facilities = tc.facilities();

        function refreshUserFacilities(){
            that.userFacilities = tc.userFacilities();
            that.nonUserFacilities = tc.nonUserFacilities();
            that.adminFacilities = tc.adminFacilities();
            refreshCartItemCount();
            refreshDownloadCount();
            tc.cache().removeAll();
            _.each(tc.facilities(), function(facility){
                facility.icat().cache().removeAll();
                facility.ids().cache().removeAll();
            });
        }
        $rootScope.$on('session:change', refreshUserFacilities);
        refreshUserFacilities();

        $rootScope.$on('http:error', function(){
            tc.purgeSessions().then(function(){
                if(tc.userFacilities().length == 0 && tc.config().maintenanceMode && tc.config().maintenanceMode.show == false){
                    $state.go("login");
                }
            });
        });

        this.leftLinks = [];
        this.rightLinks = [];

        _.each(tc.config().pages, function(page) {
            if (page.addToNavBar){
                if(page.addToNavBar.align == 'right'){
                    that.rightLinks.push(page);
                } else {
                    that.leftLinks.push(page);
                }
            }
        });

        this.cartItemCount = 0;
        this.isCartPopoverOpen = false;
        $rootScope.$on('cart:add', function(){
            that.isCartPopoverOpen = true;
        });

        var refreshCartItemCountTimeout;
        function refreshCartItemCount(){
            if(refreshCartItemCountTimeout) refreshCartItemCountTimeout.resolve();
            refreshCartItemCountTimeout = $q.defer();
            that.cartItemCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().cart(refreshCartItemCountTimeout.promise).then(function(cart){
                    that.cartItemCount = that.cartItemCount + cart.cartItems.length;
                    $timeout(function(){
                        $timeout(function(){
                            that.isCartPopoverOpen = false;
                        });
                    });
                });
            });
        }
        $rootScope.$on('cart:change', refreshCartItemCount);
        refreshCartItemCount();

        that.downloadCount = 0;
        this.isDownloadsPopoverOpen = false;
        $rootScope.$on('cart:submit', function(){
            that.isDownloadsPopoverOpen = true;
        });
        function refreshDownloadCount(){
            that.downloadCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().downloads("where download.isDeleted = false").then(function(downloads){
                    that.downloadCount = downloads.length;
                    $timeout(function(){
                        $timeout(function(){
                            that.isDownloadsPopoverOpen = false;
                        });
                    });
                });
            });
        };
        $rootScope.$on('download:change', refreshDownloadCount);
        refreshDownloadCount();

        this.changeLanguage = function(langKey) {
            $translate.use(langKey);
        };

        this.showCart = function() {
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

        this.showDownloads = function() {
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

        this.serviceStatus = tc.config().serviceStatus;

        tc.getConfVar('serviceStatus').then(function(serviceStatus){
            that.serviceStatus = serviceStatus;
        });

        if(!ipCookie('hideCookieMessage')){
            this.enableEuCookieLaw =  tc.config().enableEuCookieLaw;
            this.hideCookieMessage = function(){
                ipCookie('hideCookieMessage', true, { expires: 365 });
                this.enableEuCookieLaw = false;
            };
        }

        var refreshSessionInterval = setInterval(function(){
            _.each(tc.userFacilities(), function(facility){
                facility.icat().refreshSession();
            });
        }, 1000 * 60 * 5);
        $scope.$on('$destroy', function(){ clearInterval(refreshSessionInterval); });


        $rootScope.requestCounter = 0;
        $rootScope.updateLoadingState = function(){
            if(this.requestCounter == 0){
                this.$broadcast('loaded');
            }
        }


        $rootScope.$on('cas:authentication', function(event, facilityName, ticket){
            var service = window.location.href.replace(/#.*$/, '').replace(/[^\/]*$/, '') + 'cas?facilityName=' + facilityName;
            tc.icat(facilityName).login('cas', service, ticket).then(function(){
                var name;
                var params = {};
                if($sessionStorage.lastState){
                    name = $sessionStorage.lastState.name;
                    params = $sessionStorage.lastState.params;
                } else {
                    name = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
                }
                $state.go(name, params);
            });
        });

        this.maintenanceMode = tc.config().maintenanceMode;

    });

})();


(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('IndexController', function($rootScope, $q, $scope, $translate, $state, $uibModal, $timeout, tc, ipCookie){
        var that = this;

        this.facilities = tc.facilities();

        function refreshUserFacilities(){
            that.userFacilities = tc.userFacilities();
            that.nonUserFacilities = tc.nonUserFacilities();
            that.adminFacilities = tc.adminFacilities();
            refreshCartItemCount();
            refreshDownloadCount()
        }
        $rootScope.$on('session:change', refreshUserFacilities);
        refreshUserFacilities();

        $rootScope.$on('http:error', function(){
            tc.purgeSessions().then(function(){
                if(tc.userFacilities().length == 0){
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

        this.serviceStatus = tc.config().serviceStatus

        if(!ipCookie('hideCookieMessage')){
            this.enableEuCookieLaw =  tc.config().enableEuCookieLaw;
            this.hideCookieMessage = function(){
                ipCookie('hideCookieMessage', true, { expires: 365 });
                this.enableEuCookieLaw = false;
            };
        }

    });

})();

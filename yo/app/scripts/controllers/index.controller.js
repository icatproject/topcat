
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('IndexController', function($rootScope, $translate, $uibModal, tc){
        var that = this;

        this.facilities = tc.facilities();

        function refreshUserFacilities(){
            that.userFacilities = tc.userFacilities();
            that.nonUserFacilities = tc.nonUserFacilities();
            that.adminFacilities = tc.adminFacilities();
        }
        $rootScope.$on('session:change', refreshUserFacilities);
        refreshUserFacilities();


        this.enableEuCookieLaw = tc.config().enableEuCookieLaw;
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
        function refreshCartItemCount(){
            that.cartItemCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().cart().then(function(cart){
                    that.cartItemCount = that.cartItemCount + cart.cartItems.length;
                });
            });
        }
        $rootScope.$on('cart:change', refreshCartItemCount);
        refreshCartItemCount();

        that.downloadCount = 0;
        function refreshDownloadCount(){
            that.downloadCount = 0;
            _.each(tc.userFacilities(), function(facility){
                facility.user().downloads("where download.isDeleted = false").then(function(downloads){
                    that.downloadCount = downloads.length;
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

    });

})();

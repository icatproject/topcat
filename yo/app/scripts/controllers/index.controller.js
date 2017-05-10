
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('IndexController', function($rootScope, $q, $scope, $translate, $state, $uibModal, $timeout, $interval, $sessionStorage, tc, ipCookie){
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

        var completedDownloads = {};
        var completedDownloadsInit = false;
        var isSessionChanging = false;

        $rootScope.$on('session:changing', function(e){
            isSessionChanging = true;
        });

        $rootScope.$on('session:changed', function(e, _completedDownloads){
            completedDownloads = _completedDownloads;
            isSessionChanging = false;
        });

        this.isCompletedDownloadPopoverOpen = false;
        function checkoutForNewlyCompletedDownloads(){

            var promises = [];
            var data = [];

            _.each(tc.userFacilities(), function(facility){
                var smartclient = facility.smartclient();
                var smartclientPing = smartclient.isEnabled() ? smartclient.ping(timeout.promise) : $q.reject();

                promises.push(facility.user().downloads(["where download.isDeleted = false"], {bypassInterceptors: true}).then(function(downloads){
                    _.each(downloads, function(download){
                        var key = facility.config().name + ":" + download.id;
                        if(!completedDownloads[key] && download.status == 'COMPLETE'){
                            if(!isSessionChanging && completedDownloadsInit  && !download.isTwoLevel){
                                if(download.transport == 'https'){
                                    var url = download.transportUrl + '/ids/getData?preparedId=' + download.preparedId + '&outname=' + download.fileName;
                                    var iframe = $('<iframe>').attr('src', url).css({
                                        position: 'absolute',
                                        left: '-1000000px',
                                        height: '1px',
                                        width: '1px'
                                    });

                                    $('body').append(iframe);
                                } else {
                                    that.isCompletedDownloadPopoverOpen = true;
                                    $timeout(function(){
                                        $timeout(function(){
                                            that.isCompletedDownloadPopoverOpen = false;
                                        });
                                    });
                                }
                            }
                            completedDownloads[key] = true;
                        }

                        if(download.transport == 'smartclient' && download.status != 'COMPLETE'){
                            smartclientPing.then(function(isServer){
                                download.isServer = isServer;
                            });
                        }
                    });
                    data = _.flatten([data, downloads]);
                }));
            });

            $q.all(promises).then(function(){
                completedDownloadsInit = true;
                $rootScope.$broadcast('downloads:update', data);
            });
        }
        $interval(checkoutForNewlyCompletedDownloads, 1000);
        checkoutForNewlyCompletedDownloads();

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

        var smartClientPingDone = true;
        function pingSmartclient(){
            if(!smartClientPingDone) return;
            smartClientPingDone = false;
            var promises = [];
            var options = {bypassInterceptors: true, lowPriority: true};
            _.each(tc.userFacilities(), function(facility){
                var smartclient = facility.smartclient();
                promises.push(smartclient.ping(options).then(function(smartclientIsAvailable){
                    if(smartclientIsAvailable){
                        return smartclient.login(options).then(function(){
                            return facility.user().downloads(["where download.isDeleted = false and download.transport = 'smartclient' and download.status != org.icatproject.topcat.domain.DownloadStatus.COMPLETE"], options).then(function(downloads){
                                var promises = []
                                _.each(downloads, function(download){
                                    promises.push(smartclient.getData(download.preparedId, options).then(function(){
                                        return smartclient.isReady(download.preparedId, options).then(function(isReady){
                                            if(isReady) return facility.user().setDownloadStatus(download.id, 'COMPLETE', options);
                                        });
                                    }));
                                });
                                return $q.all(promises);
                            });
                        });
                    }
                }));
            });
            $q.all(promises).then(function(){
                smartClientPingDone = true;
                $rootScope.$broadcast('downloads:');
            });
        }

        $interval(pingSmartclient, 1000 * 60);

    });

})();


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
                    that.downloadCount += downloads.length;
                    $timeout(function(){
                        $timeout(function(){
                            that.isDownloadsPopoverOpen = false;
                        });
                    });
                }, function(response){
                    console.log("refreshing download count - failed", response);
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
                            if(!isSessionChanging && completedDownloadsInit  && download.isTwoLevel){
                                that.isCompletedDownloadPopoverOpen = true;
                                $timeout(function(){
                                    $timeout(function(){
                                        that.isCompletedDownloadPopoverOpen = false;
                                    });
                                });
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
        $interval(checkoutForNewlyCompletedDownloads, 60 * 1000);
        checkoutForNewlyCompletedDownloads();
        $rootScope.$on('downloads:dialog_opened', checkoutForNewlyCompletedDownloads);

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

        if(!ipCookie('hideCookieMessage')){
            this.enableEuCookieLaw =  tc.config().enableEuCookieLaw;
            this.hideCookieMessage = function(){
                ipCookie('hideCookieMessage', true, { expires: 365 });
                this.enableEuCookieLaw = false;
            };
        }

        var refreshSessionInterval = setInterval(function(){
            _.each(tc.userFacilities(), function(facility){
                facility.icat().refreshSession().catch(function(response){
                    console.log("Refresh session failed for " + facility.config().name + ": " + response.message?response.message:"null response received");
                });
            });
        }, 1000 * 60 * 5);
        $scope.$on('$destroy', function(){ clearInterval(refreshSessionInterval); });


        $rootScope.requestCounter = 0;

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

        this.maintenanceMode = tc.config().maintenanceMode;
        this.serviceStatus = tc.config().serviceStatus;

        tc.getConfVar('serviceStatus').then(function(serviceStatus){
            that.serviceStatus = serviceStatus;
        });


        if(tc.adminFacilities().length == 0){
            $interval(function(){
                tc.getConfVar('maintenanceMode').then(function(maintenanceMode){
                    if(!that.maintenanceMode.show && maintenanceMode.show){
                        var promises = [];

                        _.each(tc.userFacilities(), function(facility){
                            promises.push(facility.icat().logout());
                        });

                        $q.all(promises).then(function(){
                            var internalLoginCount = 0;

                            _.each(tc.facilities(), function(facility){
                                _.each(facility.config().authenticationTypes, function(authenticationType){
                                    if(!authenticationType.external){
                                        internalLoginCount++;
                                    }
                                });
                            });

                            if(internalLoginCount == 0){
                                alert("You've been logged out as this site has gone down for maintenance.");
                            }

                            window.location.reload();
                        });
                    }
                });
            }, 60 * 1000);
        }

    });

})();

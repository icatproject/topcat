(function() {
    'use strict';

    angular.
        module('angularApp').
        controller('DownloadCartController', DownloadCartController).
        controller('DownloadCartModalController', DownloadCartModalController).
        directive('downloadCart', downloadCart);

    DownloadCartController.$inject = ['$rootScope', '$uibModal', 'Cart', 'inform'];
    DownloadCartModalController.$inject = ['$modalInstance', 'Cart', 'SMARTCLIENTPING', 'idsInfos', '$translate'];
    downloadCart.$inject = [];

    function DownloadCartController ($rootScope, $uibModal, Cart, inform) {
        var dc = this;
        dc.cartItems = Cart._cart.items;

        dc.openModal = function() {
            $uibModal.open({
                templateUrl : 'views/download-cart-modal.directive.html',
                controller : 'DownloadCartModalController as dcm',
                resolve: {
                    idsInfos : ['$q', 'Config', 'APP_CONFIG', 'IdsManager', '$sessionStorage', function($q, Config, APP_CONFIG, IdsManager, $sessionStorage) {
                        var sessions = $sessionStorage.sessions;
                        var facilities = [];

                        _.each(sessions, function(session, key) {
                            var facility = Config.getFacilityByName(APP_CONFIG, key);
                            facilities.push(facility);
                        });

                        return IdsManager.isTwoLevelForFacilities(facilities);
                    }],
                    SMARTCLIENTPING : ['SmartClientManager', function(SmartClientManager) {
                        return SmartClientManager.ping();
                    }]
                },
                size : 'lg'
            }).opened.catch(function (error) {
                inform.add(error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });
        };
    }


    function DownloadCartModalController($modalInstance, Cart, SMARTCLIENTPING, idsInfos, $translate) {
        var vm = this;
        var facilityCart = Cart.getFacilitiesCart();

        vm.downloads = [];

        _.each(facilityCart, function(cart) {
            cart.transportOptions = cart.getDownloadTransportType();

            //translate transport name
            _.each(cart.transportOptions, function(options) {
                options.transportName = options.displayName;

                if (typeof options.translateDisplayName !== 'undefined') {
                    options.transportName = $translate.instant(options.translateDisplayName);
                }
            });

            //check if smartclient is online and if so add the option to the transport type dropdown
            if (typeof SMARTCLIENTPING !== 'undefined' && SMARTCLIENTPING.ping === 'online') {
                var httpTransport = _.find(cart.transportOptions, {type: 'https'});

                if (typeof httpTransport !== 'undefined') {
                    var translateName = $translate.instant('DOWNLOAD.TRANSPORT.SMARTCLIENT.NAME');

                    var smartClientTransport = {
                        translateDisplayName: 'DOWNLOAD.TRANSPORT.SMARTCLIENT.NAME',
                        transportName : translateName,
                        type : 'smartclient',
                        url: httpTransport.url,
                    };

                    var smartClientTransportExists = _.find(cart.transportOptions, {type: 'smartclient'});

                    if (typeof smartClientTransportExists === 'undefined') {
                        cart.transportOptions.push(smartClientTransport);
                    }
                }
            }

            //set the default transport dropdown
            if (cart.transportOptions.length === 1) {
                cart.transportType = cart.transportOptions[0];
            } else {
                _.each(cart.transportOptions, function(option) {
                    if (option.default === true) {
                        cart.transportType = option;
                    }
                });
            }

            vm.downloads.push(cart);
        });

        vm.hasArchive = function() {
            var isTwoLevel = false;

            _.each(idsInfos, function(data){
                if (typeof data.data !== 'undefined' && data.data === 'true') {
                    isTwoLevel = true;
                }
            });

            return isTwoLevel;
        };


        vm.ok = function() {
            $modalInstance.close();

            if (typeof vm.email !== 'undefined' && vm.email.trim() !== '') {
                _.each(vm.downloads, function(download) {
                    download.email = vm.email;
                });
            }
            //submit the cart for download

            Cart.submit(vm.downloads);
        };

        vm.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    }

    function downloadCart() {
        return {
            restrict: 'E',
            scope: {
                items: '@'
            },
            templateUrl: 'views/download-cart.directive.html',
            controller: 'DownloadCartController',
            controllerAs: 'dc'
        };
    }

})();
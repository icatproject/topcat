(function() {
    'use strict';

    angular.
        module('angularApp').
        controller('DownloadCartController', DownloadCartController).
        controller('DownloadCartModalController', DownloadCartModalController).
        directive('downloadCart', downloadCart);

    DownloadCartController.$inject = ['$modal', 'Cart'];
    DownloadCartModalController.$inject = ['$modalInstance', 'Cart', 'SMARTCLIENTPING'];
    downloadCart.$inject = [];

    function DownloadCartController ($modal, Cart) {
        var dc = this;
        dc.cartItems = Cart._cart.items;

        dc.openModal = function() {
            var modalInstance = $modal.open({
                templateUrl : 'views/download-cart-modal.directive.html',
                controller : 'DownloadCartModalController as dcm',
                size : 'lg'
            });

            modalInstance.result.then(function() {

            }, function() {

            });
        };
    }


    function DownloadCartModalController($modalInstance, Cart, SMARTCLIENTPING) {
        var vm = this;
        var facilityCart = Cart.getFacilitiesCart();

        vm.downloads = [];

        _.each(facilityCart, function(cart) {
            cart.transportOptions = cart.getDownloadTransportType();

            //check if smartclient is online and of so add the option to the transport type dropdown
            if (typeof SMARTCLIENTPING !== 'undefined' && SMARTCLIENTPING.ping === 'online') {
                var httpTransport = _.find(cart.transportOptions, {type: 'https'});

                if (typeof httpTransport !== 'undefined') {
                    var smartClientTransport = {
                        displayName : 'Smartclient',
                        type : 'smartclient',
                        url: httpTransport.url,
                    };

                    var smartClientTransportExists = _.find(cart.transportOptions, smartClientTransport);

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


        vm.ok = function() {
            $modalInstance.close();

            if (typeof vm.email !== 'undefined' && vm.email.trim() !== '') {
                _.each(vm.downloads, function(download) {
                    download.email = vm.email;
                });
            }
            ///submit the cart for download
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
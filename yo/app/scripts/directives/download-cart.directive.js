(function() {
    'use strict';

    angular.
        module('angularApp').
        controller('DownloadCartController', DownloadCartController).
        controller('DownloadCartModalController', DownloadCartModalController).
        directive('downloadCart', downloadCart);

    DownloadCartController.$inject = ['$modal', '$log'];
    DownloadCartModalController.$inject = ['$modalInstance', 'Cart', 'SMARTCLIENTPING', '$log'];
    downloadCart.$inject = [];

    function DownloadCartController ($modal, $log) { //jshint ignore: line
        var dc = this;

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


    function DownloadCartModalController($modalInstance, Cart, SMARTCLIENTPING, $log) { //jshint ignore: line
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
                } else {
                    $log.debug('No ids found. Unable to add Smartclient as an option');
                }


            }

            $log.debug('cart.transportOptions', cart.transportOptions);

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

            $log.debug(JSON.stringify(vm.downloads, null, 2));
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
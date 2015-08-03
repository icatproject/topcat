(function() {
    'use strict';

    angular.
        module('angularApp').
        controller('DownloadCartController', DownloadCartController).
        controller('DownloadCartModalController', DownloadCartModalController).
        directive('downloadCart', downloadCart);


    DownloadCartController.$inject = ['$modal', '$log'];
    DownloadCartModalController.$inject = ['$modalInstance', 'Cart', '$log'];
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


    function DownloadCartModalController($modalInstance, Cart, $log) { //jshint ignore: line
        var vm = this;
        var facilityCart = Cart.getFacilitiesCart();

        vm.downloads = [];

        _.each(facilityCart, function(cart) {
            cart.transportOptions = cart.getDownloadTransportType();

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

            Cart.submit(vm.downloads);

            //window.alert(JSON.stringify(vm.downloads, null, 2));
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
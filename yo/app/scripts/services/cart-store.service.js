(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('CartStore', CartStore);

    CartStore.$inject =['$localStorage', '$log'];

    function CartStore($localStorage, $log) { //jshint ignore: line
        return {
            get: function () {
                console.log('get $localStorage.cart', $localStorage.cart);
                return $localStorage.cart;
            },

            set: function (cart) {
                /*if (typeof $localStorage.cart === 'undefined') {
                    $localStorage.$default({
                        cart : {}
                    });
                }*/
                console.log('set $localStorage.cart', cart);
                $localStorage.cart = cart;
            }
        };
    }
})();
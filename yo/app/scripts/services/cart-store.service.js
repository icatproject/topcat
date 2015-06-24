(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('CartStore', CartStore);

    CartStore.$inject =['$localStorage', '$log'];

    function CartStore($localStorage, $log) { //jshint ignore: line
        return {
            get: function () {
                return $localStorage.cart;
            },

            set: function (cart) {
                $localStorage.cart = cart;
            },

            getUserStore: function (facility, userName) {
                var store = [];

                if (typeof $localStorage.cart.items !== 'undefined') {
                    _.each($localStorage.cart.items, function(item) {
                        if (item.facilityName === facility.facilityName && item.userName === userName) {
                            store.push(item);
                        }
                    });
                }

                return store;
            },

            setUserStore: function (facility, userName, cart) {
                $localStorage.cart = cart;
            }
        };
    }
})();
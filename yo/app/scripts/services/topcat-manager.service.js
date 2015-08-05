(function() {
    'use strict';

    angular.
        module('angularApp').factory('TopcatManager', TopcatManager);

    TopcatManager.$inject = ['$http', '$q', 'TopcatService', '$log'];

    function TopcatManager($http, $q, TopcatService, $log) { //jshint ignore: line
        var manager = {};

        function MyException(message) {
          this.name = name;
          this.message = message;
        }
        MyException.prototype = new Error();
        MyException.prototype.constructor = MyException;

        /**
         * Get the size from the ids
         * @param  {Object} sessions session object containing logged in sessions
         * @param  {Object} facility the facility object
         * @return {Object}          a promise containing the list of instruments
         */
        manager.submitCart = function(facility, cart) {
            var def = $q.defer();

            TopcatService.submitCart(facility, cart).then(function(data) {
                def.resolve(data.data);
            }, function(){
                def.reject('Failed to submit cart');
                throw new MyException('Failed to submit cart');
            });

            return def.promise;
        };


        manager.getCartItems = function(facility, userName) {
            var def = $q.defer();

            TopcatService.getCart(facility, userName).then(function(data) {
                var result = [];

                if (! _.isEmpty(data.data)) {
                    _.each(data.data.cartItems, function(item) {
                        item.facilityName = facility.facilityName;
                        item.userName = userName;
                    });

                    result = data.data.cartItems;
                }

                def.resolve(result);
            }, function(){
                def.reject('Failed to get user cart');
                throw new MyException('Failed to get user cart');
            });

            return def.promise;
        };

        manager.saveCart = function(facility, userName, cart) {
            var def = $q.defer();

            TopcatService.saveCart(facility, userName, cart).then(function(data) {
                def.resolve(data.data);
            }, function(){
                def.reject('Failed to get user cart');
                throw new MyException('Failed to get user cart');
            });

            return def.promise;
        };

        manager.getMyDownloads = function(facility, userName) {
            var def = $q.defer();

            TopcatService.getMyDownloads(facility, userName).then(function(data) {
                def.resolve(data.data);
            }, function(){
                def.reject('Failed to get user downloads');
                throw new MyException('Failed to get user cart');
            });

            return def.promise;
        };

        return manager;
    }
})();
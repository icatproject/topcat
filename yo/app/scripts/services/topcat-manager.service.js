(function() {
    'use strict';

    angular.
        module('angularApp').factory('TopcatManager', TopcatManager);

    TopcatManager.$inject = ['$http', '$q', 'TopcatService', '$log'];

    function TopcatManager($http, $q, TopcatService, $log) { //jshint ignore: line
        var manager = {};

        function getErrorMessage(error) {
            var errorMessage = '';

            if (error.status === 0) {
                errorMessage = 'Unable to contact server';
            } else {
                if (error.data !== null) {
                    if (typeof error.data.message !== 'undefined') {
                        errorMessage = error.data.message;
                    } else {
                        errorMessage = 'Unable to retrieve data';
                    }
                } else {
                    errorMessage = 'Unknown error';
                }
            }

            return errorMessage;
        }

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
            }, function(error){
                def.reject('Failed to submit cart: ' + getErrorMessage(error));
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

                $log.debug('TopcatManager getCartItems', result);

                def.resolve(result);
            }, function(error){
                def.reject('Failed to get user cart: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        manager.saveCart = function(facility, userName, cart) {
            var def = $q.defer();

            TopcatService.saveCart(facility, userName, cart).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to get user cart: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        manager.getMyDownloads = function(facility, userName) {
            var def = $q.defer();

            TopcatService.getMyDownloads(facility, userName).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to get user downloads: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        manager.removeDownloadByPreparedId = function(facility, userName, preparedId) {
            var def = $q.defer();

            TopcatService.removeDownloadByPreparedId(facility, userName, preparedId).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to remove download: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        return manager;
    }
})();
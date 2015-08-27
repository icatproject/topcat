(function() {
    'use strict';

    angular.
        module('angularApp').service('TopcatManager', TopcatManager);

    TopcatManager.$inject = ['$http', '$q', 'TopcatService', '$log'];

    function TopcatManager($http, $q, TopcatService, $log) { //jshint ignore: line
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
        this.submitCart = function(facility, cart) {
            var def = $q.defer();

            TopcatService.submitCart(facility, cart).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to submit cart: ' + getErrorMessage(error));
            });

            return def.promise;
        };


        this.getCartItems = function(facility, userName) {
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

        this.saveCart = function(facility, userName, cart) {
            var def = $q.defer();

            TopcatService.saveCart(facility, userName, cart).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to get user cart: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        this.getMyDownloads = function(facility, userName) {
            var def = $q.defer();

            TopcatService.getMyDownloads(facility, userName).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to get user downloads: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        this.getMyRestoringSmartClientDownloads = function(facility, userName) {
            var def = $q.defer();

            TopcatService.getMyRestoringSmartClientDownloads(facility, userName).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to get downloads using smartclient: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        this.removeDownloadByPreparedId = function(facility, userName, preparedId) {
            var def = $q.defer();

            TopcatService.removeDownloadByPreparedId(facility, userName, preparedId).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to remove download: ' + getErrorMessage(error));
            });

            return def.promise;
        };

        this.completeDownloadByPreparedId = function(facility, userName, preparedId) {
            var def = $q.defer();

            TopcatService.completeDownloadByPreparedId(facility, userName, preparedId).then(function(data) {
                def.resolve(data.data);
            }, function(error){
                def.reject('Failed to marked download as complete: ' + getErrorMessage(error));
            });

            return def.promise;
        };
    }
})();
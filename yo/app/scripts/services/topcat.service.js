(function() {
    'use strict';

    angular.
        module('angularApp').factory('TopcatService', TopcatService);

    TopcatService.$inject = ['$http', '$q', 'APP_CONFIG', 'Config', '$sessionStorage', '$log'];

    /*jshint -W098 */
    function TopcatService($http, $q, APP_CONFIG, Config, $sessionStorage, $log) { //jshint ignore:  line
        //private var and methods
        var data = {};

        var TOPCAT_API_PATH = Config.getSiteConfig(APP_CONFIG).topcatApiPath;

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        data.submitCart = function(facility, cart) {
            var url = TOPCAT_API_PATH + '/cart/submit';
            var params = {
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    }
                };

            return $http.post(url, cart, params);
        };


        data.getCart = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/cart/facility/' + facility.facilityName + '/user/' + encodeURIComponent(userName);

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };

        data.saveCart = function(facility, userName, cart) {
            var url = TOPCAT_API_PATH + '/cart';

            var params = {
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.post(url, cart, params);
        };

        data.removeCart = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/cart/facility/' + facility.facilityName + '/user/' + encodeURIComponent(userName);

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions['facility.facilityName'].sessionId,
                    icatUrl: facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.delete(url, params);
        };


        data.getMyDownloads = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/downloads/facility/' + facility.facilityName + '/user/' + encodeURIComponent(userName);

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };

        return data;
    }
})();

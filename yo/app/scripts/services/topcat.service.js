(function() {
    'use strict';

    angular.
        module('angularApp').service('TopcatService', TopcatService);

    TopcatService.$inject = ['$http', '$q', 'APP_CONFIG', 'Config', '$sessionStorage', '$log'];

    /*jshint -W098 */
    function TopcatService($http, $q, APP_CONFIG, Config, $sessionStorage, $log) { //jshint ignore: line
        var TOPCAT_API_PATH = Config.getSiteConfig(APP_CONFIG).topcatApiPath;

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        this.submitCart = function(facility, cart) {
            var url = TOPCAT_API_PATH + '/cart/submit';
            var params = {
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    }
                };

            return $http.post(url, cart, params);
        };


        this.getCart = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/cart/facility/' + facility.facilityName;

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };

        this.saveCart = function(facility, userName, cart) {
            var url = TOPCAT_API_PATH + '/cart';

            var params = {
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.post(url, cart, params);
        };

        this.removeCart = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/cart/facility/' + facility.facilityName;

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.delete(url, params);
        };


        this.getMyDownloads = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/downloads/facility/' + facility.facilityName;

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };


        this.getMyRestoringSmartClientDownloads = function(facility, userName) {
            var url = TOPCAT_API_PATH + '/downloads/facility/' + facility.facilityName;

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    transport: 'smartclient',
                    status: 'RESTORING',
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };

        this.removeDownloadByPreparedId = function(facility, userName, preparedId) {
            var url = TOPCAT_API_PATH + '/downloads/' + preparedId;

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.delete(url, params);
        };

        this.completeDownloadByPreparedId = function(facility, userName, preparedId) {
            var url = TOPCAT_API_PATH + '/downloads/' + preparedId + '/complete';

            var params = {
                params : {
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    userName: userName
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.put(url, {}, params);
        };
    }
})();

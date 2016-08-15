(function() {
    'use strict';

    angular.
        module('topcat').service('SmartClientService', SmartClientService);

    SmartClientService.$inject = ['$http', '$q', 'APP_CONFIG', 'APP_CONSTANT'];

    /*jshint -W098 */
    function SmartClientService($http, $q, APP_CONFIG, APP_CONSTANT) {

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        this.ping = function() {
            var url = APP_CONSTANT.smartClientUrl + '/ping';

            var params = {
                info : {
                    'smartClient' : true
                },
                headers: {
                    'Accept' : 'application/json'
                },
                timeout: 200
            };

            return $http.get(url, params);
        };


        this.getStatus = function() {
            var url = APP_CONSTANT.smartClientUrl + '/status';
            var params = {
                info : {
                    'smartClient' : true
                }
            };

            return $http.get(url, params);
        };

        this.login = function(mySessionId, facility) {
            var url = APP_CONSTANT.smartClientUrl + '/login';
            var params = {
                'headers' :{
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title,
                    'smartClient' : true
                },
                'transformRequest': function(obj) {
                    var str = [];
                    for(var p in obj) {
                        if (obj.hasOwnProperty(p)) {
                            str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]));
                        }
                    }

                    return str.join('&');
                }
            };

            var credentialObj = {
                sessionId: mySessionId,
                idsUrl: facility.idsUrl
            };

            var data = { json : JSON.stringify(credentialObj) };

            return $http.post(url, data, params);
        };


        this.getData = function(facility, preparedId) {
            var url = APP_CONSTANT.smartClientUrl + '/getData';
            var params = {
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title,
                    'smartClient' : true,
                },
                'transformRequest': function(obj) {
                    var str = [];
                    for(var p in obj) {
                        if (obj.hasOwnProperty(p)) {
                            str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]));
                        }
                    }

                    return str.join('&');
                }
            };

            var data = {
                json :  JSON.stringify({
                    idsUrl: facility.idsUrl,
                    preparedIds: [
                        preparedId
                    ]
                })
            };

            return $http.post(url, data, params);
        };


        this.isReady = function(facility, preparedId) {
            var url = APP_CONSTANT.smartClientUrl + '/isReady';
            var params = {
                params : {
                    json : {
                        idsUrl: facility.idsUrl,
                        preparedIds: [preparedId]
                    }
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title,
                    'smartClient' : true
                }
            };

            return $http.post(url, params);
        };
    }
})();

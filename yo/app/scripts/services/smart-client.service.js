(function() {
    'use strict';

    angular.
        module('angularApp').factory('SmartClientService', SmartClientService);

    SmartClientService.$inject = ['$http', '$q', 'APP_CONFIG', 'APP_CONSTANT', 'Config', '$log'];

    /*jshint -W098 */
    function SmartClientService($http, $q, APP_CONFIG, APP_CONSTANT, Config, $log) { //jshint ignore:  line
        //private var and methods
        var data = {};

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        data.ping = function() {
            $log.debug('SmartClientService ping called');
            var url = APP_CONSTANT.smartClientUrl + '/ping';

            var params = {
                info : {
                    'smartClient' : true
                }
            };

            return $http.get(url, params);
        };


        data.getStatus = function() {
            var url = APP_CONSTANT.smartClientUrl + '/status';
            var params = {
                info : {
                    'smartClient' : true
                }
            };

            return $http.get(url, params);
        };

        data.login = function(mySessionId, facility) {
            var url = APP_CONSTANT.smartClientUrl + '/login';
            var params = {
                'headers' :{
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                info : {
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


        data.getData = function(facility, preparedId) {
            var url = APP_CONSTANT.smartClientUrl + '/getData';
            var params = {
                info : {
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


        data.isReady = function(facility, preparedId) {
            var url = APP_CONSTANT.smartClientUrl + '/isReady';
            var params = {
                params : {
                    json : {
                        idsUrl: facility.idsUrl,
                        preparedIds: [preparedId]
                    }
                },
                info : {
                    'smartClient' : true
                }
            };

            return $http.post(url, params);
        };

        return data;
    }
})();

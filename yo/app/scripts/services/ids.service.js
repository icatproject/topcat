(function() {
    'use strict';

    angular.
        module('angularApp').factory('IdsService', IdsService);

    IdsService.$inject = ['$http', '$q', 'APP_CONFIG', 'Config', '$log'];

    /*jshint -W098 */
    function IdsService($http, $q, APP_CONFIG, Config, $log) { //jshint ignore:  line
        //private var and methods
        var data = {};

        var ICATDATAPROXYURL = Config.getSiteConfig(APP_CONFIG).icatDataProxyHost;

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        data.getSize = function(mySessionId, facility, options) {
            var url = ICATDATAPROXYURL + '/ids/getSize';
            var params = {
                    params : {
                        sessionId : encodeURIComponent(mySessionId),
                        server : encodeURIComponent(facility.idsUrl)
                    },
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    },
                    cache: true
                };

            params = _.merge(params, {
                params : options
            });

            return $http.get(url, params);
        };


        data.getStatus = function(mySessionId, facility, options) {
            var url = ICATDATAPROXYURL + '/ids/getStatus';
            var params = {
                    params : {
                        sessionId : encodeURIComponent(mySessionId),
                        server : encodeURIComponent(facility.idsUrl)
                    },
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    },
                    cache: true
                };

            params = _.merge(params, {
                params : options
            });

            return $http.get(url, params);
        };

        return data;
    }
})();

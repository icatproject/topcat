(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('ICATRequestInterceptor', ICATRequestInterceptor);

    ICATRequestInterceptor.$inject = ['$injector', 'APP_CONFIG', 'Config'];

    function ICATRequestInterceptor($injector, APP_CONFIG, Config){
        return {
            request: function(config) {
                if (_.has(config, 'headers') && _.has(config, 'params')) {
                    if (_.has(config.info, 'skipRefreshSession') && config.info.skipRefreshSession === true) {
                        return config;
                    }

                    if (_.has(config.info, 'facilityKeyName') && _.has(config.params, 'sessionId')) {
                        var DataManager = $injector.get('DataManager');
                        var facility = Config.getFacilityByName(APP_CONFIG, config.info.facilityKeyName);

                        //refresh the session
                        DataManager.refreshSession(config.params.sessionId, facility);
                    }
                }

                return config;
            }
        };
    }
})();

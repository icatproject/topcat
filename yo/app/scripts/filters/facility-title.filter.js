(function() {
    'use strict';

    /**
     *
     */
    angular
        .module('angularApp')
        .filter('facilityTitle', ['APP_CONFIG', 'Config', function(APP_CONFIG, Config) {
            return function(value) {
                return Config.getFacilityTitleByFacilityKey(APP_CONFIG, value);
            };
        }])
        .filter('entityTypeTitle', ['$translate', function($translate) {
            return function(value) {
                return $translate.instant('ENTITIES.' + value.toUpperCase() + '.NAME');
            };
        }]);
})();

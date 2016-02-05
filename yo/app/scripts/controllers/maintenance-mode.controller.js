(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('MaintenanceModeController', ['APP_CONFIG', function(APP_CONFIG){
        var maintenanceMode = APP_CONFIG.site.maintenanceMode;
        if(maintenanceMode){
            this.message = maintenanceMode.message;
        }
    }]);

})();
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('serviceStatus', function(){
        return {
            restrict: 'E',
            templateUrl: 'views/service-status.directive.html',
            controller: 'ServiceStatusController',
            controllerAs: 'serviceStatusController'
        };
    });

    app.controller('ServiceStatusController', ['APP_CONFIG', function(APP_CONFIG){
        var serviceStatus = APP_CONFIG.site.serviceStatus;
        this.show = serviceStatus.show;
        this.message = serviceStatus.message;

        this.close = function(){
            this.show = false;
        };

    }]);

})();
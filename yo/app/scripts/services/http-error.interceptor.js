(function() {
    'use strict';

    var app = angular.module('topcat');

    app.factory('HttpErrorInterceptor', function($q, $rootScope){
        return {
            responseError: function(rejection) {
                if(rejection.status >= 400 && rejection.status < 500){
                    $rootScope.$broadcast('http:error');
                }
                return $q.reject(rejection);
            }
        };
    });

})();

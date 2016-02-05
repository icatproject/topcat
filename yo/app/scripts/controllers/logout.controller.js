(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('LogoutController', function($q, $state, tc, SmartClientManager){
        
        var facilityName = $state.params.facilityName;

        SmartClientManager.ping().then(function(result){
            var smartClientOnline = result.ping == 'online';
            var promises = [];

            if(facilityName){
                promises.push(tc.icat(facilityName).logout(smartClientOnline));
            } else {
                _.each(tc.facilities(), function(facility){
                    promises.push(facility.icat().logout(smartClientOnline));
                });
            }

            $q.all(promises).then(function(){
                $state.go('login');
            });

        });

    });

})();

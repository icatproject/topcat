(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('LogoutController', function($q, $state, tc){
        var facilityName = $state.params.facilityName;
        
        var promises = [];

        if(facilityName){
            promises.push(tc.smartclient(facilityName).ping().then(function(smartclientIsAvailable){
                return tc.icat(facilityName).logout(smartclientIsAvailable);
            }));
        } else {
            _.each(tc.facilities(), function(facility){
                promises.push(facility.smartclient().ping().then(function(smartclientIsAvailable){
                    return facility.icat().logout(smartclientIsAvailable);
                }));
            });
        }

        $q.all(promises).then(function(){
            $state.go('login');
        });
    });

})();

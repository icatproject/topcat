(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('LogoutController', function($q, $state, tc){
        var facilityName = $state.params.facilityName;
        var promises = [];

        if(facilityName){
            promises.push(tc.icat(facilityName).logout());
        } else {
            _.each(tc.facilities(), function(facility){
                promises.push(facility.icat().logout());
            });
        }

        $q.all(promises).then(function(){
            $state.go('login');
        });
    });

})();

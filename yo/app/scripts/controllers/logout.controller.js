(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('LogoutController', function($q, $state, tc){
        var facilityName = $state.params.facilityName;
        
        var promises = [];

        if(facilityName){
            promises.push(tc.smartclient(facilityName).ping().then(function(smartclientIsAvailable){
                if(smartclientIsAvailable){
                    return tc.icat(facilityName).logout(true);
                } else {
                    return tc.user(facilityName).downloads("download.isDeleted = false and download.status = org.icatproject.topcat.domain.DownloadStatus.PREPARING").then(function(downloads){
                        return tc.icat(facilityName).logout(downloads.length > 0);
                    });
                }
            }));
        } else {
            _.each(tc.facilities(), function(facility){
                promises.push(facility.smartclient().ping().then(function(smartclientIsAvailable){
                    if(smartclientIsAvailable){
                        return facility.icat().logout(true);
                    } else {
                        return facility.user().downloads("download.isDeleted = false and download.status = org.icatproject.topcat.domain.DownloadStatus.PREPARING").then(function(downloads){
                            return tc.icat().logout(downloads.length > 0);
                        });
                    }
                }));
            });
        }

        $q.all(promises).then(function(){
            $state.go('login');
        });
    });

})();

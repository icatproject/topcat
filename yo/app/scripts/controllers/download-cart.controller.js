


(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DownloadCartController', function($q, $uibModalInstance, $scope, $rootScope, $uibModalStack, tc){
        var that = this;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });
        this.hasArchive = false;
        this.email = "";
        this.downloads = [];
        this.facilityCount = tc.facilities().length;
        this.connectionSpeed = "3932160";
        this.isSubmitting = false;

        this.isStaged = function(){
            var out = false;
            _.each(this.downloads, function(download){
                if(download.transportType != 'https' && download.transportType != 'smartclient'){
                    out = true;
                    return false;
                }
            });
            return out;
        };

        _.each(tc.userFacilities(), function(facility){
            facility.user().cart(timeout).then(function(cart){
                
                if(cart.cartItems.length > 0){
                    var transportTypes = [];

                    var transportType = facility.config().downloadTransportTypes[0].type;
                    _.each(facility.config().downloadTransportTypes, function(current){
                        transportTypes.push(current.type);
                    });
                    
                    var date = new Date();
                    var year = date.getFullYear();
                    var month = date.getMonth() + 1;
                    var day = date.getDate();
                    if(day < 10) day = '0' + day;
                    var hour = date.getHours();
                    if(hour < 10) hour = '0' + hour;
                    var minute = date.getMinutes();
                    if(minute < 10) minute = '0' + minute;
                    var second = date.getSeconds();
                    if(second < 10) second = '0' + second;
                    var fileName = facility.config().name + "_" + year + "-" + month + "-" + day + "_" + hour + "-" + minute + "-" + second;

                    var download = {
                        fileName: fileName,
                        facility: facility,
                        facilityName: facility.config().name,
                        transportTypes: transportTypes,
                        transportType: transportType
                    };

                    cart.getSize(timeout.promise).then(function(size){
                        download.size = size;
                        download.estimatedTime = Math.ceil(size);
                    });

                    that.downloads.push(download);
                }
                
            });
        });

        this.ok = function() {
            _.each(this.downloads, function(download){
                if(!download.fileName){
                    throw "A download name must be provided.";
                }
            });

            this.isSubmitting = true;

            var promises = [];
            _.each(this.downloads, function(download){
                promises.push(download.facility.user().submitCart(download.fileName, download.transportType, that.email, timeout.promise));
            });
            $q.all(promises).then(function(){
                $uibModalStack.dismissAll();
                $rootScope.$broadcast('cart:submit');
            });
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
    });

})();

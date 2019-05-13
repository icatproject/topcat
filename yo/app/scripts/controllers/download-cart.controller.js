


(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DownloadCartController', function($q, $uibModalInstance, $scope, $rootScope, $uibModalStack, tc){
        var that = this;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });
        this.email = "";
        this.downloads = [];
        this.facilityCount = tc.facilities().length;
        this.connectionSpeed = "3932160";
        this.isSubmitting = false;

        this.isStaged = function(){
            var out = false;
            _.each(this.downloads, function(download){
                if((!download.transportType.type.match(/https|http/)) && download.transportType.type != 'smartclient'){
                    out = true;
                    return false;
                }
            });
            return out;
        };

        this.isTwoLevel = function(){
            return _.select(this.downloads, function(download){
                return download.isTwoLevel;
            }).length > 0;
        };

        _.each(tc.userFacilities(), function(facility){
            facility.user().cart(timeout).then(function(cart){
                
                if(cart.cartItems.length > 0){
                	// Each facility cart has its own list of download transport types;
                	// transportTypes is used to populate the download choice list
                    var transportTypes = [];

                    // Set up a transportType object for each transportType in the configuration
                    _.each(facility.config().downloadTransportTypes, function(current){
                        transportTypes.push({
                        	type: current.type,
                        	displayName: current.displayName?current.displayName:current.type,
                        	description: current.description
                        });
                    });
                    var transportType = transportTypes[0];
                    
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
                        transportType: transportType,
                        updateIsTwoLevel: function(){
                            this.facility.downloadTransportTypeIds(this.transportType.type).isTwoLevel(timeout.promise).then(function(isTwoLevel){
                                download.isTwoLevel = isTwoLevel;
                            });
                        }
                    };

                    download.updateIsTwoLevel();

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
                // Check the status of the download type
                download.facility.user().getDownloadTypeStatus(download.transportType.type,timeout).then(function(status){
                	if(status.disabled){
                		throw downloadTypeStatus.message;
                	}
                });
            });

            this.isSubmitting = true;

            var promises = [];
            _.each(this.downloads, function(download){
                promises.push(download.facility.user().submitCart(download.fileName, download.transportType.type, that.email, timeout.promise).then(function(response){
                    return download.facility.user().downloads(["where download.id = ?",response.downloadId]).then(function(downloads){
                        var download = downloads[0];
                        if(download.transport.match(/https|http/) && download.status == 'COMPLETE'){
                            // Determine the idsUrl for the download from topcat config, using its facility and transport (type)
                            var facility = tc.facility(download.facilityName);
                            var transportType = _.select(facility.config().downloadTransportTypes, function(downloadTransportType){
                                return downloadTransportType.type == download.transport;
                            });
                            var idsUrl = transportType && transportType[0] ? transportType[0].idsUrl : undefined;
                            var url = idsUrl + '/ids/getData?preparedId=' + download.preparedId + '&outname=' + download.fileName;
                            var iframe = $('<iframe>').attr('src', url).css({
                                position: 'absolute',
                                left: '-1000000px',
                                height: '1px',
                                width: '1px'
                            });

                            $('body').append(iframe);
                        }
                    });
                }, function(response){
                    console.log('submitCart failed for file',download.fileName,'from facility',download.facility.config().name);
                }));
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

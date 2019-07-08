


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
                    }, function(response){
                    	// getSize failed (will be logged from elsewhere); use -1 for "unknown"
                    	download.size = -1;
                    	download.estimatedTime = -1;
                    });

                    that.downloads.push(download);
                }
                
            });
        });

        this.ok = function() {
        	// Brian Ritchie, 2019-05-15:
        	// Checking whether each download's type has been disabled requires promises.
        	// This complicates the coding unpleasantly: we can't just throw the first disabled message we find - it happens too late to halt this function.
        	// We need to move the rest of the code within $q.all(promiseChecks).then(...) - and replace all refs to "this" with "that"!
        	//
        	var promiseChecks = [];
        	var disabledMessages = [];
            _.each(this.downloads, function(download){
                if(!download.fileName){
                    throw "A download name must be provided.";
                }
                // Check the status of the download type
                promiseChecks.push(download.facility.user().getDownloadTypeStatus(download.transportType.type,timeout).then(function(status){
                	if(status.disabled){
                		if( status.message ){
                			disabledMessages.push( status.message );
                		} else {
                			disabledMessages.push( "Download type '" + download.transportType.displayName + "' is disabled at present.");
                		}
                	}
                }, function(response){
                	// getDownloadTypeStatus failed in some way
                	var msg = "(no message)";
                	if( response && response.message ) msg = response.message;
                	console.log("getDownloadTypeStatus('" + download.transportType.type + "') failed: " + msg);
                }));
            });
            
            $q.all(promiseChecks).then(function(){
            	if( disabledMessages.length > 0 ){
                	// In the general case, we ought to collect the possibly multiple disabled messages and display them in a new dialog.
                	// However, this can only happen when Topcat has been configured for multiple facilities (and all have disabled download types,
                	// and the user chooses more than one disabled type). No current production deployments use multiple facilities.
                	// I have chosen just to throw a join of the messages; but note that "\n" probably does not add a newline, so
            		// the results in the general case are likely to be ugly.
            		throw disabledMessages.join(", \n");
            	}

	            that.isSubmitting = true;
	
	            var promises = [];
	            _.each(that.downloads, function(download){
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
            }, function(response){
            	// $q.all failed in some way
            	var msg = "(no message)";
            	if( response && response.message ) msg = response.message;
            	console.log("$q.all(promiseChecks) failed: " + msg);
            });
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
    });

})();

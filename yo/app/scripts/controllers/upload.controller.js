
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('UploadController', function($state, $uibModalInstance, $rootScope, tc, inform){
        var that = this;
        var facility = tc.facility($state.params.facilityName);
        var icat = facility.icat();
        var ids = facility.ids();
        var investigationId = parseInt($state.params.investigationId);
        var datasetTypeId = facility.config().idsUploadDatasetTypeId;

        this.isUploading = false;
        this.name = "";
        this.files = [];
        this.datasetId = parseInt($state.params.datasetId);
        this.maxTotalFileSize = facility.config().idsUploadMaxTotalFileSize;

        this.totalFileSize = function(){
            var out = 0;
            _.each(this.files, function(file){
                out += file.size;
            });
            return out;
        };

        this.upload = function(){
            var fileNames = {};
            var duplicateFileName = null;
            _.each(this.files, function(file){
                if(fileNames[file.name]){
                    duplicateFileName = file.name;
                    return false;
                } else {
                    fileNames[file.name] = true;
                }
            });

            if(duplicateFileName){
                inform.add("Duplicate filename detected: " + duplicateFileName, {
                    'ttl': 3000,
                    'type': 'danger'
                });
                return;
            }

            this.isUploading = true;

        	if(this.datasetId){
        		ids.upload(this.datasetId, this.files).then(function(datafileIds){
        			tc.refresh();
                    $rootScope.$broadcast('upload:complete', datafileIds);
                    $uibModalInstance.dismiss('cancel');
        		}, handleError);
        	} else {
                icat.write([
                    {
                        Dataset: {
                            investigation: {id: investigationId},
                            type: {id: datasetTypeId},
                            name: that.name
                        }
                    }
                ]).then(function(datasetIds){
                    ids.upload(datasetIds[0], that.files).then(function(datafileIds){
                        tc.refresh();
                        $rootScope.$broadcast('upload:complete', datafileIds);
                        $uibModalInstance.dismiss('cancel');
                    }, handleError);
                }, handleError);
            }
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        function handleError(response){
            that.isUploading = false;

        	var message = "(no response message received)";
        	if( response && response.message ) message = response.message;
            inform.add("Upload failed: " + message, {
                'ttl': 3000,
                'type': 'danger'
            });
        }

    });
})();

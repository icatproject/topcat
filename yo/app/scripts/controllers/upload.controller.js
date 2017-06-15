
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

        this.name = "";
        this.files = [];
        this.datasetId = parseInt($state.params.datasetId);

        this.upload = function(){
            if(this.files.length > 0){
            	if(this.datasetId){
            		ids.upload(this.datasetId, this.files).then(function(datafileIds){
            			tc.refresh();
                        $rootScope.$broadcast('upload:complete', datafileIds);
                        $uibModalInstance.dismiss('cancel');
            		}, handleError);
            	} else if(this.name != "") {
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
            }
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        function handleError(response){
            inform.add(response.message, {
                'ttl': 3000,
                'type': 'danger'
            });
        }

    });
})();


(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('UploadController', function($state, $uibModalInstance, tc){
        var that = this;
        var ids = tc.ids($state.params.facilityName);
        this.files = [];

        this.datasetId = parseInt($state.params.datasetId);

        this.upload = function(){
        	if(this.datasetId){
        		ids.upload(this.datasetId, this.files).then(function(){
        			$uibModalInstance.dismiss('cancel');
        		});
        	}
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
       
    });
})();

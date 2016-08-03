
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('SearchSampleController', function($uibModalInstance){
        this.value = "";

        this.submit = function(){
            if(this.value === '') return;
            $uibModalInstance.close(this.value);
        };

        this.cancel = function(){
            $uibModalInstance.dismiss('cancel');
        };
    });


})();

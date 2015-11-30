
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('ParameterModalController', function($uibModalInstance){

        this.name = "";
        this.valueType = "";
        this.value = "";

        this.parameterTypes = [
             {'name' : 'Text', 'id' : 'text'},
             {'name' : 'Number', 'id' : 'number'},
             {'name' : 'Date', 'id' : 'date'},
        ];

        this.submit = function(){
            if(this.name === '' || this.valueType === '' || this.value === '') return;
            $uibModalInstance.close({
                name: this.name,
                valueType: this.valueType,
                value: this.value
            });
        };

        this.cancel = function(){
            $uibModalInstance.dismiss('cancel');
        };
    });


})();

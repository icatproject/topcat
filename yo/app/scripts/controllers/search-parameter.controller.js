
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('SearchParameterController', function($uibModalInstance, $filter, tc){
        var that = this;

        this.textValue = "";
        this.numberValue = 0;
        this.dateValue = new Date();
        this.isDateValueOpen = false;
        this.dateFormat = 'yyyy-MM-dd';
        this.parameterTypes = [];
        this.permissibleStringValues = [];

        _.each(tc.userFacilities(), function(facility){
            facility.icat().query("select parameterType from ParameterType parameterType include parameterType.permissibleStringValues").then(function(parameterTypes){
                that.parameterTypes = that.parameterTypes.concat(parameterTypes);
                that.parameterTypes = _.sortBy(that.parameterTypes, 'name');
            });
        });

        this.openDateValue = function(){
            this.isDateValueOpen = true;
        };

        this.submit = function(){
            var value;
            if(this.parameterType.valueType == 'STRING'){
                value = this.textValue;
            } else if(this.parameterType.valueType == 'NUMERIC'){
                value = this.numberValue;
            } else if(this.parameterType.valueType == 'DATE_AND_TIME'){
                value = $filter('date')(this.dateValue, this.dateFormat);
            }

            $uibModalInstance.close({
                parameterType: this.parameterType,
                value: value
            });
        };

        this.cancel = function(){
            $uibModalInstance.dismiss('cancel');
        };
    });


})();

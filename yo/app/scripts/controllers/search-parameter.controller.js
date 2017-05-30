
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('SearchParameterController', function($uibModalInstance, $filter, tc){
        var that = this;

        this.textValue = "";
        this.numberValue = 0;
        this.numberFromValue = 0;
        this.numberToValue = 0;
        this.dateValue = new Date();
        this.dateFromValue = new Date();
        this.dateToValue = new Date();
        this.isDateValueOpen = false;
        this.isDateFromValueOpen = false;
        this.isDateToValueOpen = false;
        this.dateFormat = 'yyyy-MM-dd';
        this.parameterTypes = [];
        this.permissibleStringValues = [];
        this.operator = "match_value";

        _.each(tc.userFacilities(), function(facility){
            facility.icat().query([
                "select parameterType from ParameterType parameterType",
                "where",
                "parameterType.applicableToInvestigation = true or",
                "parameterType.applicableToDataset = true or",
                "parameterType.applicableToDatafile = true",
                "include parameterType.permissibleStringValues"
            ]).then(function(parameterTypes){
                that.parameterTypes = that.parameterTypes.concat(parameterTypes);
                that.parameterTypes = _.sortBy(that.parameterTypes, 'name');
            });
        });

        this.openDateValue = function(){
            this.isDateValueOpen = true;
        };

        this.openDateFromValue = function(){
            this.isDateFromValueOpen = true;
        };

        this.openDateToValue = function(){
            this.isDateToValueOpen = true;
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

            var valueFrom;
            var valueTo;

            if(this.parameterType.valueType == 'NUMERIC'){
                valueFrom = this.numberFromValue;
                valueTo = this.numberToValue;
            } else if(this.parameterType.valueType == 'DATE_AND_TIME'){
                value = $filter('date')(this.dateValue, this.dateFormat);

                valueFrom = $filter('date')(this.dateFromValue, this.dateFormat);
                valueTo = $filter('date')(this.dateToValue, this.dateFormat);
            }

            $uibModalInstance.close({
                name: this.parameterType.name,
                valueType: this.parameterType.valueType,
                operator: this.operator,
                value: value,
                valueFrom: valueFrom,
                valueTo: valueTo
            });
        };

        this.cancel = function(){
            $uibModalInstance.dismiss('cancel');
        };
    });


})();

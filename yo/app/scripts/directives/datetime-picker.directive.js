

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('datetimePicker', function(){
        return {
            restrict: 'A',
            templateUrl: '/views/datetime-picker.html',
            controller: "DatetimePickerController as datetimePickerController",
            scope: {
                value: '=ngModel'
            }

        };
    });

    app.controller('DatetimePickerController', function($scope, $element){
        var dropdownElement = $($element).find('.datetime-picker');
        this.datetime = new Date();

        this.clear = function(){
            $scope.value = "";
            this.close();
        };

        this.ok = function(){
            var year = this.datetime.getFullYear();
            var month = this.datetime.getMonth() + 1;
            if(month < 10) month = "0" + month;
            var day = this.datetime.getDate();
            if(day < 10) day = "0" + day;
            var hours = this.datetime.getHours();
            if(hours < 10) hours = "0" + hours;
            var minutes = this.datetime.getMinutes();
            if(minutes < 10) minutes = "0" + minutes;
            var seconds = this.datetime.getSeconds();
            if(seconds < 10) seconds = "0" + seconds;

            $scope.value = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds; 
            this.close();
        };

        this.close = function(){
            $(dropdownElement).css('display', 'none');
        };
    });


})();


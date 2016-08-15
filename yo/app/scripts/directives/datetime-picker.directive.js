

(function() {
    'use strict';

    var app = angular.module('topcat');

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

    app.controller('DatetimePickerController', function($scope, $element, $attrs){
        var that = this;
        var dropdownElement = $($element).find('.datetime-picker');
        this.isOnlyDate = $attrs['onlyDate'] !== undefined;
        this.datetime = new Date();
        this.placeholder = $attrs['placeholder'];

        $scope.$watch('value', function(){
            if($scope.value){
                var segments = $scope.value.split(/[-:\s]+/);
                var year = segments[0];
                var month = segments[1] || "01";
                var day = segments[2] || "01";
                var hours = segments[3] || "00";
                var minutes = segments[4] || "00";
                var seconds = segments[5] || "00";

                year = year + '0000'.slice(year.length, 4);
                month = month + '00'.slice(month.length, 2);
                day = day + '00'.slice(day.length, 2);
                hours = hours + '00'.slice(hours.length, 2);
                minutes = minutes + '00'.slice(minutes.length, 2);
                seconds = seconds + '00'.slice(seconds.length, 2);

                if(parseInt(month) == 0) month = '01';
                if(parseInt(day) == 0) day = '01';

                var value = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;

                that.datetime = new Date(Date.parse(value));
            } else {
                that.datetime = new Date();
            }

        }, true);

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

            if(this.isOnlyDate){
                $scope.value = year + "-" + month + "-" + day; 
            } else {
                $scope.value = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds; 
            }
            this.close();
        };

        this.close = function(){
            $(dropdownElement).css('display', 'none');
        };
    });


})();


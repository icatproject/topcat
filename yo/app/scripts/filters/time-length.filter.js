(function() {
    'use strict';

    var app = angular.module('angularApp');

    var minuteLength = 60;
    var hourLength = (60 * minuteLength);
    var dayLength = (24 * hourLength);
    

    app.filter('timeLength', function($translate){
        return function(value){
            if(value === undefined) return '';

            var days = Math.floor(value / dayLength);
            value = value - (days * dayLength);
            var hours = Math.floor(value / hourLength);
            value = value - (hours * hourLength);
            var minutes = Math.floor(value / minuteLength);
            value = value - (minutes * minuteLength);
            var seconds = Math.ceil(value);

            var out = [];
            if(days > 0) {
                if(days == 1){
                    out.push(days + " " + $translate.instant("TIME.DAY"));
                } else {
                    out.push(days + " " + $translate.instant("TIME.DAYS"));
                }
            }
            if(hours > 0) {
                if(hours == 1){
                    out.push(hours + " " + $translate.instant("TIME.HOUR"));
                } else {
                    out.push(hours + " " + $translate.instant("TIME.HOURS"));
                }
            }
            if(minutes > 0) {
                if(minutes == 1){
                    out.push(minutes + " " + $translate.instant("TIME.MINUTE"));
                } else {
                    out.push(minutes + " " + $translate.instant("TIME.MINUTES"));
                }
            }
            if(seconds > 0) {
                if(seconds == 1){
                    out.push(seconds + " " + $translate.instant("TIME.SECOND"));
                } else {
                    out.push(seconds + " " + $translate.instant("TIME.SECONDS"));
                }
            }

            return out.join(', ');
        };
    });

    
})();

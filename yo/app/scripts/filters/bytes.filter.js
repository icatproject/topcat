(function() {
    'use strict';

    angular.module('bytes', []).filter('bytes', function($translate, tc) {
        return function(value) {
            var bytes = parseInt(value || "0");
            var power = tc.config().enableKiloBinaryBytes ? 1024 : 1000;
            var units = ['KB', 'MB', 'GB', 'TB', 'PB'];
            var unit = 'B';

            while(units.length > 0 && bytes > power){
                bytes = bytes / power;
                unit = units.shift();
            }

            bytes = "" + _.ceil(bytes, 2);

            if(bytes.match(/\.\d$/)){
                bytes = bytes + "0";
            }

            return bytes + " " + $translate.instant('FILE_SIZE_UNITS.' + unit);
        };
    });

})();



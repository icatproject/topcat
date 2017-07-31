(function() {
    'use strict';

    angular.module('bytes', []).filter('bytes', function(tc) {
        return function(value) {
            var bytes = parseInt(value || "0");
            var power = tc.config().enableKiloBinaryBytes ? 1024 : 1000;
            var units = ['B', 'MB', 'GB', 'TB', 'PB'];
            var unit = units.shift();

            while(units.length > 0 && (bytes / power) > 1){
                bytes = bytes / power;
                unit = units.shift();
            }

            bytes = "" + _.ceil(bytes, 2);

            if(bytes.match(/\.\d$/)){
                bytes = bytes + "0";
            }

            return bytes + " " + unit;
        };
    });

})();



(function() {
    'use strict';

    var app = angular.module('topcat');

    app.filter('parseInt', function(){
        return function(value){
            return parseInt(('' + value).replace(/^.*?(\d+).*$/, '$1'));
        };
    });

    
})();

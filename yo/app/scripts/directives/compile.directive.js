

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.directive('compile', function($compile){
        return {
            restrict: 'A',
            link: function(scope, element, attrs){
                var path = attrs.compile.split(/\./);
                var template = scope;
                while(path.length > 0){
                    template = template[path.shift()];
                    if(template === undefined) return;
                }
                template = '<span>' + template + '</span>';
                return $(element).html($compile(template)(scope));
            }
        };
    });

})();


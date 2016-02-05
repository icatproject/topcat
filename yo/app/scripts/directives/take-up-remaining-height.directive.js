

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('takeUpRemainingHeight', function(){
        return {
            restrict: 'A',
            controller: function($element, $timeout){
                $timeout(function(){
                    var height = $($element).parent().innerHeight();
                    $($element).parent().children().each(function(){
                        if(this != $element[0]) {
                            height = height - $(this).outerHeight();
                        }
                    });
                    $($element).css('height', height + 'px');
                });
                
            }
        };
    });

})();


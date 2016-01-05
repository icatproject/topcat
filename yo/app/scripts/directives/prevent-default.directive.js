

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('preventDefault', function(){
        return {
            restrict: 'A',
            link: function(scope, element, attrs){
            	$(element).on('keypress', function(e){
            		if(e.which == 13) e.preventDefault();
            	});
            }
        };
    });

})();


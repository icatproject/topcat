

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('onReturnClickSubmitButton', function(){
        return {
            restrict: 'A',
            link: function(scope, element, attrs){
            	$(element).on('keypress', function(e){
            		if(e.which == 13){
            			e.preventDefault();
            			$(this).parents('form').find('input[type=submit], button[type=submit]').last().trigger('click');
            		}
            	});
            }
        };
    });

})();


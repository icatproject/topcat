

(function() {
    'use strict';

    var app = angular.module('angularApp');
    var lastHeight;

    app.directive('takeUpRemainingHeight', function(){
        return {
            restrict: 'A',
            controller: function($element, $timeout, $scope){

                if(lastHeight){
                    $($element).css('height', lastHeight + 'px');
                }

                function refresh(){
                    var height;
                    var viewport = $($element).parents('.modal-content').first();
                    if(viewport.length > 0){
                        height = $(viewport).height();
                    } else {
                        height = $(window).height();
                        viewport = document.body;
                    }

                    var heightInUse = 0;
                    $(viewport).children().each(function(i, element){
                        if($(element).css('z-index') != 'auto') return;
                        heightInUse = heightInUse + $(element).outerHeight();
                    });

                    var remainingHeight = height - heightInUse;


                    $($element).css('height', $($element).height() + remainingHeight + 'px');
                    lastHeight = $($element).height();

                    if(remainingHeight !== 0){
                        $($element).trigger('resize');
                    }
                }

                var interval = setInterval(refresh, 50);

                $scope.$on('$destroy', function(){
                    clearInterval(interval);
                });
            }
        };
    });

})();


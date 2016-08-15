

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.directive('loading', function($rootScope, $compile){
        return {
            restrict: 'A',
            scope: {
                loading: '=loading'
            },
            link: function(scope, element, attrs){
                var timeout;
                scope.isLoaded = false;
                var stopListeningForLoadedEvent = $rootScope.$on('loaded', function(){
                    scope.isLoaded = true;
                    stopListeningForLoadedEvent();
                });
                scope.$on('$destroy', function(){
                    stopListeningForLoadedEvent();
                });

                scope.$watch('loading', function(){
                    if(!scope.loading){
                        scope.isLoaded = true;
                        stopListeningForLoadedEvent();
                    }
                });

                $(element).before($compile('<span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="!isLoaded" class="grid-cell-spinner"></span>')(scope));
            }
        };
    });

})();


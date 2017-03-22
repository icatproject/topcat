

(function() {
    'use strict';

    var app = angular.module('topcat');


    app.directive('widget', function($compile, $http){
        return {
            restrict: 'E',
            scope: {
              controller: '=',
              view: '='
            },
            link: function(scope, element, attrs){
                var view = scope.view;
                var controller = scope.controller;

                $http.get(view).then(function(response){
                    var template = attrs.controller ? '<span ng-controller="' + controller + '">' + response.data+ '</span>' : '<span>' + response.data + '</span>';
                    $(element).html($compile(template)(scope));
                });
            }
        };
    });

})();


(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadAvailabilityController', LoadAvailabilityController);

    LoadAvailabilityController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', '$timeout', '$log'];

    function LoadAvailabilityController ($scope, $element, $attrs, uiGridConstants, $timeout, $log) { //jshint ignore: line
        if ($scope.ngModel.entity.getAvailability() === null) {
            $timeout(function(){
                $scope.ngModel.entity.setAvailability(Math.round(Math.random()));
            }, _.random(1000, 6000));
        }
    }


    angular.
        module('angularApp').directive('loadAvailability', loadAvailability);

    loadAvailability.$inject = [];

    function loadAvailability() {
        return {
            restrict: 'A', //E = element, A = attribute, C = class, M = comment
            require: '^ngModel',
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                ngModel: '='
            },
            controller: 'LoadAvailabilityController'
        };
    }

})();
(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadSizeController', LoadSizeController);

    LoadSizeController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', '$timeout', '$log'];

    function LoadSizeController ($scope, $element, $attrs, uiGridConstants, $timeout, $log) { //jshint ignore: line
        if ($scope.ngModel.entity.getSize() === null) {
            $timeout(function(){
                $scope.ngModel.entity.setSize(_.random(1, 999999));
            }, _.random(1000, 6000));
        }
    }


    angular.
        module('angularApp').directive('loadSize', loadSize);

    loadSize.$inject = [];

    function loadSize() {
        return {
            restrict: 'A', //E = element, A = attribute, C = class, M = comment
            require: '^ngModel',
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                ngModel: '='
            },
            controller: 'LoadSizeController'
        };
    }

})();
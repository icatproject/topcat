(function() {
    'use strict';

    angular.
        module('angularApp').directive('cartItemCount', cartItemCount);

    cartItemCount.$inject = ['Cart'];

    function cartItemCount(Cart) {
        return {
            restrict: 'E', //E = element, A = attribute, C = class, M = comment
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                'singular': '@',
                'plural': '@'
            },
            template: '<span>{{count}} {{text}}</span>',
            link: function ($scope, element, attrs) { //jshint ignore: line
                $scope.text = $scope.plural;
                $scope.count = 0;

                $scope.$watch(function () {
                    return Cart._cart.items.length;
                }, function (newValue, oldValue) {
                    if ( newValue !== oldValue ) {
                        $scope.count = Cart._cart.items.length;

                        if (newValue === 1) {
                            $scope.text = $scope.singular;
                        } else {
                            $scope.text = $scope.plural;
                        }
                    }
                });
            }
        };
    }

})();
(function() {
    'use strict';

    angular.
        module('angularApp').controller('BreadCrumbController', BreadCrumbController);

    BreadCrumbController.$inject = ['$scope', '$state', '$stateParams', 'RouteService'];

    function BreadCrumbController ($scope, $state, $stateParams, RouteService) {
        var bc = this;
        var previousRoutes = RouteService.getPreviousRoutes($state);

        var items = [];

        _.each(previousRoutes, function(route){
            var item = {
                translate: 'ENTITIES.' + route.entity.toUpperCase() + '.NAME'
            };

            //special case for facility
            if (route.route === 'facility') {
                item.route = 'home.browse.facility('+ JSON.stringify($stateParams) + ')';
            } else {
                item.route = 'home.browse.facility.' + route.route + '('+ JSON.stringify($stateParams) + ')';
            }

            items.push(item);
        });

        bc.items = items;
    }


    angular.
        module('angularApp').directive('breadCrumb', breadCrumb);

    breadCrumb.$inject = [];

    function breadCrumb() {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                items: '@'
            },
            template: '<ul class="breadcrumb"><li ng-repeat="item in bc.items"><span ng-show="! $last"><a ui-sref="{{ item.route }}" translate="{{ item.translate }}"></a></span><span ng-show="$last" translate="{{ item.translate }}"></span></li></ul>',
            //templateUrl: 'views/breadcrumb.directive.html',
            controller: 'BreadCrumbController',
            controllerAs: 'bc'
        };
    }

})();
(function() {
    'use strict';

    angular.
        module('angularApp').controller('BreadCrumbController', BreadCrumbController);

    BreadCrumbController.$inject = ['$scope',  '$state', '$stateParams', 'RouteService', '$log'];

    function BreadCrumbController ($scope, $state, $stateParams, RouteService, $log) {
        var bc = this;

        $log.debug($state);

        var previousRoutes = RouteService.getPreviousRoutes($state);

        var items = [];

        _.each(previousRoutes, function(route){
            var item = {
                title : route.entity
            };

            //special case for facility
            if (route.route === 'facility') {
                item.route = 'home.browse.facility('+ JSON.stringify($stateParams) + ')';
            } else {
                item.route = 'home.browse.facility.' + route.route + '('+ JSON.stringify($stateParams) + ')';
            }

            items.push(item);
        });

        $log.debug('items', items);

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
            template: '<ul class="breadcrumb"><li ng-repeat="item in bc.items"><span ng-show="! $last"><a ui-sref="{{ item.route }}">{{ item.title }}</a></span><span ng-show="$last">{{ item.title }}</span></li></ul>',
            //templateUrl: 'views/breadcrumb.directive.html',
            controller: 'BreadCrumbController',
            controllerAs: 'bc'
        };
    }

})();
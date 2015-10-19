(function() {
    'use strict';

    angular.
        module('angularApp').controller('BreadCrumbController', BreadCrumbController);

    BreadCrumbController.$inject = ['$scope', '$state', '$stateParams', '$sessionStorage', '$q', 'Config', 'RouteService', 'APP_CONFIG', 'ICATService'];

    function BreadCrumbController ($scope, $state, $stateParams, $sessionStorage, $q, Config, RouteService, APP_CONFIG, ICATService) {
        var bc = this;
        var previousRoutes = RouteService.getPreviousRoutes($state);
        var sessionId = $stateParams.facilityName ? $sessionStorage.sessions[$stateParams.facilityName].sessionId : null;
        var facility = Config.getFacilityByName(APP_CONFIG, $stateParams.facilityName);
        var promises = [];
        var items = [];

        var titles = {};
        if(facility){
            titles.facility = facility.title;
        }
        if($stateParams.proposalId){
            titles.proposal = $stateParams.proposalId;
        }
        if($stateParams.investigationId){
            promises.push(ICATService.getEntityById(
                sessionId,
                facility,
                'Investigation',
                $stateParams.investigationId
            ).success(function(data){
                titles.investigation = data[0].Investigation.title;
            }));
        }
        if($stateParams.datasetId){
            promises.push(ICATService.getEntityById(
                sessionId,
                facility,
                'Dataset',
                $stateParams.datasetId
            ).success(function(data){
                titles.dataset = data[0].Dataset.name;
            }));
        }
        
        
        $q.all(promises).then(function(){
            _.each(previousRoutes, function(route){
                var item = {
                    translate: 'ENTITIES.' + route.entity.toUpperCase() + '.NAME',
                    title: titles[route.entity]
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
            template: '<ul class="breadcrumb"><li ng-repeat="item in bc.items"><span ng-show="! $last"><a ui-sref="{{ item.route }}">{{item.title}}</a></span><span ng-show="$last" translate="{{ item.translate }}"></span></li></ul>',
            //templateUrl: 'views/breadcrumb.directive.html',
            controller: 'BreadCrumbController',
            controllerAs: 'bc'
        };
    }

})();
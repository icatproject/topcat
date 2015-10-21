(function() {
    'use strict';

    angular.
        module('angularApp').controller('BreadCrumbController', BreadCrumbController);

    BreadCrumbController.$inject = ['$scope', '$state', '$stateParams', '$sessionStorage', '$q', 'Config', 'RouteService', 'APP_CONFIG', 'ICATService'];

    function BreadCrumbController ($scope, $state, $stateParams, $sessionStorage, $q, Config, RouteService, APP_CONFIG, ICATService) {
        var bc = this;
        var previousRoutes = RouteService.getPreviousRoutes($state);
        var sessionId = $stateParams.facilityName ? $sessionStorage.sessions[$stateParams.facilityName].sessionId : null;
        var facility = $stateParams.facilityName ? Config.getFacilityByName(APP_CONFIG, $stateParams.facilityName) : null;
        var promises = [];
        var items = [];

        var titles = {};
        if(facility){
            titles.facility = facility.title;
        }
        if($stateParams.proposalId){
            titles.proposal = $stateParams.proposalId;
        }
        if($stateParams.instrumentId){
            promises.push(ICATService.getEntityById(
                sessionId,
                facility,
                'Instrument',
                $stateParams.instrumentId
            ).success(function(data){
                titles.instrument = data[0].Instrument.name;
            }));
        }
        if($stateParams.facilityCycleId){
            promises.push(ICATService.getEntityById(
                sessionId,
                facility,
                'FacilityCycle',
                $stateParams.facilityCycleId
            ).success(function(data){
                titles.facilityCycle = data[0].FacilityCycle.name;
            }));
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
            
            if(_.keys(APP_CONFIG.facilities).length > 1){
                items.push({
                    translate: 'BROWSE.BREADCRUMB.ROOT.NAME',
                    route: 'home.browse.facility'
                });
            }

            _.each(previousRoutes, function(route, i){
                var item = {
                    translate: 'ENTITIES.' + route.entity.toUpperCase() + '.NAME',
                    title: titles[route.entity]
                };

                //special case for facility
                var nextRoute = previousRoutes[i + 1];

                if(nextRoute){
                    item.route = 'home.browse.facility.' + nextRoute.route + '('+ JSON.stringify($stateParams) + ')';
                } else {
                    item.route = $state.current.name;
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
            templateUrl: 'views/breadcrumb.directive.html',
            //templateUrl: 'views/breadcrumb.directive.html',
            controller: 'BreadCrumbController',
            controllerAs: 'bc'
        };
    }

})();
(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadAvailabilityController', LoadAvailabilityController);

    LoadAvailabilityController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', 'APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$timeout', 'usSpinnerService', 'inform', '$log'];

    function LoadAvailabilityController ($scope, $element, $attrs, uiGridConstants, APP_CONFIG, Config, IdsManager, $sessionStorage, $timeout, usSpinnerService, inform, $log) { //jshint ignore: line
        $timeout(loadStatus, 0);

        function loadStatus() {
            var params = {};
            params[$scope.ngModel.entity.getEntityType()  + 'Ids'] = $scope.ngModel.entity.getEntityId();
            var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.entity.getFacilityName());

            usSpinnerService.spin('spinner-status-' + $scope.ngModel.uid);

            IdsManager.getStatus($sessionStorage.sessions, facility, params).then(function(data){
                $scope.ngModel.entity.availability = data;
                usSpinnerService.stop('spinner-status-' + $scope.ngModel.uid);
            }, function(error) {
                $log.error(error);
                inform.add(error, {
                    'ttl': 4000,
                    'type': 'danger'
                });
            });

        }
    }

    angular.
        module('angularApp').directive('loadAvailability', loadAvailability);

    loadAvailability.$inject = [];

    function loadAvailability() {
        return {
            restrict: 'A', //E = element, A = attribute, C = class, M = comment
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                ngModel: '='
            },
            controller: 'LoadAvailabilityController'
        };
    }

})();
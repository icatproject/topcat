(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadAvailabilityController', LoadAvailabilityController);

    LoadAvailabilityController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', 'APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$log'];

    function LoadAvailabilityController ($scope, $element, $attrs, uiGridConstants, APP_CONFIG, Config, IdsManager, $sessionStorage, $log) { //jshint ignore: line
        if ($scope.ngModel.entity.getAvailability() === null) {
            var params = {};
            params[$scope.ngModel.entity.getEntityType()  + 'Ids'] = $scope.ngModel.entity.getId();
            var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.entity.getFacilityName());

            IdsManager.getStatus($sessionStorage.sessions, facility, params).then(function(data){
                $scope.ngModel.entity.setAvailability(data);
            });
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
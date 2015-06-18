(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadSizeController', LoadSizeController);

    LoadSizeController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', 'APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$log'];

    function LoadSizeController ($scope, $element, $attrs, uiGridConstants, APP_CONFIG, Config, IdsManager, $sessionStorage, $log) { //jshint ignore: line
        if ($scope.ngModel.entity.getSize() === null) {
            var params = {};
            params[$scope.ngModel.entity.getEntityType()  + 'Ids'] = $scope.ngModel.entity.getId();
            var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.entity.getFacilityName());

            IdsManager.getSize($sessionStorage.sessions, facility, params).then(function(data){
                $scope.ngModel.entity.setSize(parseInt(data));
            });
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
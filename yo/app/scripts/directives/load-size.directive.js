(function() {
    'use strict';

    angular.
        module('angularApp').controller('LoadSizeController', LoadSizeController);

    LoadSizeController.$inject = ['$scope', '$element', '$attrs', 'uiGridConstants', 'APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$timeout', 'usSpinnerService'];

    function LoadSizeController ($scope, $element, $attrs, uiGridConstants, APP_CONFIG, Config, IdsManager, $sessionStorage, $timeout, usSpinnerService) {
        $timeout(loadSize, 0);

        function loadSize() {
            var params = {};
            params[$scope.ngModel.entity.getEntityType()  + 'Ids'] = $scope.ngModel.entity.getEntityId();
            var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.entity.getFacilityName());

            usSpinnerService.spin('spinner-size-' + $scope.ngModel.uid);

            IdsManager.getSize($sessionStorage.sessions, facility, params).then(function(data){
                $scope.ngModel.entity.size = parseInt(data);
                usSpinnerService.stop('spinner-size-' + $scope.ngModel.uid);
            });
        }
    }

    angular.
        module('angularApp').directive('loadSize', loadSize);

    loadSize.$inject = [];

    function loadSize() {
        return {
            restrict: 'A', //E = element, A = attribute, C = class, M = comment
            scope: {
                //@ reads the attribute value, = provides two-way binding, & works with functions
                ngModel: '='
            },
            controller: 'LoadSizeController'
        };
    }

})();
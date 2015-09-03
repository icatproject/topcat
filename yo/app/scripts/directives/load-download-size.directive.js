(function() {
    'use strict';

    angular.
        module('angularApp').directive('loadDownloadSize', loadDownloadSize);

    loadDownloadSize.$inject = ['APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$timeout', 'usSpinnerService', '$log'];

    function loadDownloadSize(APP_CONFIG, Config, IdsManager, $sessionStorage, $timeout, usSpinnerService, $log) { //jshint ignore: line
        return {
            restrict: 'A',
            scope: {
                ngModel: '='
            },
            link: function($scope, $element, $attrs) { //jshint ignore: line
                $timeout(calcDownnloadSize, 0);

                function calcDownnloadSize() {
                    if ($scope.ngModel.getSize() === null) {
                        var params = $scope.ngModel.getDataSelection();
                        var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.getFacilityName());

                        $log.debug('calcDownnloadSize called', params);

                        usSpinnerService.spin('spinner-size-' + $scope.ngModel.getFacilityName());

                        IdsManager.getSize($sessionStorage.sessions, facility, params).then(function(data){
                            $scope.ngModel.setSize(parseInt(data));
                            usSpinnerService.stop('spinner-size-' + $scope.ngModel.getFacilityName());
                        });
                    } else {
                        $log.debug('calcDownnloadSize not null');
                    }
                }
            }
        };
    }

})();
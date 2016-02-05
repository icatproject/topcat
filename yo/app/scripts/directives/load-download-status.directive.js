(function() {
    'use strict';

    angular.
        module('angularApp').directive('loadDownloadStatus', loadDownloadStatus);

    loadDownloadStatus.$inject = ['APP_CONFIG', 'Config', 'IdsManager', '$sessionStorage', '$timeout', 'usSpinnerService'];

    function loadDownloadStatus(APP_CONFIG, Config, IdsManager, $sessionStorage, $timeout, usSpinnerService) {
        return {
            restrict: 'A',
            scope: {
                ngModel: '='
            },
            link: function($scope, $element, $attrs) { //jshint ignore: line
                $timeout(calcDownnloadStatus, 0);

                function calcDownnloadStatus() {
                    if ($scope.ngModel.getAvailability() === null) {
                        var params = $scope.ngModel.getDataSelection();
                        var facility = Config.getFacilityByName(APP_CONFIG, $scope.ngModel.getFacilityName());

                        usSpinnerService.spin('spinner-status-' + $scope.ngModel.getFacilityName());

                        IdsManager.getStatus($sessionStorage.sessions, facility, params).then(function(data){
                            $scope.ngModel.setAvailability(data);
                            usSpinnerService.stop('spinner-status-' + $scope.ngModel.getFacilityName());
                        });
                    }
                }
            }
        };
    }

})();
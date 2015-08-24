(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('DownloadController', DownloadController)
        .controller('RemoveDownloadModalController', RemoveDownloadModalController);

    DownloadController.$inject = ['$rootScope', '$scope', '$state', 'APP_CONFIG', 'Config', 'Cart', 'DownloadModel', '$sessionStorage', '$modal', 'uiGridConstants', '$log'];
    RemoveDownloadModalController.$inject = ['$modalInstance', 'APP_CONFIG', 'Config', 'row', 'TopcatManager', 'inform', '$log'];


    function DownloadController($rootScope, $scope, $state, APP_CONFIG, Config, Cart, DownloadModel, $sessionStorage, $modal, uiGridConstants, $log) { //jshint ignore: line
        var dl = this;
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        dl.isScroll = (pagingType === 'scroll') ? true : false;

        DownloadModel.init($scope);

        dl.gridOptions = DownloadModel.gridOptions;

        $scope.remove = function(row, rowIndex) {
            $log.debug('download remove called');
            $log.debug(row, rowIndex);

            var modalInstance = $modal.open({
                templateUrl: 'views/remove-download-modal.html',
                controller: 'RemoveDownloadModalController as vm',
                resolve: {
                    row : function() {
                        return row;
                    }
                }
            });

            modalInstance.result.then(function () {
                dl.gridOptions.data.splice(rowIndex, 1);
                $scope.gridApi.grid.refresh(true);
            }, function () {
                $log.debug('Remove cancelled');
            });
        };
    }

    function RemoveDownloadModalController($modalInstance, APP_CONFIG, Config, row, TopcatManager, inform, $log) { //jshint ignore: line
        var vm = this;

        vm.ok = function() {
            $modalInstance.close(row);

            var facility = Config.getFacilityByName(APP_CONFIG, row.facilityName);

            TopcatManager.removeDownloadByPreparedId(facility, row.userName, row.preparedId).then(function(data) {
                $log.debug('removeDownloadByPreparedId', data);

                if (typeof data.value !== 'undefined') {
                    inform.add('Download successfully removed', {
                        'ttl': 4000,
                        'type': 'success'
                    });
                } else if (_.isEmpty(data) === true) {
                    inform.add('Failed to removed download. Either the download has already been removed or you do not have the correct permission.', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                }
            }, function(error) { //jshint ignore: line
                inform.add('Failed to removed download: ' + error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });


        };

        vm.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    }

})();

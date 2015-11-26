(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('DownloadController', DownloadController)
        .controller('RemoveDownloadModalController', RemoveDownloadModalController)
        .controller('SmartClientDownloadModalController', SmartClientDownloadModalController);

    DownloadController.$inject = ['$rootScope', '$scope', '$state', 'APP_CONFIG', 'Config', 'Cart', 'DownloadModel', '$sessionStorage', '$uibModal'];
    RemoveDownloadModalController.$inject = ['$modalInstance', 'APP_CONFIG', 'Config', 'row', 'TopcatManager', 'inform', '$translate'];
    SmartClientDownloadModalController.$inject = ['$modalInstance'];


    function DownloadController($rootScope, $scope, $state, APP_CONFIG, Config, Cart, DownloadModel, $sessionStorage, $uibModal) {
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        $scope.isScroll = (pagingType === 'scroll') ? true : false;

        $scope.gridOptions = {
            appScopeProvider: $scope
        };

        DownloadModel.init($scope);
        DownloadModel.getPage();

        $scope.gridOptions.onRegisterApi = function(gridApi) {
            $scope.gridApi = gridApi;
        };


        $scope.remove = function(row, rowIndex) {
            var modalInstance = $uibModal.open({
                templateUrl: 'views/remove-download-modal.html',
                controller: 'RemoveDownloadModalController as vm',
                resolve: {
                    row : function() {
                        return row;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.gridOptions.data.splice(rowIndex, 1);
                $scope.gridApi.grid.refresh(true);
            }, function () {
            });
        };

        $scope.refresh = function() {
            DownloadModel.refresh();
        };

        $scope.smartClientModal = function() {
            $uibModal.open({
                templateUrl: 'views/smartclient-download-modal.html',
                controller: 'SmartClientDownloadModalController as sc',
            });

            //window.alert('Your files were downloaded by the Smart Client. Please check your home smartclient directory for the downloaded files');
        };
    }

    function RemoveDownloadModalController($modalInstance, APP_CONFIG, Config, row, TopcatManager, inform, $translate) {
        var vm = this;

        vm.ok = function() {
            $modalInstance.close(row);

            var facility = Config.getFacilityByName(APP_CONFIG, row.facilityName);

            TopcatManager.removeDownloadByPreparedId(facility, row.userName, row.preparedId).then(function(data) {
                if (typeof data.value !== 'undefined') {
                    inform.add($translate.instant('DOWNLOAD.REMOVE_DOWNLOAD.NOTIFY_MESSAGE.SUCCESS'), {
                        'ttl': 4000,
                        'type': 'success'
                    });
                } else if (_.isEmpty(data) === true) {
                    inform.add($translate.instant('DOWNLOAD.REMOVE_DOWNLOAD.NOTIFY_MESSAGE.FAILURE'), {
                        'ttl': 0,
                        'type': 'danger'
                    });
                }
            }, function(error) { //jshint ignore: line
                inform.add($translate.instant('DOWNLOAD.REMOVE_DOWNLOAD.NOTIFY_MESSAGE.ERROR', {'errorMEssage' : error}), {
                    'ttl': 0,
                    'type': 'danger'
                });
            });


        };

        vm.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    }


    function SmartClientDownloadModalController($modalInstance) {
        var sc = this;

        sc.ok = function() {
            $modalInstance.dismiss('cancel');
        };
    }

})();

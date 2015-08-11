(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('DownloadController', DownloadController);

    DownloadController.$inject = ['$rootScope', '$scope', '$state', 'APP_CONFIG', 'Config', 'Cart', 'DownloadModel', '$sessionStorage', '$log'];

    function DownloadController($rootScope, $scope, $state, APP_CONFIG, Config, Cart, DownloadModel, $sessionStorage, $log) { //jshint ignore: line
        var dl = this;
        var pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'

        $scope.isEmpty = false;
        dl.isScroll = (pagingType === 'scroll') ? true : false;

        DownloadModel.init($scope);

        dl.gridOptions = DownloadModel.gridOptions;


        $scope.getDownloadUrl = function(row) {
            $log.debug('getDownloadUrl called');
            if (row.entity.transport === 'https') {
                if (row.entity.status === 'COMPLETE') {
                    return '<a href="' + row.entity.transportUrl + '/ids/getData?preparedId=' + row.entity.preparedId + '&outname=' + row.entity.fileName + '">Download</a>';
                } else {
                    return '<span class="not-active">Download</span>';
                }
            } else if (row.entity.transport === 'globus') {
                var route = $state.href('globus-faq');
                return '<a href="' + route + '">Download Via Globus</a>';
            }

            return '';
        };

    }
})();

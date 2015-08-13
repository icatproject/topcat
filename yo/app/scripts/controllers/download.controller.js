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
    }
})();

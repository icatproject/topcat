'use strict';

angular
    .module('angularApp')
    .factory('DownloadModel', DownloadModel);

DownloadModel.$inject = ['$rootScope', 'Cart', '$sessionStorage', '$log'];

function DownloadModel($rootScope, Cart, $sessionStorage, $log){  //jshint ignore: line


    return {

        init : function() {

        }

    };
}


(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', '$translate', '$sessionStorage'];

    function IndexController($scope, $translate, $sessionStorage) {
        var vm = this;
        vm.changeLanguage = function (langKey) {
            $translate.use(langKey);
        };

        vm.facilitiesToLogout = function(){
            return _.keys($sessionStorage.sessions);
        };

        vm.isLoggedIn = function(){
            return ! (_.isEmpty($sessionStorage.sessions));
        };
    }
})();

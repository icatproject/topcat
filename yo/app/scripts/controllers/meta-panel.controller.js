(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = [];

    function MetaPanelController(){
        var vm = this;

        //tabs config
        var data = [

        ];

        vm.data = data;
    }
})();

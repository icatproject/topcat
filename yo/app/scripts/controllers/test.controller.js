'use strict';

angular
    .module('angularApp')
    .controller('TestController', TestController);

TestController.$inject = [];

function TestController() {
    var vm = this;

    var random;

    if (!random) {
        random = Math.round(Math.random()*10000);
    }

    vm.random = random;
}


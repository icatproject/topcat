'use strict';

var TestCtrl = function() {
    var vm = this;

    var random;

    if (!random) {
        random = Math.round(Math.random()*10000);
    }

    vm.random = random;
};

angular.module('angularApp').controller('TestCtrl', TestCtrl);
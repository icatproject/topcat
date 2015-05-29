(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('ModalInstanceController', ModalInstanceController);

    ModalInstanceController.$inject = ['$modalInstance', 'searchFormData'];

    function ModalInstanceController($modalInstance, searchFormData) {
        //modal
        var vm = this;

        vm.parameterTypes = [
             {'name' : 'Text', 'id' : 'text'},
             {'name' : 'Number', 'id' : 'number'},
             {'name' : 'Date', 'id' : 'date'},
        ];

        vm.ok = function(modalForm) {
            //TODO need to prevent duplicate parameters being added here
            if (angular.isDefined(searchFormData.parameters)) {
                console.log(searchFormData.parameters);
            }

            $modalInstance.close(modalForm);
        };

        vm.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    }
})();



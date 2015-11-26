(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('ParameterModalController', ParameterModalController);

    ParameterModalController.$inject = ['$uibModal'];

    function ParameterModalController($uibModal) {
        var vm = this;

        // open a model window
        vm.openModal = function(form, size) {
            var modalInstance = $uibModal.open({
                templateUrl : 'views/search/parameter-modal.html',
                controller : 'ModalInstanceController as filter',
                size : size,
                resolve : {
                    searchFormData: function () {
                        return form;
                    }
                }
            });

            modalInstance.result.then(function(modalForm) {
                //initialize array if form.parameters not defined
                form.parameters = form.parameters || [];

                //should push the modal form data to the form data here
                form.parameters.push(modalForm);
            }, function() {

            });
        };
    }
})();


(function() {
    'use strict';

    angular.
        module('angularApp').controller('FilterDatepickerController', FilterDatepickerController);

    FilterDatepickerController.$inject = ['$scope'];

    function FilterDatepickerController ($scope) {
        $scope.opened = false;

        //Datepicker
        $scope.dateOptions = {
            'show-weeks' : false
        };
    }

    angular.
        module('angularApp').directive('filterDatepicker', filterDatepicker);

    filterDatepicker.$inject = [];

    function filterDatepicker() {
        return {
            restrict: 'E',
            scope:{
              ngModel: '=',
              dateOptions: '=',
              opened: '='
            },
            link: function($scope, element, attrs) { //jshint ignore: line
              $scope.open = function(event){
                event.preventDefault();
                event.stopPropagation();
                $scope.opened = true;
              };

              $scope.clear = function () {
                $scope.ngModel = null;
              };
            },
            controller: 'FilterDatepickerController',
            template: '<p class="input-group">' +
                '<input type="text" class="form-control input-sm" datepicker-popup="datePickerOptions" ng-model="ngModel" is-open="opened" datepicker-options="dateOptions" date-disabled="disabled(date, mode)" ng-required="true" close-text="Close" />' +
                '<span class="input-group-btn">' +
                '<button class="btn btn-default input-sm" ng-click="open($event)"><i class="glyphicon glyphicon-calendar"></i></button>' +
                '</span>' +
                '</p>'
        };
    }

})();
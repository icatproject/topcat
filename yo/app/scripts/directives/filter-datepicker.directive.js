(function() {
    'use strict';

    angular.
        module('angularApp')
        .controller('FilterDatepickerController', FilterDatepickerController)
        .directive('datepickerPopup', function (){
            return {
                restrict: 'EAC',
                require: 'ngModel',
                link: function(scope, elem, attrs, ngModel) {
                    ngModel.$parsers.push(function toModel(date) {
                        return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate();
                    });
                }
            };
        });

    FilterDatepickerController.$inject = ['$scope', '$log'];

    function FilterDatepickerController ($scope, $log) { //jshint ignore: line
        //Datepicker
        $scope.dateOptions = {
            'dateformat' : 'yyyy-MM-dd',
            'show-weeks' : false
        };

        $scope.open = function(event){ //jshint ignore: line
            //event.preventDefault();
            //event.stopPropagation();
            $scope.status.opened = true;
        };

        /*$scope.clear = function () {
            $scope.ngModel = null;
        };*/

        $scope.status = {
            opened: false
        };
    }

    angular.
        module('angularApp').directive('filterDatepicker', filterDatepicker);

    filterDatepicker.$inject = [];

    function filterDatepicker() {
        return {
            restrict: 'A',
            scope:{
              ngModel: '=',
            },
            controller: 'FilterDatepickerController'
            //template: '<input type="text" class="ui-grid-filter-input" ng-click="open($event)" ng-model="ngModel" datepicker-popup is-open="status.opened" datepicker-options="dateOptions" close-text="Close" datepicker-append-to-body="true"/>'
        };
    }

})();
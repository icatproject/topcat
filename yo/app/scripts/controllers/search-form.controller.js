(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('SearchFormController', SearchFormController);

    SearchFormController.$inject = ['$state', '$filter'];

    function SearchFormController($state, $filter) {
        var vm = this;
        vm.form = {};

        //prefill the search form with the parameters from the querystring
        if (typeof $state.params !== 'undefined') {
            vm.form = $state.params;
        }

        //set form defaults
        vm.init = {
            facilityItems : [
                {'name' : 'facility 1', 'id' : 'facility one'},
                {'name' : 'facility 2', 'id' : 'facility two'},
                {'name' : 'facility 3', 'id' : 'facility three'}
            ],
            searchTypeItems : [
                {'name' : 'Investigation', 'id' : 'investigation'},
                {'name' : 'Dataset', 'id' : 'dataset'},
                {'name' : 'Datafile', 'id' : 'datafile'}
            ],
            datepickers : {
                dateformat : 'yyyy-MM-dd',
                startDate: false,
                endDate: false
            }
        };

        vm.open = function($event, datePicker) {
            $event.preventDefault();
            $event.stopPropagation();
            vm.closeAll();

            vm.init.datepickers[datePicker] = true;
        };

        vm.closeAll = function() {
            vm.init.datepickers.startDate = false;
            vm.init.datepickers.endDate = false;
        };

        /**
         * Perform a search
         */
        vm.search = function() {
            var params = searchFormToQueryParams($filter, vm.form);

            //TODO why need reload:true to work
            $state.go('home.browse.facility.search', params, {'reload' : false});


        };


        //$scope.initialise();
    }




    /**
     * Format the values passed from the search form.
     * The date values are formated to something sensible using the angular filter functions
     *
     * @param $filter angular filter helper
     * @param data the data values
     * @returns
     */
    function searchFormToQueryParams($filter, data) {
        var params = {};

        if (typeof data !== 'undefined') {
            if ('meta' in data) {
                params.meta = data.meta;
            }

            if ('query' in data) {
                params.query =  data.query;
            }

            if ('type' in data) {
                params.type =  data.type;
            }

            if ('facility' in data) {
                params.facility =  data.facility;
            }

            if ('startDate' in data) {
                params.startDate = $filter('date')(data.startDate, 'yyyy-MM-dd');
            }

            if ('endDate' in data) {
                params.endDate = $filter('date')(data.endDate, 'yyyy-MM-dd');
            }

            return params;
        }

        return data;
    }
})();
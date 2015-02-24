'use strict';

var SearchFormController = ['$rootScope', '$scope', '$state', '$stateParams', '$log', '$filter', function($rootScope, $scope, $state, $stateParams, $log, $filter) {

    $log.warn($state);

    if (typeof $state.params !== "undefined") {
        $scope.searchForm = $state.params;
    }
    /*
    $scope.initialise = function() {
        $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams){
                $log.warn(fromParams);

                $scope.searchForm = fromParams;

                $log.warn($stateParams);


            });
    };
    */

    //set select options
    $scope.facilityItems = [
            {"name" : "facility 1", "id" : "facility one"},
            {"name" : "facility 2", "id" : "facility two"},
            {"name" : "facility 3", "id" : "facility three"},
    ];

    $scope.searchTypeItems = [
            {"name" : "Investigation", "id" : "investigation"},
            {"name" : "Dataset", "id" : "dataset"},
            {"name" : "Datafile", "id" : "datafile"},
    ];

    $scope.formData = [];

    //datepicker date format
    $scope.format = 'yyyy-MM-dd';

    $scope.datepickers = {
            startDate: false,
            endDate: false
    };

    $scope.open = function($event, datePicker) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.closeAll();

        $scope.datepickers[datePicker] = true;
    };

    $scope.closeAll = function() {
        $scope.datepickers.startDate = false;
        $scope.datepickers.endDate = false;
    };

    /**
     * Perform a search
     */
    $scope.search = function() {
        var params = searchFormToQueryParams($filter, $scope.searchForm);
        $state.go("home.browse.meta-1", params);
    };


    $scope.initialise();
}];


angular.module('angularApp').controller('SearchFormController', SearchFormController);


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

    if (typeof data !== "undefined") {
        if ("query" in data) {
            params["query"] =  data["query"];
        }

        if ("type" in data) {
            params["type"] =  data["type"];
        }

        if ("facility" in data) {
            params["facility"] =  data["facility"];
        }

        if ("startDate" in data) {
            params["startDate"] = $filter('date')(data["startDate"], "yyyy-MM-dd");
        }

        if ("endDate" in data) {
            params["endDate"] = $filter('date')(data["endDate"], "yyyy-MM-dd");
        }

        return params;
    }

    return data;

}





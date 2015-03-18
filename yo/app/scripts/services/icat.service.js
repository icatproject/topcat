'use strict';

angular.
    module('angularApp').factory('ICATService', ICATService);

ICATService.$inject = ['$http', '$q'];

/*jshint -W098 */
function ICATService($http, $q) {
    var data = {};

    /** get facilities */
    data.getFacilties = function(facilityId) {
        return $http.get('data/facilities.json');
    };

    /** get instruments **/
    data.getInstrumentsByFacilityId = function(facilityId){
        return $http.get('data/instruments.json');
    };


    /** get cycles */
    data.getCycles = function(facilityId) {
        return $http.get('data/cycles.json');
    };

    data.getCyclesByInstrumentId = function(facilityId, instrumentId) {
        return $http.get('data/cycles.json');
    };


    /** get investigations */
    data.getInvestigations = function(facilityId) {
        return $http.get('data/investigations.json');
    };

    data.getInvestigationsByCycleId = function(facilityId, cycleId) {
        return $http.get('data/investigations-small-set.json');
    };

    data.getInvestigationsByInstrumentId = function(facilityId, instrumentId) {
        var def = $q.defer();

        $http.get('data/investigations-small-set.json')
            .success(function(data) {
                    def.resolve(data);
            })
            .error(function() {
                def.reject("Failed to get data");
            });

        return def.promise;

        //$http.get('data/investigations-small-set.json');
    };

    data.getInvestigationsByInstrumentIdByCycleId = function(facilityId, instrumentId, cycleId) {
        return $http.get('data/investigations.json');
    };


    /** get datasets **/
    data.getDatasetByFacilityId = function(facilityId){
        return $http.get('data/dataset.json');
    };

    data.getDatasetByInvestigationId = function(facilityId, investigationId) {
        return $http.get('data/dataset.json');
    };


    /** get datafiles **/
    data.getDatafiles = function(facilityId) {
        return $http.get('data/datafiles.json');
    };

    data.getDatafilesByDatasetId = function(facilityId, datasetId) {
        return $http.get('data/dataset.json');
    };

    data.getDatafilesByInvestigationId = function(facilityId, investigationId) {
        return $http.get('data/datafiles.json');
    };

    return data;
}





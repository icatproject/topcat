'use strict';

angular.
    module('angularApp').factory('DataManager', DataManager);

DataManager.$inject = ['$http', '$q', 'ICATService'];

/*jshint -W098 */
function DataManager($http, $q, ICATService) {
    var manager = {};

    function MyException(message) {
      this.name = 'Data Error';
      this.message = message;
    }
    MyException.prototype = new Error();
    MyException.prototype.constructor = MyException;



    /** login **/
    manager.login = function() {
        var def = $q.defer();

            ICATService.login()
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function(error) {
                    def.reject('Failed to login');
                    throw new MyException('Failed to login:' + error);
                });

            return def.promise;
    };

    /** login **/
    manager.getVersion = function() {
        var def = $q.defer();

            ICATService.getVersion()
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function(error) {
                    def.reject('Failed to get server version');
                    throw new MyException('Failed to get server version. ' + error);
                });

            return def.promise;
    };




    /** get facilities */
    manager.getFacilities = function() {
        var def = $q.defer();

            ICATService.getFacilities()
                .success(function(data) {
                    data = _.pluck(data, 'Facility');
                    def.resolve(data);
                })
                .error(function(error) {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    /** get instruments **/
    manager.getInstruments = function(facilityId){
        var def = $q.defer();

            ICATService.getInstruments(facilityId)
                .success(function(data) {
                    data = _.pluck(data, 'Instrument');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    /** get cycles */
    manager.getCycles = function(facilityId) {
        var def = $q.defer();

            ICATService.getCycles(facilityId)
                .success(function(data) {
                    data = _.pluck(data, 'FacilityCycle');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getCyclesByInstrumentId = function(facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getCyclesByInstrumentId(facilityId, instrumentId)
                .success(function(data) {
                    data = _.pluck(data, 'FacilityCycle');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    /** get investigations */
    manager.getInvestigations = function(facilityId) {
        var def = $q.defer();

            ICATService.getInvestigations(facilityId)
                .success(function(data) {
                    data = _.pluck(data, 'Investigation');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getInvestigationsByCycleId = function(facilityId, cycleId) {
        var def = $q.defer();

            ICATService.getInvestigationsByCycleId(facilityId, cycleId)
                .success(function(data) {
                    data = _.pluck(data, 'Investigation');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getInvestigationsByInstrumentId = function(facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getInvestigationsByInstrumentId(facilityId, instrumentId)
                .success(function(data) {
                    data = _.pluck(data, 'Investigation');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getInvestigationsByInstrumentIdByCycleId = function(facilityId, instrumentId, cycleId) {
        var def = $q.defer();

            ICATService.getInvestigationsByInstrumentIdByCycleId(facilityId, instrumentId, cycleId)
                .success(function(data) {
                    data = _.pluck(data, 'Investigation');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    /** get datasets **/
    manager.getDatasets = function(facilityId){
        var def = $q.defer();

            ICATService.getDatasets(facilityId)
                .success(function(data) {
                    data = _.pluck(data, 'Dataset');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    manager.getDatasetsByInstrumentId = function(facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getDatasetsByInstrumentId(facilityId, instrumentId)
                .success(function(data) {
                    data = _.pluck(data, 'Dataset');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getDatasetsByInvestigationId = function(facilityId, investigationId) {
        var def = $q.defer();

            ICATService.getDatasetsByInvestigationId(facilityId, investigationId)
                .success(function(data) {
                    data = _.pluck(data, 'Dataset');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    /** get datafiles **/
    manager.getDatafiles = function(facilityId) {
        var def = $q.defer();

            ICATService.getDatafiles(facilityId)
                .success(function(data) {
                    data = _.pluck(data, 'Datafile');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    manager.getDatafilesByDatasetId = function(facilityId, datasetId) {
        var def = $q.defer();

            ICATService.getDatafilesByDatasetId(facilityId, datasetId)
                .success(function(data) {
                    data = _.pluck(data, 'Datafile');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    manager.getDatafilesByInstrumentId = function(facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getDatafilesByInstrumentId(facilityId, instrumentId)
                .success(function(data) {
                    data = _.pluck(data, 'Datafile');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };


    manager.getDatafilesByInvestigationId = function(facilityId, investigationId) {
        var def = $q.defer();

            ICATService.getDatafilesByInvestigationId(facilityId, investigationId)
                .success(function(data) {
                    data = _.pluck(data, 'Datafile');
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    throw new MyException('Failed to retrieve data from server');
                });

            return def.promise;
    };

    return manager;
}





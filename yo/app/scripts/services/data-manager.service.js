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
    manager.getFacilities = function(sessionId) {
        var def = $q.defer();

            ICATService.getFacilities(sessionId)
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
    manager.getInstruments = function(sessionId, facilityId){
        var def = $q.defer();

            ICATService.getInstruments(sessionId, facilityId)
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
    manager.getCycles = function(sessionId, facilityId) {
        var def = $q.defer();

            ICATService.getCycles(sessionId, facilityId)
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

    manager.getCyclesByInstrumentId = function(sessionId, facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getCyclesByInstrumentId(sessionId, facilityId, instrumentId)
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
    manager.getInvestigations = function(sessionId, facilityId) {
        var def = $q.defer();

            ICATService.getInvestigations(sessionId, facilityId)
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

    manager.getInvestigationsByCycleId = function(sessionId, facilityId, cycleId) {
        var def = $q.defer();

            ICATService.getInvestigationsByCycleId(sessionId, facilityId, cycleId)
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

    manager.getInvestigationsByInstrumentId = function(sessionId, facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getInvestigationsByInstrumentId(sessionId, facilityId, instrumentId)
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

    manager.getInvestigationsByInstrumentIdByCycleId = function(sessionId, facilityId, instrumentId, cycleId) {
        var def = $q.defer();

            ICATService.getInvestigationsByInstrumentIdByCycleId(sessionId, facilityId, instrumentId, cycleId)
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
    manager.getDatasets = function(sessionId, facilityId){
        var def = $q.defer();

            ICATService.getDatasets(sessionId, facilityId)
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


    manager.getDatasetsByInstrumentId = function(sessionId, facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getDatasetsByInstrumentId(sessionId, facilityId, instrumentId)
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

    manager.getDatasetsByInvestigationId = function(sessionId, facilityId, investigationId) {
        var def = $q.defer();

            ICATService.getDatasetsByInvestigationId(sessionId, facilityId, investigationId)
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
    manager.getDatafiles = function(sessionId, facilityId) {
        var def = $q.defer();

            ICATService.getDatafiles(sessionId, facilityId)
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

    manager.getDatafilesByDatasetId = function(sessionId, facilityId, datasetId) {
        var def = $q.defer();

            ICATService.getDatafilesByDatasetId(sessionId, facilityId, datasetId)
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


    manager.getDatafilesByInstrumentId = function(sessionId, facilityId, instrumentId) {
        var def = $q.defer();

            ICATService.getDatafilesByInstrumentId(sessionId, facilityId, instrumentId)
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


    manager.getDatafilesByInvestigationId = function(sessionId, facilityId, investigationId) {
        var def = $q.defer();

            ICATService.getDatafilesByInvestigationId(sessionId, facilityId, investigationId)
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





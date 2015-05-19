'use strict';

angular.
    module('angularApp').factory('DataManager', DataManager);

DataManager.$inject = ['$http', '$q', 'ICATService', 'APP_CONFIG', 'Config', '$log'];

/*jshint -W098 */
function DataManager($http, $q, ICATService, APP_CONFIG, Config, $log) {
    var manager = {};

    function MyException(message) {
      this.name = name;
      this.message = message;
    }
    MyException.prototype = new Error();
    MyException.prototype.constructor = MyException;


    /**
     * Get the session value for the facility that was passed
     * @param  {[type]} session  [description]
     * @param  {[type]} facility [description]
     * @return {[type]}          [description]
     */
    function getSessionValueForFacility(sessions, facility) {
        return sessions[facility.keyName].sessionId;
    }


    /**
     * Perform a login
     * @param  {[type]} facility   [description]
     * @param  {[type]} credential [description]
     * @return {[type]}            [description]
     */
    manager.login = function(facility, credential) {
        var def = $q.defer();

        ICATService.login(facility, credential)
            .success(function(data) {
                def.resolve(data);
            })
            .error(function(error, status) {
                $log.debug('login status', status);

                def.reject('Failed to login');
                throw new MyException('Failed to login:' + error);
            });

        return def.promise;
    };

    /**
     * Perform a logout
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.logout = function(sessions, facility, options) {
        $log.debug('DataManager.logout called for facility' , facility);
        $log.debug('DataManager.logout called for sessions' , sessions);

        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.logout(sessionId, facility, options)
            .success(function(data) {
                def.resolve(data);
            })
            .error(function(error, status) {
                $log.debug('logout status', status);

                def.reject('Failed to login');
                throw new MyException('Failed to login:' + error);
            });

        return def.promise;
    };

    /**
     * Get the icat version of a facility
     * @param  {Object} facility config object
     * @return {Object} a promise containing the version number
     */
    manager.getVersion = function(facility) {
        var def = $q.defer();

        ICATService.getVersion(facility)
            .success(function(data) {
                def.resolve(data);
            })
            .error(function(error) {
                def.reject('Failed to get server version');
                throw new MyException('Failed to get server version. ' + error);
            });

        return def.promise;
    };

    /**
     * Get a specific entity
     * @param  {[type]} sessions   [description]
     * @param  {[type]} facility   [description]
     * @param  {[type]} entityType [description]
     * @param  {[type]} entityId   [description]
     * @param  {[type]} options    [description]
     * @return {[type]}            [description]
     */
    manager.getEntityById = function(sessions, facility, entityType, entityId, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getEntityById(sessionId, facility, entityType, entityId, options)
            .success(function(data) {
                def.resolve(data);
            })
            .error(function() {
                def.reject('Failed to retrieve data from server');
                throw new MyException('Failed to retrieve data from server');
            });

        return def.promise;
    };


    /**
     * Get the list of facilities
     * @TODO doesn't make sense to pass a facility to get a list of faciltities
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @return {[type]}          [description]
     */
    /*manager.getFacilities = function(sessions, facility) {
        var sessionId = getSessionValueForFacility(sessions, facility.name);
        var def = $q.defer();

        ICATService.getFacilities(sessionId, facility)
            .success(function(data) {
                def.resolve(data);
            })
            .error(function(error) {
                def.reject('Failed to retrieve data');
                throw new MyException('Failed to retrieve data from server');
            });

        return def.promise;
    };*/

    /**
     * Get the instruments in facility
     * @param  {Object} sessions session object containing logged in sessions
     * @param  {Object} facility the facility object
     * @return {Object}          a promise containing the list of instruments
     */
    manager.getInstruments = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getInstruments(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Instrument');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the facility cycles in a facility
     * @param  {Object} sessions session object containing logged in sessions
     * @param  {Object} facility the facility object
     * @return {Object}          a promise containing the list of cycles
     */
    manager.getCycles = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getCycles(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'FacilityCycle');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the facility cycles for an intrument in a facility
     * @param  {Object} sessions session object containing logged in sessions
     * @param  {Object} facility the facility object
     * @param  {int} instrumentId the id of the instrument
     * @return {Object}          a promise containing the list of cycles
     */
    manager.getCyclesByInstrumentId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getCyclesByInstrumentId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'FacilityCycle');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the investigation in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getInvestigations = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getInvestigations(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Investigation');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the investigations for a cycle in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getInvestigationsByCycleId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getInvestigationsByCycleId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Investigation');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };

    /**
     * Get the investigations for an instrument in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getInvestigationsByInstrumentId = function(sessions, facility, options) {
        //$log.debug('manager.getInvestigationsByInstrumentId options', options);

        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getInvestigationsByInstrumentId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Investigation');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };

    /**
     * Get the investigations for an instrument and cycle in a facility
     * @param  {[type]} sessions     [description]
     * @param  {[type]} facility     [description]
     * @param  {[type]} instrumentId [description]
     * @param  {[type]} cycleId      [description]
     * @param  {[type]} options      [description]
     * @return {[type]}              [description]
     */
    manager.getInvestigationsByInstrumentIdByCycleId = function(sessions, facility, instrumentId, cycleId, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getInvestigationsByInstrumentIdByCycleId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Investigation');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the datasets in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatasets = function(sessions, facility, options){
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatasets(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Dataset');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the datasets for an instrument in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatasetsByInstrumentId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatasetsByInstrumentId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Dataset');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };

    /**
     * Get the datasets for an investigation in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatasetsByInvestigationId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatasetsByInvestigationId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Dataset');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the datafiles in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatafiles = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatafiles(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Datafile');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get the datafiles for a dataset in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatafilesByDatasetId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatafilesByDatasetId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Datafile');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };

    /**
     * Get the datafiles for an instrument in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatafilesByInstrumentId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatafilesByInstrumentId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Datafile');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };

    /**
     * Get the datafile for an investigation in a facility
     * @param  {[type]} sessions [description]
     * @param  {[type]} facility [description]
     * @param  {[type]} options  [description]
     * @return {[type]}          [description]
     */
    manager.getDatafilesByInvestigationId = function(sessions, facility, options) {
        var sessionId = getSessionValueForFacility(sessions, facility);
        var def = $q.defer();

        ICATService.getDatafilesByInvestigationId(sessionId, facility, options).then(function(data) {
            var result = {};
            result.data = _.pluck(data[0].data, 'Datafile');
            result.totalItems = data[1].data[0];

            def.resolve(result);
        }, function(error){
            def.reject('Failed to retrieve data');
            throw new MyException('Failed to retrieve data from server');
        });

        return def.promise;
    };


    /**
     * Get data based on the current ui-route
     * @param  {[type]} currentRouteSegment [description]
     * @param  {[type]} facilityName        [description]
     * @param  {[type]} sessions            [description]
     * @param  {[type]} $stateParams        [description]
     * @param  {[type]} options             [description]
     * @return {[type]}                     [description]
     */
    manager.getData = function(currentRouteSegment, facilityName, sessions, $stateParams, options) {
        var facility = Config.getFacilityByName(APP_CONFIG, facilityName);

        switch (currentRouteSegment) {
            case 'facility-instrument':
                $log.debug('function called: getInstruments');

                return this.getInstruments(sessions, facility, options);
            case 'facility-cycle':
                $log.debug('function called: getCycles');

                return this.getCyclesByFacilityId(sessions, facility, options);
            case 'facility-investigation':
                $log.debug('function called: getInvestigations');

                return this.getInvestigations(sessions, facility, options);
            case 'facility-dataset':
                $log.debug('function called: getDatasets');

                return this.getDatasets(sessions, facility, options);
            case 'facility-datafile':
                $log.debug('function called: getDatafiles');

                return this.getDatafiles(sessions, facility, options);
            case 'instrument-cycle':
                $log.debug('function called: getCyclesByInstruments');
                options.instrumentId = $stateParams.id;

                return this.getCyclesByInstruments(sessions, facility, options);
            case 'instrument-investigation':
                $log.debug('function called: getInvestigationsByInstrumentId');
                options.instrumentId = $stateParams.id;

                return this.getInvestigationsByInstrumentId(sessions, facility, options);
            case 'instrument-dataset':
                $log.debug('function called: getDatasetsByInstrumentId');
                options.instrumentId = $stateParams.id;

                return this.getDatasetsByInstrumentId(sessions, facility, options);
            case 'instrument-datafile':
                $log.debug('function called: getDatafilesByInstrumentId');
                options.instrumentId = $stateParams.id;

                return this.getDatafilesByInstrumentId(sessions, facility, options);
            case 'cycle-instrument':
                $log.debug('function called: getInstrumentsByCycleId');
                options.cycleId = $stateParams.id;

                return this.getInstrumentsByCycleId(sessions, facility, options);
            case 'cycle-investigation':
                $log.debug('function called: getCyclesByInvestigationId');
                options.cycleId = $stateParams.id;

                return this.getInvestigationsByCycleId(sessions, facility, options);
            case 'cycle-dataset':
                $log.debug('function called: getDatasetsByCycleId');
                options.cycleId = $stateParams.id;

                return this.getDatasetsByCycleId(sessions, facility, options);
            case 'cycle-datafile':
                $log.debug('function called: getDatafilesByCycleId');
                options.cycleId = $stateParams.id;

                return this.getDatafilesByCycleId(sessions, facility, options);
            case 'investigation-dataset':
                $log.debug('function called: getDatasetsByInvestigationId');
                options.investigationId = $stateParams.id;

                return this.getDatasetsByInvestigationId(sessions, facility, options);
            case 'investigation-datafile':
                $log.debug('function called: getDatafilesByInvestigationId');
                options.investigationId = $stateParams.id;

                return this.getDatafilesByInvestigationId(sessions, facility, options);
            case 'dataset-datafile':
                $log.debug('function called: getDatafilesByDatasetId');
                options.datasetId = $stateParams.id;

                return this.getDatafilesByDatasetId(sessions, facility, options);
            default:
                $log.debug('function called: default');
                return;
        }
    };


    return manager;
}

(function() {
    'use strict';

    angular.
        module('angularApp').factory('ICATService', ICATService);

    ICATService.$inject = ['$http', '$q', 'APP_CONFIG', 'Config', '$rootScope', 'ICATQueryBuilder', '$log'];

    /*jshint -W098 */
    function ICATService($http, $q, APP_CONFIG, Config, $rootScope, ICATQueryBuilder, $log) {
        //private var and methods
        var data = {};

        var getPromise = function(mySessionId, facility, params) {
            var RESTAPI = facility.icatUrl + '/icat/entityManager';

            var itemsQueryParams = {
                params : {
                    sessionId : mySessionId,
                    query : params.query,
                    server : facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            var countQueryParams = {
                params : {
                    sessionId : mySessionId,
                    query : params.countQuery,
                    server : facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            $log.debug('params', params);

            var deferred = $q.defer();
            var asyncCalls = [
                $http.get(RESTAPI, itemsQueryParams),
                $http.get(RESTAPI, countQueryParams)
            ];

            $q.all(asyncCalls).then(
                function(data) {
                    $log.debug('promise data', data);

                    deferred.resolve(data);
                }, function(errors) {
                    deferred.reject(errors);
                }, function(updates) {
                    deferred.update(updates);
                }
            );

            return deferred.promise;
        };

        /**
         * Parse options object and append it to the JPQL query.
         *
         * The option object expected is:
         *
         * {
         *     sort: {
         *         sortBy: 'ds.name',
         *         orderBy: 'asc',
         *     },
         *     limit: {
         *         offset: 0,
         *         maxRows: 100
         *     },
         *     include: [
         *         'ds.datafiles',
         *         'ds.datafiles AS df',
         *         'df.parameters'
         *     ]
         *
         * }
         *
         * @param  {[type]} option [description]
         * @return {[type]}        [description]
         */
        var appendConciseInclude = function(options, query) {
            if (!angular.isDefined(options)) {
                return query;
            }

            var opt = '';

            if (angular.isDefined(options.include)) {
                if (! (options.include instanceof Array)) {
                    throw {name : 'BAD_OPTION', message: 'INVALID OPTION: include must be an array'};
                }

                if (options.include.length === 0) {
                    throw {name : 'BAD_OPTION', message: 'INVALID OPTION: include must contain at least one element'};
                }

                var include = options.include.join();

                if(opt.length !== 0) {
                    opt = opt + ' ';
                }

                opt = opt + 'INCLUDE ' + include;
            }

            //prepend starting space to make it easier to append to query
            if(opt.length !== 0) {
                opt = ' ' + opt;
            }

            query = query + opt;
            return query;
        };



        //public methods
        /**
         * Perform a login
         * @param  {[type]} facility   [description]
         * @param  {[type]} credential [description]
         * @return {[type]}            [description]
         */
        data.login = function(facility, credential) {
            //$log.debug('login called for facility' , facility);

            var url = facility.icatUrl + '/icat/session';

            var credentialObj = {
                plugin: credential.plugin,
                credentials: [
                    {username: credential.credentials.username},
                    {password: credential.credentials.password}
                ]
            };

            var data = { json : JSON.stringify(credentialObj) };

            var options = {
                'headers' :{
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                'info': {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                },
                'transformRequest': function(obj) {
                    var str = [];
                    for(var p in obj) {
                        if (obj.hasOwnProperty(p)) {
                            str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]));
                        }
                    }

                    return str.join('&');
                }
            };

            return $http.post(url, data, options);
        };

        /**
         * Perform a logout
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.logout = function(mySessionId, facility, options) { //jshint ignore:line
            //$log.debug('icatservice.logout called for facility' , facility);

            var url = facility.icatUrl + '/icat/session/' + mySessionId;
            var params = {
                params : {
                    server : facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.delete(url, params);
        };


        /**
         * Get session
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getSession = function(mySessionId, facility, options) { //jshint ignore:line
            $log.debug('icatservice.getSession called for facility', mySessionId, facility, options);

            var url = facility.icatUrl + '/icat/session/' + mySessionId;
            var params = {
                params : {
                    server : facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };

        /**
         * Get ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        data.getVersion = function(facility) {
            var url = facility.icatUrl + '/icat/version';
            var params = {
                    params : {
                        server : facility.icatUrl
                    },
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    }
                };

            return $http.get(url, params);
        };

        /**
         * refresh the current session ICAT version
         * @param  {[type]} facility [description]
         * @return {[type]}          [description]
         */
        data.refreshSession = function(mySessionId, facility) {
            var url = facility.icatUrl + '/icat/session/' + mySessionId + '?server=' + facility.icatUrl;
            var params = {
                    info : {
                        'facilityKeyName' : facility.facilityName,
                        'facilityTitle' : facility.title
                    }
                };
            $log.debug('icatservice refresh session');
            return $http.put(url, params);
        };

        /**
         * Get a specific entity
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} entityType  [description]
         * @param  {[type]} entityId    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getEntityById = function(mySessionId, facility, entityType, entityId, options) {
            var url = facility.icatUrl + '/icat/entityManager';
            var entityIcatName = entityType.charAt(0).toUpperCase() + entityType.slice(1);
            var query = entityIcatName ;
            query = appendConciseInclude(options, query);
            query += ' [id=' + entityId + ']';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    server : facility.icatUrl
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };


        /** get facilities */
        /*data.getFacilities = function(mySessionId, facility, options) {

            var url = facility.icatUrl + '/icat/entityManager';
            var query = 'SELECT f FROM Facility f';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Facility'
                },
                info : {
                    'facilityKeyName' : facility.facilityName,
                    'facilityTitle' : facility.title
                }
            };

            return $http.get(url, params);
        };
    */

        /**
         * Returns a promise with a list of instruments
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getInstruments = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getInstruments(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        /**
         * Returns a promise with a list of facility cycles
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getFacilityCycles = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getFacilityCycles(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of facility cycles from an instrument id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getFacilityCyclesByInstrumentId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getFacilityCyclesByInstrumentId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        data.getProposals = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getProposals(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        /**
         * Returns a promise with a list of investigations
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getInvestigations = function(mySessionId, facility, options) {
            $log.debug('getInvestigations options', options);
            var params = ICATQueryBuilder.getInvestigations(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of instruments from a facility cycle id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getInvestigationsByFacilityCycleId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getInvestigationsByFacilityCycleId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        data.getDatasetsByFacilityCycleId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatasetsByFacilityCycleId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        data.getDatafilesByFacilityCycleId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatafilesByFacilityCycleId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        data.getProposalsByInstrumentId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getProposalsByInstrumentId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        data.getProposalsByFacilityCycleId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getProposalsByFacilityCycleId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        data.getInvestigationsByProposalId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getInvestigationsByProposalId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of investigations from an instrument id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getInvestigationsByInstrumentId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getInvestigationsByInstrumentId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of investigations from an instrument id and facility cycle id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getInvestigationsByInstrumentIdByFacilityCycleId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getInvestigationsByInstrumentIdByFacilityCycleId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        /**
         * Returns a promise with a list of datasets
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatasets = function(mySessionId, facility, options){
            var params = ICATQueryBuilder.getDatasets(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of datasets from an instrument id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatasetsByInstrumentId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatasetsByInstrumentId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        /**
         * Returns a promise with a list of datasets from an investigation id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatasetsByInvestigationId = function(mySessionId, facility, options) {
            $log.debug('getDatasetsByInvestigationId options', options);
            var params = ICATQueryBuilder.getDatasetsByInvestigationId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };


        /**
         * Returns a promise with a list of datafiles
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatafiles = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatafiles(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of datafiles from an instrument id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatafilesByInstrumentId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatafilesByInstrumentId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);

        };

        /**
         * Returns a promise with a list of datafiles from an investigation id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatafilesByInvestigationId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatafilesByInvestigationId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        /**
         * Returns a promise with a list of datafiles from a dataset id
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} options     [description]
         * @return {[type]}             [description]
         */
        data.getDatafilesByDatasetId = function(mySessionId, facility, options) {
            var params = ICATQueryBuilder.getDatafilesByDatasetId(mySessionId, facility, options);

            return getPromise(mySessionId, facility, params);
        };

        return data;
    }
})();

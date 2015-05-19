'use strict';

angular.
    module('angularApp').factory('ICATService', ICATService);

ICATService.$inject = ['$http', '$q', 'APP_CONFIG', 'Config', '$rootScope', 'ICATQueryBuilder', '$log'];

/*jshint -W098 */
function ICATService($http, $q, APP_CONFIG, Config, $rootScope, ICATQueryBuilder, $log) {
    //private var and methods
    var data = {};

    var ICATDATAPROXYURL = Config.getSiteConfig(APP_CONFIG).icatDataProxyHost;
    var RESTAPI = ICATDATAPROXYURL + '/icat/entityManager';

    var getPromise = function(mySessionId, facility, params) {
        var itemsQueryParams = {
            params : {
                sessionId : encodeURIComponent(mySessionId),
                query : params.query,
                server : encodeURIComponent(facility.icatUrl)
            },
            headers : {
                'facilityKeyName' : facility.keyName,
                'facilityTitle' : facility.title
            }
        };

        var countQueryParams = {
            params : {
                sessionId : encodeURIComponent(mySessionId),
                query : params.countQuery,
                server : encodeURIComponent(facility.icatUrl)
            },
            headers : {
                'facilityKeyName' : facility.keyName,
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
                console.log('promise data', data);

                deferred.resolve(data);
            }, function(errors) {
                deferred.reject(errors);
            }, function(updates) {
                deferred.update(updates);
            }
        );

        return deferred.promise;
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

        var url = ICATDATAPROXYURL + '/icat/session';
        var data = {
            server : facility.icatUrl,
            plugin : credential.plugin,
            username : credential.credentials.username,
            password : credential.credentials.password
        };

        var options = {
            'headers': {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'facilityKeyName' : facility.keyName,
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
    data.logout = function(mySessionId, facility, options) {
        //$log.debug('icatservice.logout called for facility' , facility);

        var url = ICATDATAPROXYURL + '/icat/session/' + mySessionId;
        var params = {
            params : {
                server : encodeURIComponent(facility.icatUrl)
            },
            headers : {
                'facilityKeyName' : facility.keyName,
                'facilityTitle' : facility.title
            }
        };

        var data = {
            server : facility.icatUrl
        };

        return $http.delete(url, params);
    };

    /**
     * Get ICAT version
     * @param  {[type]} facility [description]
     * @return {[type]}          [description]
     */
    data.getVersion = function(facility) {
        var url = ICATDATAPROXYURL + '/icat/version';
        var params = {
                params : {
                    server : encodeURIComponent(facility.icatUrl)
                },
                headers : {
                    'facilityKeyName' : facility.keyName,
                    'facilityTitle' : facility.title
                }
            };

        return $http.get(url, params);
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
        var url = ICATDATAPROXYURL + '/icat/entityManager';
        var entityIcatName = entityType.charAt(0).toUpperCase() + entityType.slice(1);
        var query = entityIcatName ;
        //query = appendOptionsToQuery(options, query);
        query += ' [id=' + entityId + ']';

        var params = {
            params : {
                sessionId : mySessionId,
                query : query,
                entity : entityIcatName,
                server : facility.icatUrl
            }
        };

        return $http.get(url, params);
    };


    /** get facilities */
    /*data.getFacilities = function(mySessionId, facility, options) {

        var url = ICATDATAPROXYURL + '/icat/entityManager';
        var query = 'SELECT f FROM Facility f';

        var params = {
            params : {
                sessionId : mySessionId,
                query : query,
                entity : 'Facility'
            },
            headers : {
                'facilityKeyName' : facility.keyName,
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
    data.getCycles = function(mySessionId, facility, options) {
        var params = ICATQueryBuilder.getCycles(mySessionId, facility, options);

        return getPromise(mySessionId, facility, params);
    };

    /**
     * Returns a promise with a list of facility cycles from an instrument id
     * @param  {[type]} mySessionId [description]
     * @param  {[type]} facility    [description]
     * @param  {[type]} options     [description]
     * @return {[type]}             [description]
     */
    data.getCyclesByInstrumentId = function(mySessionId, facility, options) {
        var params = ICATQueryBuilder.getCyclesByInstrumentId(mySessionId, facility, options);

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
    data.getInvestigationsByCycleId = function(mySessionId, facility, options) {
        var params = ICATQueryBuilder.getInvestigationsByCycleId(mySessionId, facility, options);

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
    data.getInvestigationsByInstrumentIdByCycleId = function(mySessionId, facility, options) {
        var params = ICATQueryBuilder.getInvestigationsByInstrumentIdByCycleId(mySessionId, facility, options);

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





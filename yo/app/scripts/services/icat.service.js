'use strict';

angular.
    module('angularApp').factory('ICATService', ICATService);

ICATService.$inject = ['$http', '$q', 'APP_CONFIG', '$rootScope'];

/*jshint -W098 */
function ICATService($http, $q, APP_CONFIG, $rootScope) {

    var useFileForData = angular.isDefined(APP_CONFIG.site.useFileForData) ? APP_CONFIG.site.useFileForData : false;
    var useFileForSession = angular.isDefined(APP_CONFIG.site.useFileForSession) ? APP_CONFIG.site.useFileForSession : false;
    var data = {};
    //var mySessionId = '297b3916-2080-4e58-9ff5-8a4383be147e';
    var LIMIT = ' LIMIT 0, 1000'; //apply a limit to a query. FOR DEBUG ONLY
    var ICATDATAPROXYURL = 'https://localhost:3001';

    console.log('useFileForData:' + useFileForData);
    console.log('useFileForSession:' + useFileForSession);

    console.log('ICATService session', $rootScope.session);


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
    var appendOptionsToQuery = function(options, query) {
        if (!angular.isDefined(options)) {
            return query;
        }

        var opt = '';

        if (angular.isDefined(options.sort)) {
            if (angular.isDefined(options.sort.sortBy) && angular.isDefined(options.sort.orderBy)) {
                if (['ASC','DESC'].indexOf(options.sort.orderBy.toUpperCase()) < 0) {
                    throw {name : 'BAD_OPTION', message: 'INVALID OPTION: orderBy must be asc or desc'};
                }

                opt = opt + 'ORDER BY ' + options.sort.sortBy + ' ' + options.sort.orderBy;
            }
        }

        if (angular.isDefined(options.include)) {
            console.log('optionsToQuery include', options.include);
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

        if (angular.isDefined(options.limit)) {
            if (angular.isDefined(options.limit.offset) && angular.isDefined(options.limit.maxRows)) {
                //add space if already has order options
                if(opt.length !== 0) {
                    opt = opt + ' ';
                }

                opt = opt + 'LIMIT ' + options.limit.offset.toString() + ',' + options.limit.maxRows.toString();
            }
        }

        //prepend starting space to make it easier to append to query
        if(opt.length !== 0) {
            opt = ' ' + opt;
        }

        query = query + opt;
        return query;
    };


    data.login = function(facility, credential) {
        console.log('login called for facility' , facility);
        if (useFileForSession) {
            console.log('login returning json file');
            return $http.get('data/icatapi-session-multi.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/session';
            var data = {
                server : facility.icatUrl,
                plugin : credential.plugin,
                username : credential.credentials.username,
                password : credential.credentials.password
            };

            var options = {
                'headers': {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
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
        }
    };

    data.getVersion = function(facility) {
        var url = ICATDATAPROXYURL + '/icat/version';

        return $http.get(url, {
                params: {
                    server: facility.icatUrl
                }
            });
    };


    /** get facilities */
    data.getFacilities = function(mySessionId, facility, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-facility.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT f FROM Facility f';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Facility'
                }

            };

            return $http.get(url, params);
        }
    };

    /** get instruments **/
    data.getInstruments = function(mySessionId, facility, options) {
        console.log('getInstruments session: ', mySessionId);
        console.log('getInstruments options: ', options);

        if (useFileForData) {
            return $http.get('data/icatapi-instruments.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT ins FROM Instrument ins, ins.facility f where f.id = ' + facility.facilityId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Instrument',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    /** get cycles */
    data.getCycles = function(mySessionId, facility, options) {
        return $http.get('data/cycles.json');
    };

    data.getCyclesByInstrumentId = function(mySessionId, facility, instrumentId, options) {
        return $http.get('data/cycles.json');
    };


    /** get investigations */
    data.getInvestigations = function(mySessionId, facility, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-investigations.json');
        } else {

            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT i FROM Investigation i, i.facility f where f.id = 1 LIMIT 0, 5000';
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Investigation',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };

    data.getInvestigationsByCycleId = function(mySessionId, facility, cycleId, options) {
        return $http.get('data/icatapi-investigations-5-items.json');
    };

    data.getInvestigationsByInstrumentId = function(mySessionId, facility, instrumentId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-investigations-5-items.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT i FROM Investigation i, i.investigationInstruments ii, ii.instrument ins where ins.id = ' + instrumentId;
            query = appendOptionsToQuery(options, query);

            console.log('query', query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Investigation',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }


    };

    data.getInvestigationsByInstrumentIdByCycleId = function(mySessionId, facility, instrumentId, cycleId, options) {

        return $http.get('data/icatapi-investigations-5-items.json');
    };


    /** get datasets **/
    data.getDatasets = function(mySessionId, facility, options){
        console.log('facilityId: ' + facility.facilityId);

        if (useFileForData) {
            return $http.get('data/icatapi-datasets.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT d FROM Dataset d, d.investigation i, i.facility f where f.id = ' + facility.facilityId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Dataset',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    data.getDatasetsByInstrumentId = function(mySessionId, facility, instrumentId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT d FROM Dataset d, d.investigation i, i.facility f ,i.investigationInstruments ii, ii.instrument ins where f.id = ' + facility.facilityId + ' AND ins.id = ' + instrumentId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Dataset',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    /** get datasets by investigationid**/
    data.getDatasetsByInvestigationId = function(mySessionId, facility, investigationId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datasets.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT d FROM Dataset d, d.investigation i, i.facility f where f.id = ' + facility.facilityId + ' AND i.id = ' + investigationId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Dataset',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    /** get datafiles **/
    data.getDatafiles = function(mySessionId, facility, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facility.facilityId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Datafile',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    data.getDatafilesByInstrumentId = function(mySessionId, facility, instrumentId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f, i.investigationInstruments ii, ii.instrument ins where f.id = ' + facility.facilityId + ' AND ins.id = ' + instrumentId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Datafile',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };


    data.getDatafilesByInvestigationId = function(mySessionId, facility, investigationId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facility.facilityId + ' AND i.id = ' + investigationId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Datafile',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };

    data.getDatafilesByDatasetId = function(mySessionId, facility, datasetId, options) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = ICATDATAPROXYURL + '/icat/entityManager';
            var query = 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facility.facilityId + ' AND d.id = ' + datasetId;
            query = appendOptionsToQuery(options, query);

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : query,
                    entity : 'Datafile',
                    server : facility.icatUrl
                }
            };

            return $http.get(url, params);
        }
    };

    return data;
}





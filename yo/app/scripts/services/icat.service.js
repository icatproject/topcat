'use strict';

angular.
    module('angularApp').factory('ICATService', ICATService);

ICATService.$inject = ['$http', '$q', 'APP_CONFIG'];

/*jshint -W098 */
function ICATService($http, $q, APP_CONFIG) {

    var useFileForData = angular.isDefined(APP_CONFIG.site.useFileForData) ? APP_CONFIG.site.useFileForData : false;
    var useFileForSession = angular.isDefined(APP_CONFIG.site.useFileForSession) ? APP_CONFIG.site.useFileForSession : false;
    var data = {};
    var mySessionId = '40960569-1645-4766-ae8f-fea6d40fb46a';
    var LIMIT = ' LIMIT 0, 1000'; //apply a limit to a query. FOR DEBUG ONLY

    console.log('useFileForData:' + useFileForData);
    console.log('useFileForSession:' + useFileForSession);

    data.login = function() {
        if (useFileForSession) {
            console.log('mySessionId before: ' + mySessionId);
            return $http.get('data/icatapi-session.json')
                .success(function(data) {
                    //mySessionId = data.sessionId;
                    //console.log('mySessionId after: ' + mySessionId);
                })
                .error(function(error) {

                });

        } else {

            var url = 'api/icat/session';
            var data = {
                'json' : '{"plugin":"ldap","credentials":[{"username":"vcf21513"},{"password":"PASSWORD"}]}'
            };

            /*var data = {
                'plugin': 'ldap',
                'credentials': [
                    {'username': 'vcf21513'},
                    {'password': 'PASSWORD'}
                ]
            };

            data = 'json=' + encodeURIComponent(JSON.stringify(data));*/

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


    data.getVersion = function() {
        var url = 'api/icat/version';

        return $http.get(url);
    };


    /** get facilities */
    data.getFacilities = function() {
        if (useFileForData) {
            return $http.get('data/icatapi-facility.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT f FROM Facility f'
                }

            };

            return $http.get(url, params);
        }
    };

    /** get instruments **/
    data.getInstruments = function(facilityId) {
        if (useFileForData) {
            return $http.get('data/icatapi-instruments.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT ins FROM Instrument ins, ins.facility f where f.id = ' + facilityId
                }

            };

            return $http.get(url, params);
        }
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
        if (useFileForData) {
            return $http.get('data/icatapi-investigations.json');
        } else {

            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT i FROM Investigation i, i.facility f where f.id = 1 LIMIT 0, 5000'
                }
            };

            return $http.get(url, params);
        }
    };

    data.getInvestigationsByCycleId = function(facilityId, cycleId) {
        return $http.get('data/icatapi-investigations-5-items.json');
    };

    data.getInvestigationsByInstrumentId = function(facilityId, instrumentId) {
        if (useFileForData) {
            return $http.get('data/icatapi-investigations-5-items.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT i FROM Investigation i, i.investigationInstruments ii, ii.instrument ins where ins.id = ' + instrumentId + LIMIT
                }

            };

            return $http.get(url, params);
        }


    };

    data.getInvestigationsByInstrumentIdByCycleId = function(facilityId, instrumentId, cycleId) {

        return $http.get('data/icatapi-investigations-5-items.json');
    };


    /** get datasets **/
    data.getDatasets = function(facilityId){
        console.log('facilityId: ' + facilityId);

        if (useFileForData) {
            return $http.get('data/icatapi-datasets.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT d FROM Dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };


    data.getDatasetsByInstrumentId = function(facilityId, instrumentId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT d FROM Dataset d, d.investigation i, i.facility f ,i.investigationInstruments ii, ii.instrument ins where f.id = ' + facilityId + ' AND ins.id = ' + instrumentId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };


    /** get datasets by investigationid**/
    data.getDatasetsByInvestigationId = function(facilityId, investigationId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datasets.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT d FROM Dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + ' AND i.id = ' + investigationId + LIMIT
                }

            };

            return $http.get(url, params);
        }


    };


    /** get datafiles **/
    data.getDatafiles = function(facilityId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };


    data.getDatafilesByInstrumentId = function(facilityId, instrumentId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f, i.investigationInstruments ii, ii.instrument ins where f.id = ' + facilityId + ' AND ins.id = ' + instrumentId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };


    data.getDatafilesByInvestigationId = function(facilityId, investigationId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + ' AND i.id = ' + investigationId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };

    data.getDatafilesByDatasetId = function(facilityId, datasetId) {
        if (useFileForData) {
            return $http.get('data/icatapi-datafiles.json');
        } else {
            var url = 'api/icat/entityManager';

            var params = {
                params : {
                    sessionId : mySessionId,
                    query : 'SELECT df FROM Datafile df, df.dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + ' AND d.id = ' + datasetId + LIMIT
                }

            };

            return $http.get(url, params);
        }
    };

    return data;
}





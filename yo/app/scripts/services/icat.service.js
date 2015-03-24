'use strict';

angular.
    module('angularApp').factory('ICATService', ICATService);

ICATService.$inject = ['$http', '$q', 'APP_CONFIG'];

/*jshint -W098 */
function ICATService($http, $q, APP_CONFIG) {

    var useFileForData = angular.isDefined(APP_CONFIG.site.useFileForData) ? APP_CONFIG.site.useFileForData : false;
    var useFileForSession = angular.isDefined(APP_CONFIG.site.useFileForSession) ? APP_CONFIG.site.useFileForSession : false;
    var data = {};
    var mySessionId = 'ea18b656-f56a-4732-bf37-f7eba008e203';

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
    data.getInstruments = function(facilityId){

        return $http.get('data/icatapi-instruments.json');
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
                    query : 'SELECT i FROM Investigation i, i.facility f where f.id = 1 LIMIT 10, 100'
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
                    query : 'SELECT i FROM Investigation i, i.investigationInstruments ii, ii.instrument ins where ins.id = ' + instrumentId + ' LIMIT 0, 100'
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
                    query : 'SELECT d FROM Dataset d, d.investigation i, i.facility f where f.id = ' + facilityId + ' LIMIT 0, 100'
                }

            };

            return $http.get(url, params);
        }
    };

    data.getDatasetsByInvestigationId = function(facilityId, investigationId) {
        return $http.get('data/icatapi-datasets.json');
    };


    /** get datafiles **/
    data.getDatafiles = function(facilityId) {
        return $http.get('data/icatapi-datafiles.json');
    };

    data.getDatafilesByDatasetId = function(facilityId, datasetId) {
        return $http.get('data/icatapi-datafiles.json');
    };

    data.getDatafilesByInvestigationId = function(facilityId, investigationId) {
        return $http.get('data/icatapi-datafiles.json');
    };

    return data;
}





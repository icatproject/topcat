(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('ICATSearchService', ['$http', '$sessionStorage', '$q', 'APP_CONFIG', function($http, $sessionStorage, $q, APP_CONFIG){

        this.search = function(facilityNames, query, fn){
            var results = [];
            var promises = [];
            var canceler = $q.defer();

            _.each(facilityNames, function(facilityName){
                var sessionId = $sessionStorage.sessions[facilityName].sessionId;
                var url = APP_CONFIG.facilities[facilityName].icatUrl + '/icat/lucene/data';
                promises.push($http({
                    url: url, 
                    method: 'GET',
                    params: {
                        sessionId: sessionId,
                        query: JSON.stringify(query),
                        maxCount: 1000
                    },
                    timeout: canceler.promise
                }).then(function(response){
                    results = _.sortBy(_.flatten([results, response.data]), 'score').reverse();
                    fn.call(null, results);
                }));
            });

            return _.assign($q.all(promises), {cancel: function(){ canceler.resolve(); }});
        };

    }]);

})();

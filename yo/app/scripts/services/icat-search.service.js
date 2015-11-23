(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('ICATSearchService', ['$http', '$sessionStorage', '$q', 'APP_CONFIG', function($http, $sessionStorage, $q, APP_CONFIG){

        this.search = function(facilityNames, query, fn){
            var results = [];
            var promises = [];

            _.each(facilityNames, function(facilityName){
                var sessionId = $sessionStorage.sessions[facilityName].sessionId;
                var url = APP_CONFIG.facilities[facilityName].icatUrl + '/icat/lucene/data';
                promises.push($http({
                    url: url, 
                    method: "GET",
                    params: {
                        sessionId: sessionId,
                        query: JSON.stringify(query)
                    }
                }).then(function(response){
                    _.each(response.data, function(result){
                        results.push(result);
                    });
                    fn.call(null, results);
                }));
            });

            return $q.all(promises);
        };

    }]);

})();

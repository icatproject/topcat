(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('ICATSearchService', ['$http', '$sessionStorage', '$q', 'APP_CONFIG', function($http, $sessionStorage, $q, APP_CONFIG){

        this.search = function(facilityNames, query, fn, cancelerPromise){
            var results = [];
            var promises = [];
            var canceler = $q.defer();

            if(cancelerPromise) cancelerPromise.then(function(){ canceler.resolve(); });

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
                    var _results = _.map(response.data, function(result){
                        var out = result[query.target];
                        out.score = result.score;
                        out.facilityName = facilityName;
                        return out;
                    });
                    results = _.sortBy(_.flatten([results, _results]), 'score').reverse();
                    fn.call(null, results);
                }));
            });

            return _.assign($q.all(promises), {cancel: function(){ canceler.resolve(); }});
        };

    }]);

})();

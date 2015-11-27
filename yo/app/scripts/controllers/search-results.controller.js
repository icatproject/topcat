(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('SearchResultsController', ['$stateParams', '$scope', '$q', '$sessionStorage', 'ICATSearchService', 'IdsManager', 'APP_CONFIG', function($stateParams, $scope, $q, $sessionStorage, ICATSearchService, IdsManager, APP_CONFIG){
    	var facilities = $stateParams.facilities ? JSON.parse($stateParams.facilities) : [];
    	var text = $stateParams.text;
    	var type = $stateParams.type;
    	var startDate = $stateParams.startDate;
    	var endDate = $stateParams.endDate;

        var canceler = $q.defer();
        $scope.$on('$destroy', function(){ canceler.resolve(); });

        var gridOptions = {data: []};
        _.merge(gridOptions, APP_CONFIG.site.searchGridOptions[type]);
        this.gridOptions = gridOptions;

     	var query = {target: type}
     	if(text) query.text = text;
     	if(startDate) query.lower = startDate.replace(/-/g, '') + "0000";
     	if(endDate) query.upper = endDate.replace(/-/g, '') + "0000";

        ICATSearchService.search(facilities, query, function(results){
        	gridOptions.data = results;
        }, canceler.promise).then(function(){
            _.each(gridOptions.data, function(entity){
                getSize(entity.facilityName, entity.id).then(function(data){
                    entity.size = parseInt(data);
                });
            });
        });

        //proxy to simplify later refactor
        function getSize(facilityName, id){
            var params = {};
            params[type  + 'Ids'] = id;
            params.canceler = canceler.promise;
            return IdsManager.getSize($sessionStorage.sessions, APP_CONFIG.facilities[facilityName], params);
        }

        console.log('gridOptions', gridOptions);

    }]);

})();
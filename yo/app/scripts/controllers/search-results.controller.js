(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('SearchResultsController', ['$stateParams', 'ICATSearchService', 'APP_CONFIG', function($stateParams, ICATSearchService, APP_CONFIG){
    	var facilities = $stateParams.facilities ? JSON.parse($stateParams.facilities) : [];
    	var text = $stateParams.text;
    	var type = $stateParams.type;
    	var startDate = $stateParams.startDate;
    	var endDate = $stateParams.endDate;

        var gridOptions = {data: []};
        _.merge(gridOptions, APP_CONFIG.site.searchGridOptions[type]);
        this.gridOptions = gridOptions;

     	var query = {target: type}
     	if(text) query.text = text;
     	if(startDate) query.lower = startDate.replace(/-/g, '') + "0000";
     	if(endDate) query.upper = endDate.replace(/-/g, '') + "0000";

     	console.log('query', query);

        ICATSearchService.search(facilities, query, function(results){
        	console.log('results', results);
        	gridOptions.data = _.map(results, function(result){
        		return result[type];
        	});
        });
    }]);

})();
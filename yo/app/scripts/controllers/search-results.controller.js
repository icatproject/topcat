(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('SearchResultsController', ['$stateParams', 'ICATSearchService', 'APP_CONFIG', function($stateParams, ICATSearchService, APP_CONFIG){
    	var facilities = $stateParams.facilities ? JSON.parse($stateParams.facilities) : [];
    	var text = $stateParams.text;
    	var type = $stateParams.type;

        var gridOptions = {data: []};
        _.merge(gridOptions, APP_CONFIG.site.searchGridOptions[type]);
        this.gridOptions = gridOptions;

     	var query = {target: type}
     	if(text) query.text = text;

        ICATSearchService.search(facilities, query, function(results){
        	gridOptions.data = _.map(results, function(result){
        		return result[type];
        	});
        });
    }]);

})();
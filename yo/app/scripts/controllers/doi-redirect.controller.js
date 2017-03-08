

(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DoiRedirectController', function($state, tc, helpers){
    	var facilityName = $state.params.facilityName;
    	var entityType = $state.params.entityType;
    	var entityId = $state.params.entityId;

    	tc.icat(facilityName).query(["select ? from ? ? where ?.id = ", entityType.safe(), helpers.capitalize(entityType).safe(), entityType.safe(),entityType.safe(), entityId]).then(function(entities){
    		if(entityType == 'datafile'){
    			entities[0].parent().then(function(entity){
    				entity.browse();
    			});
    		} else {
    			entity.browse();
    		}
    	});
    });

})();

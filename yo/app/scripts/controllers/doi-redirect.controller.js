

(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DoiRedirectController', function($state, tc){
    	var facilityName = $state.params.facilityName;
    	var entityType = $state.params.entityType;
    	var entityId = $state.params.entityId;

    	tc.icat(facilityName).entity(entityType, ["where ?.id = ", entityType.safe(), entityId]).then(function(entity){
    		if(entityType == 'datafile'){
    			entity.parent().then(function(entity){
    				entity.browse();
    			});
    		} else {
    			entity.browse();
    		}
    	});
    });

})();

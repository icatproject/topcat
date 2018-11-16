

(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DoiRedirectController', function($state, inform, tc, helpers){
    	var facilityName = $state.params.facilityName;
    	var entityType = $state.params.entityType;
    	var entityId = $state.params.entityId;

    	tc.icat(facilityName).query(["select ? from ? ? where ?.id = ", entityType.safe(), helpers.capitalize(entityType).safe(), entityType.safe(),entityType.safe(), entityId]).then(function(entities){
    		if( ! entities || ! entities[0] ){
    			// Query may return [] or null if the entity ID is unknown, [null] if user has no read access
    			// In these cases, inform the user.
    			// We could go to the default home page in this case, but BR prefers to leave the page blank.
    			// If we *do* set a particular state, it has to happen before the inform.
    			// var state = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
    	        // $state.go(state);
    			inform.add("Cannot read the " + entityType + ". You may not have read access, or it may not be published yet", {
    				'ttl' : -1,
    				'type' : 'warning'
    			});
    		} else if(entityType == 'datafile'){
    			entities[0].parent().then(function(entity){
    				entity.browse();
    			});
    		} else {
    			entities[0].browse();
    		}
    	});
    });

})();

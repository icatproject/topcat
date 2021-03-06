

(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DoiRedirectController', function($state, $translate, $q, inform, tc, helpers){
    	var facilityName = $state.params.facilityName;
    	var entityType = $state.params.entityType;
    	var entityId = $state.params.entityId;
    	
    	// anonLogin (boolean) will only be set in some cases
    	var anonLogin = $state.params.anonLogin;
    	
    	// By default, use an "empty" promise rather than logging in
    	var optionalLoginPromise = $q.when();
    	
    	if( ! tc.icat(facilityName).session().sessionId ){
    		console.log("doi-redirect: no sessionId for " + facilityName);
			if( anonLogin === 'true'){
				console.log("doi-redirect: anonLogin set, so auto-login");
				// This should be configurable, or hard-wired to the anon authenticator;
				// (ICAT for) LILS is not set up for anon, so hard-wire simple auth instead
				var doiAuth = tc.facility(facilityName).config().doiAutoLoginAuth;
				if( ! doiAuth ){
					console.log("doi-redirect: no doi authentication configured, so assume anon");
					doiAuth = {
						plugin: "anon",
						credentials: {}
					}
				};
				optionalLoginPromise = tc.icat(facilityName).login(doiAuth.plugin,doiAuth.credentials);
			}
    	}
    	
    	// We would like the eventual target to replace the original link in the browser history,
    	// so that the Back button doesn't simply repeat the redirection.
    	// Passing {location: 'replace'} as an option to the eventual call of $state.go() seems to work.
    	
    	optionalLoginPromise.then(function() {
	    	tc.icat(facilityName).query(["select ? from ? ? where ?.id = ", entityType.safe(), helpers.capitalize(entityType).safe(), entityType.safe(),entityType.safe(), entityId]).then(function(entities){
	    		if( ! entities || ! entities[0] ){
	    			// Query may return [] or null if the entity ID is unknown, [null] if user has no read access
	    			// In these cases, inform the user.
	    			// We could go to the default home page in this case, but BR prefers to leave the page blank.
	    			// If we *do* set a particular state, it has to happen before the inform.
	    			// var state = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
	    	        // $state.go(state);
	    			var msg = $translate.instant("DOI.QUERY_EMPTY");
	    			if( msg != "DOI.QUERY_EMPTY" ){
	    				msg = msg.replace(/_ENTITY_TYPE_/g,entityType);
	    			} else {
	    				msg = "Cannot read the " + entityType + ". You may not have read access, or it may not be published yet.";
	    			}
	    			inform.add(msg, {
	    				'ttl' : -1,
	    				'type' : 'warning'
	    			});
	    		} else if(entityType == 'datafile'){
	    			entities[0].parent().then(function(entity){
	    				entity.browse({location:'replace'});
	    			});
	    		} else {
	    			entities[0].browse({location:'replace'});
	    		}
	    	})
    	});
    });

})();

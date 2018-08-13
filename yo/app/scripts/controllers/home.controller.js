(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('HomeController', function($rootScope, $scope, tc, helpers) {

    	this.tabs = [];

    	var existingTabs = [
    		{
    			name: "my-data",
    			translate: "MAIN_NAVIGATION.MAIN_TAB.MY_DATA",
    			sref: "home.my-data",
    			showState: "home.my-data"
    		},
    		{
    			name: "browse",
    			translate: "MAIN_NAVIGATION.MAIN_TAB.BROWSE",
    			sref: "home.browse.facility",
    			showState: "home.browse"
    		}
    	];

        if( tc.config().search.enabled ){
            existingTabs.push({
                name: "search",
                translate: "MAIN_NAVIGATION.MAIN_TAB.SEARCH",
                sref: "home.search.start",
                showState: "home.search"                
            })
        };

    	var otherTabs = _.map(tc.ui().mainTabs(), function(otherTab){
    		return {
    			name: otherTab.name,
    			translate: "MAIN_NAVIGATION.MAIN_TAB." + otherTab.name.toUpperCase().replace(/-/g, '_'),
    			sref: "home." + otherTab.name,
    			showState: "home." + otherTab.name,
    			insertBefore: otherTab.options.insertBefore,
    			insertAfter: otherTab.options.insertAfter
    		};
    	});

    	this.tabs = helpers.mergeNamedObjectArrays(existingTabs, otherTabs);
    });
    
})();

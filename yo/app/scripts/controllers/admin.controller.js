
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminController', function($state, tc, helpers){
    	this.tabs = [];

    	var existingTabs = [
    		{
    			name: "downloads",
    			translate: "ADMIN.MAIN_TAB.DOWNLOADS",
    			sref: "admin.downloads",
    			showState: "admin.downloads"
    		},
    		{
    			name: "messages",
    			translate: "ADMIN.MAIN_TAB.MESSAGES",
    			sref: "admin.messages",
    			showState: "admin.messages"
    		}
    	];

    	var otherTabs = _.map(tc.ui().mainTabs(), function(otherTab){
    		return {
    			name: otherTab.name,
    			translate: "ADMIN.MAIN_TAB." + otherTab.name.toUpperCase().replace(/-/g, '_'),
    			sref: "admin." + otherTab.name,
    			showState: "admin." + otherTab.name,
    			insertBefore: otherTab.options.insertBefore,
    			insertAfter: otherTab.options.insertAfter
    		};
    	});

    	this.tabs = helpers.mergeNamedObjectArrays(existingTabs, otherTabs);
    });

})();
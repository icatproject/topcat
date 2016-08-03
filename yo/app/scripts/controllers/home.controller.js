(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('HomeController', function($rootScope, $scope, tc) {
        var that = this;
    	this.tabs = [];

    	$scope.$on('$destroy', $rootScope.$on('maintab:change', refreshTabs));


    	refreshTabs();

    	function refreshTabs(){
    		that.tabs = [
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
	    		},
	    		{
	    			name: "search",
	    			translate: "MAIN_NAVIGATION.MAIN_TAB.SEARCH",
	    			sref: "home.search.start",
	    			showState: "home.search"
	    		}
	    	];

	    	var otherTabs = _.map(tc.ui().mainTabs(), function(otherTab){
	    		return {
	    			name: otherTab.name,
	    			translate: "MAIN_NAVIGATION.MAIN_TAB." + otherTab.name.toUpperCase().replace(/-/, '_'),
	    			sref: "home." + otherTab.name,
	    			showState: "home." + otherTab.name,
	    			insertBefore: otherTab.options.insertBefore,
	    			insertAfter: otherTab.options.insertAfter
	    		};
	    	});

	    	var changed;
	    	
	    	while(true){
	    		changed = false;

	    		_.each(_.clone(otherTabs), function(otherTab){
	    			if(otherTab.insertBefore){
	    				var index = _.findIndex(that.tabs, function(tab){
	    					return tab.name == otherTab.insertBefore
	    				});

	    				if(index !== -1){
	    					that.tabs.splice(index, 0, otherTab);
	    					_.remove(otherTabs, {name: otherTab.name});
	    					changed = true;
	    				}

	    			} else if(otherTab.insertAfter){
	    				var index = _.findIndex(that.tabs, function(tab){
	    					return tab.name == otherTab.insertAfter;
	    				});

	    				if(index !== -1){
	    					that.tabs.splice(index + 1, 0, otherTab);
	    					_.remove(otherTabs, {name: otherTab.name});
	    					changed = true;
	    				}
	    			} else {
	    				that.tabs.push(otherTab);
	    				_.remove(otherTabs, {name: otherTab.name});
	    				changed = true;
	    			}	
	    		});

	    		if(!changed) break;
	    	}

	    }
    });
    
})();

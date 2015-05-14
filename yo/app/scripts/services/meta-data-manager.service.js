'use strict';

angular
	.module('angularApp')
	.factory('MetaDataManager', MetaDataManager);

MetaDataManager.$inject = [];

function MetaDataManager() {


	function MyException(message) {
		this.name = name;
		this.message = message;
	}
	MyException.prototype = new Error();
	MyException.prototype.constructor = MyException;
	

	var extractMetaData = function (tabDataArray, icatData) {

        var content = '';

        if(!Array.isArray(icatData)){
            icatData = [icatData];
        }

        for(var l in icatData) {
            var icatDataCurrent = icatData[l];

            for(var counter in tabDataArray) {
                var dataV = tabDataArray[counter];

                if(typeof dataV.data !== 'undefined') {
                    content += extractMetaData(dataV.data, icatDataCurrent[dataV.icatName]);
                } else {
                    content += dataV.title + ': ';
                    content += icatDataCurrent[dataV.icatName] + '<br>';
                }
            }
        }
        return content;
	};

	return {

		updateTabs : function(dataResults, tabs) {

	        var tabsUpdated = [];

	        for(var i in tabs)
	        {

	            var icatData = dataResults;
	            var currentTab = tabs[i];
	            var tabTitle = currentTab.title;
	            var tabData = currentTab.data;
	            var tabContent = '';

	            if(currentTab.default === true) {
	                tabContent += extractMetaData(tabData, icatData);
	            } else {
	                tabContent += extractMetaData(tabData, icatData[0][currentTab.icatName]);
	            }

	            var temp = {title : tabTitle, content : tabContent};
	            tabsUpdated.push(temp);
	        }
	        return tabsUpdated;
		},

		getTabQueryOptions : function(tabConfig) {

	        var optionsList = {
	            'include' : []
	        };

	        for(var index in tabConfig) {
	            var tab = tabConfig[index];

	            if(typeof tab.queryParams !== 'undefined') {
	                optionsList.include.push(tab.queryParams);
	            }
	        }

	        if(optionsList.include.length === 0){
	            optionsList = {};
	        }

	        return optionsList;
	    }
	};
}
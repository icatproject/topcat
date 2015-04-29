(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = ['$rootScope', '$scope', '$state', '$stateParams','DataManager', 'APP_CONFIG', 'Config', 'RouteUtils', '$sessionStorage'];

    function MetaPanelController($rootScope, $scope, $state, $stateParams, DataManager, APP_CONFIG, Config, RouteUtils, $sessionStorage){
        var vm = this;

        var facilityName = $stateParams.facilityName;
        var currentEntityType = RouteUtils.getCurrentEntityType($state);
        var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facilityName);
        var nextEntityType = RouteUtils.getNextEntityType(structure, currentEntityType);

        var tabs = Config.getMetaTabsByEntityType(APP_CONFIG, facilityName, currentEntityType);

        var options = getQueryOptions(tabs);

        var sessions = $sessionStorage.sessions;

        console.log('$state: ', $state);
        console.log('$stateParams: ', $stateParams);
        console.log('Facility name : ', facilityName);
        console.log('structure: ', structure);
        console.log('currentEntityType: ', currentEntityType);
        console.log('nextEntityType: ', nextEntityType);
        console.log('sessions: ', sessions);

        $scope.message = null;

        var cleanUpFunc = $rootScope.$on("rowclick", function(event, message){

            $scope.message = message;
            vm.tabs = [];

            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);

            DataManager.getEntityById(sessions, facility, message.Type, message.Id, options)
            .then(function(data) {
                vm.tabs = updateTabs(vm, data, tabs);
            }), (function(error) {
                console.log('Error: Failed to get data from icat'); 
            });
        })

        $scope.$on('$destroy', function() {
            cleanUpFunc();
        });
    }

    function getQueryOptions(tabConfig) {

        var optionsList = optionsList = {"include" : []};;

        for(var index in tabConfig) 
        {
            var tab = tabConfig[index];

            if(typeof tab.queryParams !== 'undefined')
            {
                optionsList["include"].push(tab.queryParams);
            }
        }

        if(optionsList['include'].length == 0){
            optionsList = {};
        }

        return optionsList;
    }

    function updateTabs(vm, dataResults, tabs) { 

        var tabsUpdated = [];

        for(var i in tabs)
        {  

            var icatData = dataResults;            
            var currentTab = tabs[i];
            var tabTitle = currentTab.title;
            var tabData = currentTab.data;
            var tabContent = ''; 

            if(currentTab.default == true) {
                tabContent += getMetaData(tabData, icatData);
            } else {
                tabContent += getMetaData(tabData, icatData[0][currentTab.icatName]);
            }

            var temp = {title : tabTitle, content : tabContent};
            tabsUpdated.push(temp);
        }
        return tabsUpdated;
    }

    function getMetaData(dataArray, icatData) {

        var content = '';

        if(!Array.isArray(icatData)){
            icatData = [icatData];
        }

        for(var l in icatData){

            var icatDataCurrent = icatData[l];

            for(var counter in dataArray)
            {
                var dataV = dataArray[counter];

                if(typeof dataV.data != 'undefined') 
                {
                    content += getMetaData(dataV.data, icatDataCurrent[dataV.icatName]);
                } else {
                    content += dataV.title + ': '; 
                    content += icatDataCurrent[dataV.icatName] + '<br>';
                }
            }   
        }
        return content;
    }
})();

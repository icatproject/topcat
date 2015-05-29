(function() {
    'use strict';

    angular
    .module('angularApp')
    .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = ['$rootScope', '$scope', '$state', '$stateParams','DataManager', 'APP_CONFIG', 'Config', 'RouteUtils', '$sessionStorage', 'MetaDataManager'];

    function MetaPanelController($rootScope, $scope, $state, $stateParams, DataManager, APP_CONFIG, Config, RouteUtils, $sessionStorage, MetaDataManager){
        var vm = this;

        var facilityName = $stateParams.facilityName;
        var currentEntityType = RouteUtils.getCurrentEntityType($state);

        var tabs = [];

        //if facility then get the facility meta tab config from site, else get config from facility config
        if (currentEntityType === 'facility') {
            tabs = Config.getSiteFacilitiesMetaTabs(APP_CONFIG);
        } else {
            tabs = Config.getMetaTabsByEntityType(APP_CONFIG, facilityName, currentEntityType);
        }

        var options = MetaDataManager.getTabQueryOptions(tabs);

        var sessions = $sessionStorage.sessions;


        $scope.message = null;

        $scope.$on('rowclick', function(event, message){

            $scope.message = message;

            if(typeof tabs !== 'undefined') {

                vm.tabs = [];

                var facility = Config.getFacilityByName(APP_CONFIG, message.facilityName);

                DataManager.getEntityById(sessions, facility, message.type, message.id, options)
                .then(function(data) {
                    vm.tabs = MetaDataManager.updateTabs(data, tabs);
                }, function(error) {
                    console.log('Error: Failed to get data from icat', error);
                });
            }
        });
    }
})();



(function() {
    'use strict';

    angular
    .module('angularApp')
    .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = ['$rootScope', '$scope', '$state', '$stateParams','DataManager', 'APP_CONFIG', 'Config', 'RouteUtils', '$sessionStorage', 'MetaDataManager', 'inform'];

    function MetaPanelController($rootScope, $scope, $state, $stateParams, DataManager, APP_CONFIG, Config, RouteUtils, $sessionStorage, MetaDataManager, inform){
        var vm = this;

        var tabs = [];
        $scope.data = null;

        $scope.$on('rowclick', function(event, data){
            //if facility then get the facility meta tab config from site, else get config from facility config
            if (data.type === 'facility') {
                tabs = Config.getSiteFacilitiesMetaTabs(APP_CONFIG);
            } else {
                tabs = Config.getMetaTabsByEntityType(APP_CONFIG, data.facilityName, data.type);
            }

            var options = MetaDataManager.getTabQueryOptions(tabs);
            var sessions = $sessionStorage.sessions;

            $scope.data = data;

            if(typeof tabs !== 'undefined') {
                vm.tabs = [];

                var facility = Config.getFacilityByName(APP_CONFIG, data.facilityName);

                DataManager.getEntityById(sessions, facility, data.type, data.id, options)
                .then(function(data) {
                    vm.tabs = MetaDataManager.updateTabs(data, tabs);
                }, function(error) {
                    inform.add(error, {
                        'ttl': 4000,
                        'type': 'danger'
                    });
                });
            }
        });
    }
})();

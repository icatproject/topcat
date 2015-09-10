(function() {
    'use strict';

    angular
    .module('angularApp')
    .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = ['$rootScope', '$scope', '$state', '$stateParams','DataManager', 'APP_CONFIG', 'Config', 'RouteUtils', '$sessionStorage', 'MetaDataManager', 'inform', '$log'];

    function MetaPanelController($rootScope, $scope, $state, $stateParams, DataManager, APP_CONFIG, Config, RouteUtils, $sessionStorage, MetaDataManager, inform, $log){
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

            $log.debug('MetaPanelController data', data);

            var options = MetaDataManager.getTabQueryOptions(tabs);
            var sessions = $sessionStorage.sessions;

            $scope.data = data;

            if(typeof tabs !== 'undefined') {
                vm.tabs = [];

                var facility = Config.getFacilityByName(APP_CONFIG, data.facilityName);

                /*//deal with special case where entity type is a proposal
                var entityType = data.type;

                if (data.type === 'propropal') {
                    entityType = 'investigation';
                }*/

                DataManager.getEntityById(sessions, facility, data.type, data.id, options)
                .then(function(data) {
                    vm.tabs = MetaDataManager.updateTabs(data, tabs);
                }, function(error) {
                    $log.error(error);

                    inform.add(error, {
                        'ttl': 4000,
                        'type': 'danger'
                    });
                });
            }
        });
    }
})();

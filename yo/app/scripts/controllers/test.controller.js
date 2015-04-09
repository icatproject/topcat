(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('TestController', TestController);

    TestController.$inject = ['APP_CONFIG', 'Config'];

    function TestController(APP_CONFIG, Config) {
        var vm = this;

        var random;

        if (!random) {
            random = Math.round(Math.random()*10000);
        }

        vm.random = random;

        console.log('getFacilities', Config.getFacilities(APP_CONFIG));

        console.log('getFacilityByName isis', Config.getFacilityByName(APP_CONFIG, 'isis'));
        console.log('getFacilityByName dls', Config.getFacilityByName(APP_CONFIG, 'dls'));

        console.log('getHierarchyByFacilityName isis', Config.getHierarchyByFacilityName(APP_CONFIG, 'isis'));
        console.log('getHierarchyByFacilityName dls', Config.getHierarchyByFacilityName(APP_CONFIG, 'dls'));

        console.log('getColumnsByFacilityName isis', Config.getColumnsByFacilityName(APP_CONFIG, 'isis'));
        console.log('getColumnsByFacilityName dls', Config.getColumnsByFacilityName(APP_CONFIG, 'dls'));

        console.log('getFacilitiesColumns dls', Config.getFacilitiesColumns(APP_CONFIG));



    }


})();

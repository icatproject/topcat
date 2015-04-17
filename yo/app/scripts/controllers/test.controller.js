(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('TestController', TestController);

    TestController.$inject = ['APP_CONFIG', 'Config', 'DataManager'];

    function TestController(APP_CONFIG, Config, DataManager) {
        var vm = this;

        var facility = Config.getFacilityByName(APP_CONFIG, 'dls');
        var promise = DataManager.login(facility);

        promise.then(function(data){
            vm.session = data;
        });



        /*console.log('getFacilities', Config.getFacilities(APP_CONFIG));

        console.log('getFacilityByName isis', Config.getFacilityByName(APP_CONFIG, 'isis'));
        console.log('getFacilityByName dls', Config.getFacilityByName(APP_CONFIG, 'dls'));

        console.log('getHierarchyByFacilityName isis', Config.getHierarchyByFacilityName(APP_CONFIG, 'isis'));
        console.log('getHierarchyByFacilityName dls', Config.getHierarchyByFacilityName(APP_CONFIG, 'dls'));

        console.log('getColumnsByFacilityName isis', Config.getColumnsByFacilityName(APP_CONFIG, 'isis'));
        console.log('getColumnsByFacilityName dls', Config.getColumnsByFacilityName(APP_CONFIG, 'dls'));

        console.log('getFacilitiesColumns dls', Config.getFacilitiesColumns(APP_CONFIG));*/



    }


})();

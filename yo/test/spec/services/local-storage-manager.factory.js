'use strict';

describe('Service: LocalStorageManager', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('test/mock/data/mock-config-multi.json'));
            $provide.constant('APP_CONSTANT', {
                storageName: 'TopCAT_0.1'
            });
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var LocalStorageManager;
    beforeEach(inject(function(_LocalStorageManager_) {
        LocalStorageManager = _LocalStorageManager_;
    }));

    var $localStorage;
    beforeEach(inject(function(_$localStorage_) {
        $localStorage = _$localStorage_;
    }));

    var APP_CONSTANT;
    beforeEach(inject(function(_APP_CONSTANT_) {
        APP_CONSTANT = _APP_CONSTANT_;
    }));

    /*var Config;
    beforeEach(inject(function(_Config_) {
        Config = _Config_;
    }));*/

    it('local storage init for a new user', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/vcf21513');

        expect($localStorage[APP_CONSTANT.storageName]).toEqual(
            {
                'ldap/vcf21513@dls': {
                    items: []
                }
            }
        );

        $localStorage.$reset();
    });

    it('get local storage a user', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/vcf21513');

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/vcf21513')).toEqual(
            {
                items: []
            }
        );

        //console.log(JSON.stringify($localStorage, null, 2));
        $localStorage.$reset();
    });

    it('get local storage for a non existing facility', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/vcf21513');

        expect(LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/vcf21513')).toEqual(
            {
                items: []
            }
        );

        //console.log(JSON.stringify($localStorage, null, 2));
        $localStorage.$reset();
    });

    it('get local storage for a non existing user', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/vcf21513');

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/rachel')).toEqual(
            {
                items: []
            }
        );

        //console.log(JSON.stringify($localStorage, null, 2));
        $localStorage.$reset();
    });


    it('get local storage init for a specific user', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/abby');
        LocalStorageManager.init({facilityName: 'isis'}, 'ldap/rachel');

        var dlsStore = LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby');
        var isisStore = LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel');

        dlsStore.items = ['betamax', 'vhs'];
        isisStore.items = ['hello', 'world'];

        //console.log(JSON.stringify($localStorage[APP_CONSTANT.storageName], null, 2));

        expect(Object.keys($localStorage[APP_CONSTANT.storageName]).length).toEqual(2);


        expect(LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel')).toEqual(
            {
                items: ['hello', 'world']
            }
        );

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby')).toEqual(
            {
                items: ['betamax', 'vhs']
            }
        );

        //console.log(JSON.stringify($localStorage, null, 2));
        $localStorage.$reset();
    });


    it('clear a user\'s local storage', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/abby');

        var dlsStore = LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby');

        dlsStore.items = ['betamax', 'vhs'];

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby')).toEqual(
            {
                items: ['betamax', 'vhs']
            }
        );

        LocalStorageManager.resetUser({facilityName: 'dls'}, 'ldap/abby');

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby')).toEqual({
            items: []
        });

        $localStorage.$reset();
    });

    it('clear a user\'s local storage - ensure correct one is cleared', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/abby');
        LocalStorageManager.init({facilityName: 'isis'}, 'ldap/rachel');

        var dlsStore = LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby');
        var isisStore = LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel');

        dlsStore.items = ['betamax', 'vhs'];
        isisStore.items = ['hello', 'world'];

        //console.log(JSON.stringify($localStorage[APP_CONSTANT.storageName], null, 2));

        expect(Object.keys($localStorage[APP_CONSTANT.storageName]).length).toEqual(2);

        LocalStorageManager.resetUser({facilityName: 'dls'}, 'ldap/abby');

        expect(LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel')).toEqual(
            {
                items: ['hello', 'world']
            }
        );

        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby')).toEqual({
            items: []
        });

        //console.log(JSON.stringify($localStorage, null, 2));
        $localStorage.$reset();
    });


    it('clear entire store', function() {
        //console.log(APP_CONSTANT);
        LocalStorageManager.init({facilityName: 'dls'}, 'ldap/abby');
        LocalStorageManager.init({facilityName: 'isis'}, 'ldap/rachel');

        var dlsStore = LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby');
        var isisStore = LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel');

        dlsStore.items = ['betamax', 'vhs'];
        isisStore.items = ['hello', 'world'];

        //console.log(JSON.stringify($localStorage[APP_CONSTANT.storageName], null, 2));

        expect(Object.keys($localStorage[APP_CONSTANT.storageName]).length).toEqual(2);

        LocalStorageManager.reset();

        expect(LocalStorageManager.getUserStore({facilityName: 'isis'}, 'ldap/rachel')).toEqual({
            items: []
        });
        expect(LocalStorageManager.getUserStore({facilityName: 'dls'}, 'ldap/abby')).toEqual({
            items: []
        });

        expect($localStorage[APP_CONSTANT.storageName]).not.toBeDefined();


        $localStorage.$reset();
    });

});



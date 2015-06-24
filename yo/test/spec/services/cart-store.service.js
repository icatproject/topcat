'use strict';

describe('Service: CartStore', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // inject sessionStorage
    var $localStorage;
    beforeEach(inject(function(_$localStorage_) {
        $localStorage = _$localStorage_;
    }));


    // instantiate service
    var CartStore;
    beforeEach(inject(function(_CartStore_) {
        CartStore = _CartStore_;
    }));

    it('set cart to localstorage', function() {
        var cart = {
            sig : {
                items: [
                    {
                        'facilityName': 'dls',
                        'entityType': 'dataset',
                        'id': 123456,
                        'name': 'my test name',
                        'size': null,
                        'availability': 'ONLINE',
                        'parents': [
                            {
                              'entityType': 'investigation',
                              'id': 7654321
                            }
                        ]
                    }
                ]
            }
        };

        CartStore.set(cart);

        expect($localStorage.cart).toEqual(cart);
    });

    it('get cart from localstorage', function() {
        var initCart = {
            sig : {
                items: [
                    {
                        'facilityName': 'dls',
                        'entityType': 'dataset',
                        'id': 123456,
                        'name': 'my test name',
                        'size': null,
                        'availability': 'ONLINE',
                        'parents': [
                            {
                              'entityType': 'investigation',
                              'id': 7654321
                            }
                        ]
                    }
                ]
            }
        };

        CartStore.set(initCart);
        var cart = CartStore.get();

        expect(cart).toEqual(initCart);
    });


    it('changes to localstorage cart', function() {
        var initCart = {
            sig : {
                items: [
                    {
                        'facilityName': 'dls',
                        'entityType': 'dataset',
                        'id': 123456,
                        'name': 'my test name',
                        'size': null,
                        'availability': 'ONLINE',
                        'parents': [
                            {
                              'entityType': 'investigation',
                              'id': 7654321
                            }
                        ]
                    }
                ]
            }
        };

        CartStore.set(initCart);
        var cart = CartStore.get();
        expect(cart).toEqual(initCart);

        //make change something
        $localStorage.cart.sig.items[0].facilityName = 'isis';
        $localStorage.cart.sig.items[0].entityType = 'datafile';

        expect(cart).toEqual({
            sig : {
                items: [
                    {
                        'facilityName': 'isis',
                        'entityType': 'datafile',
                        'id': 123456,
                        'name': 'my test name',
                        'size': null,
                        'availability': 'ONLINE',
                        'parents': [
                            {
                              'entityType': 'investigation',
                              'id': 7654321
                            }
                        ]
                    }
                ]
            }
        });
    });
});



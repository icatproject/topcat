'use strict';

describe('Service: Cart', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('test/mock/data/mock-config-multi.json'));
            $provide.constant('SMARTCLIENTPING', {ping: 'offline'});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));

    // inject dependancies
    var $rootScope;
    var CartItem;
    var log;
    var Cart;
    var Config;
    var $sessionStorage;
    var LocalStorageManager;
    var $localStorage;
    var APP_CONSTANT;

    beforeEach(inject(function(_$rootScope_, _CartItem_, _$log_, _Cart_, _Config_, _$sessionStorage_, _LocalStorageManager_, _$localStorage_, _APP_CONSTANT_) {
        $rootScope = _$rootScope_;
        spyOn($rootScope, '$broadcast').and.callThrough();
        CartItem = _CartItem_;
        log = _$log_;
        Cart = _Cart_;
        Config = _Config_;
        $sessionStorage = _$sessionStorage_;
        LocalStorageManager = _LocalStorageManager_;
        $localStorage = _$localStorage_;
        APP_CONSTANT = _APP_CONSTANT_;
    }));

    it('add item to cart', function() {
        var facilityName = 'dls';
        var entityType = 'dataset';
        var id = 123456;
        var name = 'my test dataset';
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName, entityType, id, name, parentEntities);

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(1);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemAdded', { added: 1 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 1, exists: 0 });
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });

    it('add 2 different items to cart', function() {
        var facilityName1 = 'dls';
        var entityType1 = 'dataset';
        var id1 = 123456;
        var name1 = 'my test dataset';
        var parentEntities1 = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var facilityName2 = 'dls';
        var entityType2 = 'datafile';
        var id2 = 654321;
        var name2 = 'my test datafile';
        var parentEntities2 = [{
            entityType: 'dataset',
            id: 456789
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName1, entityType1, id1, name1, parentEntities1);
        Cart.addItem(facilityName2, entityType2, id2, name2, parentEntities2);

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(2);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemAdded', { added: 1 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 1, exists: 0 });
        expect($rootScope.$broadcast.calls.count()).toEqual(4);
    });

    it('add same items multiple times to cart', function() {
        var facilityName = 'dls';
        var entityType = 'dataset';
        var id = 123456;
        var name = 'my test dataset';
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName, entityType, id, name, parentEntities);
        Cart.addItem(facilityName, entityType, id, name, parentEntities);
        Cart.addItem(facilityName, entityType, id, name, parentEntities);
        Cart.addItem(facilityName, entityType, id, name, parentEntities);

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(1);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemAdded', { added: 1 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 1, exists: 0 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 0, exists: 1 });

        expect($rootScope.$broadcast.calls.count()).toEqual(8);
    });


    it('add items to cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });


        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(2);

        //expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 1, exists: 0 });
        expect($rootScope.$broadcast.calls.count()).toEqual(4);
    });


    it('add items to cart with duplicates', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(2);

        //expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemsAdded', { added: 2 });
        //expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 2, exists: 1 });
        expect($rootScope.$broadcast.calls.count()).toEqual(6);
    });

    it('remove an item from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        $rootScope.$broadcast.calls.reset();

        Cart.removeItem('dls', 'dataset', 123456);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(1);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemRemoved', { removed: 1 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { removed: 1});
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });

    it('remove an item from empty cart', function() {
        $rootScope.$broadcast.calls.reset();

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.removeItem('dls', 'dataset', 123456);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);

        expect($rootScope.$broadcast.calls.count()).toEqual(0);
    });

    it('remove items from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        $rootScope.$broadcast.calls.reset();

        Cart.removeItems([
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456
            },{
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457
            }
        ]);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemRemoved', { removed: 2 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { removed: 2});
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });

    it('remove an item from empty cart', function() {
        $rootScope.$broadcast.calls.reset();

        Cart.removeItems([
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456
            },{
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457
            }
        ]);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);

        expect($rootScope.$broadcast.calls.count()).toEqual(0);
    });

    it('remove all items from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(2);

        $rootScope.$broadcast.calls.reset();

        Cart.removeAllItems();

        expect(Cart.getCart().items.length).toEqual(0);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemRemoved', { removed: 2 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { removed: 2});
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });

    it('remove all items from empty cart', function() {
        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);

        $rootScope.$broadcast.calls.reset();

        Cart.removeAllItems();

        expect(cart.items.length).toEqual(0);
        expect($rootScope.$broadcast.calls.count()).toEqual(0);
    });

    it('get an item from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': null,
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(2);

        var item = Cart.getItem('dls', 'dataset', 123457);

        expect(item).toEqual(jasmine.any(CartItem));
        expect(item.toObject()).toEqual({
            'facilityName': 'dls',
            'userName': 'vcf21513',
            'entityType': 'dataset',
            'entityId': 123457,
            'name': 'my test dataset 2',
            'size': null,
            'availability': null,
            'parentEntities': [
                {
                  'entityType': 'investigation',
                  'entityId': 7654321
                }
            ]
        });
    });


    it('has an item in cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': null,
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(2);

        expect(Cart.hasItem('dls', 'dataset', 123456)).toEqual(true);
        expect(Cart.hasItem('dls', 'dataset', 123457)).toEqual(true);
        expect(Cart.hasItem('dls', 'dataset', 123458)).toEqual(false);
        expect(Cart.hasItem('sig', 'dataset', 123458)).toEqual(false);
        expect(Cart.hasItem('dls', 'investigation', 123458)).toEqual(false);
    });


    it('set and get a cart', function() {
        var myCart = {
            items: [
                {
                    'facilityName': 'isis',
                    'userName': 'vcf21513',
                    'entityType': 'datafile',
                    'entityId': 123456,
                    'name': 'my test name',
                    'size': null,
                    'availability': 'ONLINE',
                    'parentEntities': [
                        {
                          'entityType': 'investigation',
                          'entityId': 7654321
                        }
                    ]
                }
            ]
        };

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);
        Cart.setCart(myCart);
        expect(Cart.getCart().items.length).toEqual(1);
    });


    xit('getTotalItems from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        expect(Cart.getTotalItems()).toEqual(2);

        Cart.removeAllItems();

        expect(Cart.getTotalItems()).toEqual(0);
    });

    it('save cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        //init localstorage
        LocalStorageManager.init({facilityName: 'dls'}, 'vcf21513');

        //console.log('$localStorage', JSON.stringify($localStorage, null, 2));

        spyOn(Cart, 'save').and.callThrough();

        //deregister Cart:Change listener
        delete $rootScope.$$listeners['Cart:change'];

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        Cart.save();

        expect(Cart.save).toHaveBeenCalled();
    });

/*    it('restore cart after removing all items', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'entityId': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parentEntities': [
                    {
                      'entityType': 'investigation',
                      'entityId': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        //init localstorage
        LocalStorageManager.init({facilityName: 'dls'}, 'vcf21513');

        spyOn(Cart, 'restore').and.callThrough();
        spyOn(Cart, 'save').and.callThrough();

        //deregister Cart:Change listener
        delete $rootScope.$$listeners['Cart:change'];

        //console.log(JSON.stringify($rootScope.$$listeners, null, 2));

        angular.forEach(items, function(item) {
            Cart.addItem(item.facilityName, item.entityType, item.entityId, item.name, item.parentEntities);
        });

        Cart.save();

        Cart.save.calls.reset();

        Cart.removeAllItems();
        expect(Cart.save).not.toHaveBeenCalled();

    });*/


});



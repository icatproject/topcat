'use strict';

describe('Service: Cart', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));

    // inject dependancies
    var $rootScope;
    var CartItem;
    var CartStore;
    var log;
    var Cart;
    var Config;
    var $sessionStorage;

    beforeEach(inject(function(_$rootScope_, _CartItem_, _CartStore_, _$log_, _Cart_, _Config_, _$sessionStorage_) {
        $rootScope = _$rootScope_;
        spyOn($rootScope, '$broadcast').and.callThrough();
        CartItem = _CartItem_;
        CartStore = _CartStore_;
        log = _$log_;
        Cart = _Cart_;
        Config = _Config_;
        $sessionStorage = _$sessionStorage_;
    }));

    it('add item to cart', function() {
        var facilityName = 'dls';
        var entityType = 'dataset';
        var id = 123456;
        var name = 'my test dataset';
        var parents = [{
            entityType: 'investigation',
            id: 7654321
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName, entityType, id, name, parents);

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
        var parents1 = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var facilityName2 = 'dls';
        var entityType2 = 'datafile';
        var id2 = 654321;
        var name2 = 'my test datafile';
        var parents2 = [{
            entityType: 'dataset',
            id: 456789
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName1, entityType1, id1, name1, parents1);
        Cart.addItem(facilityName2, entityType2, id2, name2, parents2);

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
        var parents = [{
            entityType: 'investigation',
            id: 7654321
        }];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };

        Cart.addItem(facilityName, entityType, id, name, parents);
        Cart.addItem(facilityName, entityType, id, name, parents);
        Cart.addItem(facilityName, entityType, id, name, parents);
        Cart.addItem(facilityName, entityType, id, name, parents);

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
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(2);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemsAdded', { added: 2 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 2, exists: 0 });
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });


    it('add items to cart with duplicates', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

        var cart = Cart.getCart();

        expect(cart.items.length).toEqual(2);

        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:itemsAdded', { added: 2 });
        expect($rootScope.$broadcast).toHaveBeenCalledWith('Cart:change', { added: 2, exists: 1 });
        expect($rootScope.$broadcast.calls.count()).toEqual(2);
    });

    it('remove an item from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

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
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

        $rootScope.$broadcast.calls.reset();

        Cart.removeItems([
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456
            },{
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457
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
                'id': 123456
            },{
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457
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
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

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
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(2);

        var item = Cart.getItem('dls', 'dataset', 123457);

        expect(item).toEqual(jasmine.any(CartItem));
        expect(item.toObject()).toEqual({
            'facilityName': 'dls',
            'userName': 'vcf21513',
            'entityType': 'dataset',
            'id': 123457,
            'name': 'my test dataset 2',
            'size': null,
            'availability': null,
            'parents': [
                {
                  'entityType': 'investigation',
                  'id': 7654321
                }
            ]
        });
    });


    it('has an item in cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
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

        Cart.addItems(items);

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
        };

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(0);
        Cart.setCart(myCart);
        expect(Cart.getCart().items.length).toEqual(1);
    });


    it('getLoggedInItems from a cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'isis',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        ];

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            },
            isis: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'uows/jane'
            }
        };

        Cart.addItems(items);

        var cart = Cart.getCart();
        expect(cart.items.length).toEqual(2);

        $sessionStorage.sessions = {
            dls: {
                sessionId: 'cedf298d-640e-44cf-900d-5f5bf22dddad',
                userName: 'vcf21513'
            }
        };


        var loggedInItems = Cart.getItems();

        console.log(JSON.stringify(loggedInItems, null, 2));

        expect(loggedInItems.length).toEqual(1);
        expect(loggedInItems[0].toObject()).toEqual(
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': null,
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        );

        $sessionStorage.$reset();
    });


    xit('getTotalItems from cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        ];

        Cart.addItems(items);

        expect(Cart.getTotalItems()).toEqual(2);

        Cart.removeAllItems();

        expect(Cart.getTotalItems()).toEqual(0);
    });

    xit('save cart', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        ];

        spyOn(Cart, 'save').and.callThrough();

        //deregister Cart:Change listener
        delete $rootScope.$$listeners['Cart:change'];

        Cart.addItems(items);
        Cart.save();

        expect(Cart.save).toHaveBeenCalled();

        var localStorage = CartStore.get();

        expect(localStorage.items.length).toEqual(2);
    });

    xit('restore cart after removing all items', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        ];

        spyOn(Cart, 'restore').and.callThrough();
        spyOn(Cart, 'save').and.callThrough();

        //deregister Cart:Change listener
        delete $rootScope.$$listeners['Cart:change'];

        //console.log(JSON.stringify($rootScope.$$listeners, null, 2));

        Cart.addItems(items);
        Cart.save();

        Cart.save.calls.reset();

        var localStorage = CartStore.get();

        expect(localStorage.items.length).toEqual(2);

        //console.log(JSON.stringify(CartStore.get(), null, 2));

        Cart.removeAllItems();
        expect(Cart.save).not.toHaveBeenCalled();

        //console.log(JSON.stringify(CartStore.get(), null, 2));

        expect(Cart.getTotalItems()).toEqual(0);

        Cart.restore();

        expect(Cart.restore).toHaveBeenCalled();
        //console.log(JSON.stringify(CartStore.get(), null, 2));

        expect(Cart.getTotalItems()).toEqual(2);
    });

    //TODO issue where CartStore is somehow linked with the cart in cart.service when it shouldn't
    //but it is a behaviour that we actually want
    /*xit('restore cart after removing one item', function() {
        var items = [
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123456,
                'name': 'my test dataset 1',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            },
            {
                'facilityName': 'dls',
                'entityType': 'dataset',
                'id': 123457,
                'name': 'my test dataset 2',
                'size': null,
                'availability': 'ONLINE',
                'parents': [
                    {
                      'entityType': 'investigation',
                      'id': 7654321
                    }
                ]
            }
        ];

        spyOn(Cart, 'restore').and.callThrough();
        spyOn(Cart, 'save').and.callThrough();

        //deregister Cart:Change listener
        delete $rootScope.$$listeners['Cart:change'];

        //console.log(JSON.stringify($rootScope.$$listeners, null, 2));

        Cart.addItems(items);
        Cart.save();

        Cart.save.calls.reset();

        var localStorage = CartStore.get();

        expect(localStorage.items.length).toEqual(2);

        //console.log(JSON.stringify(CartStore.get(), null, 2));

        Cart.removeItem('dls', 'dataset', 123456);
        expect(Cart.save).not.toHaveBeenCalled();

        //console.log(JSON.stringify(CartStore.get(), null, 2));

        expect(Cart.getTotalItems()).toEqual(1);

        Cart.restore();

        expect(Cart.restore).toHaveBeenCalled();
        //console.log(JSON.stringify(CartStore.get(), null, 2));

        expect(Cart.getTotalItems()).toEqual(2);
    });*/


});



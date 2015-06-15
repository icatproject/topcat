(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('Cart', Cart);

    Cart.$inject =['$rootScope', 'CartItem', 'CartStore', '$log'];

    function Cart($rootScope, CartItem, CartStore, $log) { //jshint ignore: line

        this.init = function(){
            this._cart = {
                items : []
            };
        };

        this.addItem = function (facilityName, entityType, id, name, parents) {
            var addedItemsCount = 0;
            var itemExistsCount = 0;
            //get item from cart
            var item = this.getItem(facilityName, entityType, id);


            if (typeof item === 'object'){
                $rootScope.$broadcast('Cart:itemExists', item);
                itemExistsCount++;
            } else {
                var newItem = new CartItem(facilityName, entityType, id, name);
                newItem.setParents(parents);
                this._cart.items.push(newItem);
                addedItemsCount++;
                $rootScope.$broadcast('Cart:itemAdded', {added: addedItemsCount});
            }

            //remove child items
            var itemsToRemove = [];

            _.each(this._cart.items, function(cartItem){
                //deal with items from the same facility
                if (!(cartItem.getFacilityName() === facilityName && cartItem.getEntityType() === entityType && cartItem.getId() === id)) {
                    if (cartItem.getFacilityName() === facilityName) {
                        _.each(cartItem.getParents(), function(parent) {
                            if (parent.entityType === entityType && parent.id === id) {
                                itemsToRemove.push(cartItem);
                            }
                        });

                    }
                }
            });

            _.each(itemsToRemove, function(item) {
                this.removeItem(item.getFacilityName(), item.getEntityType(), item.getId());
            }, this);


            $rootScope.$broadcast('Cart:change', {added: addedItemsCount, exists: itemExistsCount});
        };

        this.addItems = function (items) {
            var addedItemsCount = 0;
            var itemExistsCount = 0;

            _.each(items, function(item) {
                var myItem = this.getItem(item.facilityName, item.entityType, item.id);

                if (typeof myItem === 'object'){
                    itemExistsCount++;
                } else {
                    var newItem = new CartItem(item.facilityName, item.entityType, item.id, item.name);
                    newItem.setParents(item.parents);
                    this._cart.items.push(newItem);
                    addedItemsCount++;
                }
            }, this);

            if (addedItemsCount !== 0) {
                $rootScope.$broadcast('Cart:itemsAdded', {added: addedItemsCount});
            }

            if (addedItemsCount !== 0 || itemExistsCount !== 0) {
                $rootScope.$broadcast('Cart:change', {added: addedItemsCount, exists: itemExistsCount});
            }
        };


        /*this.addItemObjects = function (items) {
            var addedItems = [];
            var itemExists = [];

            _.each(items, function(item) {
                var myItem = this.getItem(item.getFacilityName(), item.getEntityType(), item.getId());

                if (typeof myItem === 'object'){
                    itemExists.push(myItem);
                } else {
                    var newItem = new CartItem(item.getFacilityName(), item.getEntityType(), item.getId(), item.getName());
                    this._cart.items.push(newItem);
                    addedItems.push(newItem);
                }
            });

            if (itemExists.length !== 0) {
                $rootScope.$broadcast('Cart:itemsExists', itemExists);
            }

            if (addedItems.length !== 0) {
                $rootScope.$broadcast('Cart:itemsAdded', addedItems);
            }

            if (addedItems.length !== 0 || itemExists.length !== 0) {
                $rootScope.$broadcast('Cart:change', {});
            }
        };*/


        this.removeItem = function (facilityName, entityType, id) {
            var removedItemsCount = 0;

            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (item.getFacilityName() === facilityName && item.getId() === id && item.getEntityType() === entityType);
            });

            if (matchIndex !== -1) {
                this.getCart().items.splice(matchIndex, 1);
                removedItemsCount++;
            }

            $rootScope.$broadcast('Cart:itemRemoved', {remove: removedItemsCount});
            $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
        };

        this.removeItems = function (items) {
            var removedItemsCount = 0;

            _.each(items, function(item) {
                var matchIndex = _.findIndex(this.getCart().items, function(cartItem) {
                    return (cartItem.getFacilityName() === item.facilityName && cartItem.getId() === item.id && cartItem.getEntityType() === item.entityType);
                });

                if (matchIndex !== -1) {
                    removedItemsCount++;
                    this.getCart().items.splice(matchIndex, 1);
                }

            }, this);

            $rootScope.$broadcast('Cart:itemRemoved', {removed: removedItemsCount});
            $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
        };

        this.removeAllItems = function () {
            var cart = this.getCart();
            var removedItemsCount = cart.items.length;

            cart.items = [];

            this.setCart(cart);
            $rootScope.$broadcast('Cart:itemRemoved', {removed: removedItemsCount});
            $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
        };


        this.getItem = function (facilityName, entityType, id) {
            var items = this.getCart().items;
            var result = false;

            _.each(items, function (item) {
                if  (item.getFacilityName() === facilityName && item.getId() === id && item.getEntityType() === entityType) {
                    result = item;
                    return;
                }
            });

            return result;
        };


        this.hasItem = function (facilityName, entityType, id) {
            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (facilityName === item.getFacilityName() && id === item.getId() && entityType === item.getEntityType());
            });

            if (matchIndex === -1) {
                return false;
            }

            return true;
        };

        this.setCart = function (cart) {
            this._cart = cart;
            return this.getCart();
        };

        this.getCart = function(){
            return this._cart;
        };

        this.getItems = function(){
            return this.getCart().items;
        };

        this.getLoggedInItems = function($sessionStorage){
            var filteredItems = _.filter(this.getCart().items, function(item) {
                return _.has($sessionStorage.sessions, item.getFacilityName());
            });

            return filteredItems;
        };

        this.getTotalItems = function(){
            return this.getCart().items.length;
        };

        this.save = function() {
            CartStore.set(this.getCart());
        };

        this.restore = function() {
            var _self = this;

            _self.init();

            var items = CartStore.get().items;

            /*_.each(CartStore.get().items, function(item) {
                items.push({
                    facilityName: item.facilityName,
                    entityType: item.entityType,
                    id: item.id,
                    name: item.name
                });
            });*/

            //add items to the cart
            _self.addItems(items);
        };

        this.isRestorable = function() {
            var cartSession = CartStore.get();

            if (typeof cartSession !== 'undefined') {
                if (cartSession.items.length > 0) {
                    return true;
                }
            }

            return false;
        };
    }
})();
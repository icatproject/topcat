(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('Cart', Cart);

    Cart.$inject =['$rootScope', 'APP_CONFIG', 'Config', 'CartItem', 'LocalStorageManager', '$sessionStorage', 'FacilityCart', '$log'];

    function Cart($rootScope, APP_CONFIG, Config, CartItem, LocalStorageManager, $sessionStorage, FacilityCart, $log) { //jshint ignore: line

        /**
         * Initialise a cart
         * @return {[type]} [description]
         */
        this.init = function() {
            this._cart = {
                items : []
            };
        };

        /**
         * Add an item to the cart
         *
         * @param {[type]} facilityName [description]
         * @param {[type]} entityType   [description]
         * @param {[type]} id           [description]
         * @param {[type]} name         [description]
         * @param {[type]} parents      [description]
         */
        this.addItem = function(facilityName, entityType, id, name, parents) {
            var addedItemsCount = 0;
            var itemExistsCount = 0;
            //get item from cart
            var item = this.getItem(facilityName, entityType, id);

            if (typeof item === 'object') {
                $rootScope.$broadcast('Cart:itemExists', item);
                itemExistsCount++;
            } else {
                var newItem = new CartItem(facilityName, $sessionStorage.sessions[facilityName].userName, entityType, id, name);
                newItem.setParents(parents);
                this._cart.items.push(newItem);
                addedItemsCount++;
                $rootScope.$broadcast('Cart:itemAdded', {added: addedItemsCount});
            }

            //remove child items
            var itemsToRemove = [];

            _.each(this._cart.items, function(cartItem) {
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

        /**
         * Add items to the cart
         *
         * @param {[type]} items [description]
         */
        this.addItems = function(items) {
            var addedItemsCount = 0;
            var itemExistsCount = 0;

            _.each(items, function(item) {
                var myItem = this.getItem(item.facilityName, item.entityType, item.id);

                if (typeof myItem === 'object') {
                    itemExistsCount++;
                } else {
                    var newItem = new CartItem(item.facilityName, $sessionStorage.sessions[item.facilityName].userName, item.entityType, item.id, item.name);
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

        /**
         * Remove an item from the cart
         *
         * @param  {[type]} facilityName [description]
         * @param  {[type]} entityType   [description]
         * @param  {[type]} id           [description]
         * @return {[type]}              [description]
         */
        this.removeItem = function(facilityName, entityType, id) {
            var removedItemsCount = 0;

            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (item.getFacilityName() === facilityName && item.getId() === id && item.getEntityType() === entityType);
            });

            if (matchIndex !== -1){
                this.getCart().items.splice(matchIndex, 1);
                removedItemsCount++;
            }

            if (removedItemsCount > 0) {
                $rootScope.$broadcast('Cart:itemRemoved', {removed: removedItemsCount});
                $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
            }
        };

        /**
         * Remove items from the cart
         *
         * @param  {[type]} items [description]
         * @return {[type]}       [description]
         */
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

            if (removedItemsCount > 0) {
                $rootScope.$broadcast('Cart:itemRemoved', {removed: removedItemsCount});
                $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
            }
        };

        /**
         * Remove all item in the cart
         *
         * @return {[type]} [description]
         */
        this.removeAllItems = function() {
            var cart = this.getCart();
            var removedItemsCount = cart.items.length;

            this.setCart({
                items : []
            });

            if (removedItemsCount > 0) {
                $rootScope.$broadcast('Cart:itemRemoved', {removed: removedItemsCount});
                $rootScope.$broadcast('Cart:change', {removed: removedItemsCount});
            }
        };

        /**
         * Reset the cart to empty
         *
         * @return {[type]} [description]
         */
        this.reset = function() {
            this.setCart({
                items : []
            });
        };

        /**
         * Remove the items for a facility user
         *
         * @param  {[type]} facilityName [description]
         * @param  {[type]} userName     [description]
         * @return {[type]}              [description]
         */
        this.removeUserItems = function(facilityName, userName) {
            var items = this.getCart().items;

            _.each(items, function (item, index) {
                if  (item.getFacilityName() === facilityName && item.getUserName() === userName) {
                    delete items[index];
                }
            });
        };

        /**
         *  Get an item from the cart
         *
         * @param  {[type]} facilityName [description]
         * @param  {[type]} entityType   [description]
         * @param  {[type]} id           [description]
         * @return {[type]}              [description]
         */
        this.getItem = function(facilityName, entityType, id) {
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

        /**
         * Check if the cart has an item
         *
         * @param  {[type]}  facilityName [description]
         * @param  {[type]}  entityType   [description]
         * @param  {[type]}  id           [description]
         * @return {Boolean}              [description]
         */
        this.hasItem = function(facilityName, entityType, id) {
            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (facilityName === item.getFacilityName() && id === item.getId() && entityType === item.getEntityType());
            });

            if (matchIndex === -1) {
                return false;
            }

            return true;
        };

        /**
         * Set the cart
         *
         * @param {[type]} cart [description]
         */
        this.setCart = function(cart) {
            this._cart = cart;
            return this.getCart();
        };

        /**
         * Get the entire cart
         *
         * @return {[type]} [description]
         */
        this.getCart = function(){
            return this._cart;
        };

        /**
         * Get the items in the cart
         *
         * @return {[type]} [description]
         */
        this.getItems = function(){
            return this.getCart().items;
        };

        /**
         * Get the total items in the cart
         *
         * @return {[type]} [description]
         */
        this.getTotalItems = function(){
            return this.getCart().items.length;
        };

        /**
         * Save the cart to the browser localstorage
         *
         * @return {[type]} [description]
         */
        this.save = function() {
            LocalStorageManager.setStore(this.getCart());
        };

        /**
         * Restore a list of items to the cart
         *
         * @param  {[type]} items [description]
         * @return {[type]}       [description]
         */
        this._restoreItems = function(items) {
            var restoreItemsCount = 0;
            var itemExistsCount = 0;

            _.each(items, function(item) {
                var myItem = this.getItem(item.facilityName, item.entityType, item.id);

                if (typeof myItem === 'object') {
                    itemExistsCount++;
                } else {
                    var newItem = new CartItem(item.facilityName, item.userName, item.entityType, item.id, item.name);
                    newItem.setParents(item.parents);
                    this._cart.items.push(newItem);
                    restoreItemsCount++;
                }
            }, this);

            if (restoreItemsCount !== 0) {
                $rootScope.$broadcast('Cart:itemsRestored', {added: restoreItemsCount});
            }
        };

        /**
         * Restore all the current logged in users cart from localstorage
         *
         * @return {[type]} [description]
         */
        this.restore = function() {
            var _self = this;

            //clear all
            _self.reset();

            //restore cart for each logged in session
            _.each($sessionStorage.sessions, function(session, key) {
                var facility = Config.getFacilityByName(APP_CONFIG, key);
                var items = LocalStorageManager.getUserStore(facility, session.userName);

                _self._restoreItems(items.items);
            });

        };

        /**
         * Check whether a cart can be restored. (If there are logged in users)
         *
         * @return {Boolean} [description]
         */
        this.isRestorable = function() {
            //var cartSession = LocalStorageManager.getLocalStorage();

            //must has at least one session
            if (_.size($sessionStorage.sessions) > 0) {
                return true;
                //must have a cart
                /*if (typeof cartSession !== 'undefined') {
                    //must have items in the cart
                    if (cartSession.items.length > 0) {
                        return true;
                    }
                }*/
            }

            return false;
        };


        /*this.getFacilitiesCart = function(){
            var facilityCarts = {};

            _.each(this.getItems(), function(item) {
                //initialise array
                if (typeof facilityCarts[item.getFacilityName()] === 'undefined') {
                    facilityCarts[item.facilityName] = [];
                }

                facilityCarts[item.facilityName].push(item);
            });

            $log.debug('facilityCarts', facilityCarts);

            return facilityCarts;
        };*/

        this.getFacilitiesCart = function(){
            var facilityCarts = {};

            _.each(this.getItems(), function(item) {
                //initialise array
                if (typeof facilityCarts[item.getFacilityName()] === 'undefined') {
                    facilityCarts[item.facilityName] = new FacilityCart(item.facilityName);
                }

                facilityCarts[item.facilityName].addItem(item);
            });

            return facilityCarts;
        };
    }
})();
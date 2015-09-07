(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('Cart', Cart);

    Cart.$inject =['$rootScope', '$q', 'APP_CONFIG', 'Config', 'CartItem', 'RemoteStorageManager', '$sessionStorage', 'FacilityCart', 'CartRequest', 'TopcatManager', 'inform', 'SmartClientManager', 'SmartClientPollManager', '$translate', '$log'];

    function Cart($rootScope, $q, APP_CONFIG, Config, CartItem, RemoteStorageManager, $sessionStorage, FacilityCart, CartRequest, TopcatManager, inform, SmartClientManager, SmartClientPollManager, $translate, $log) { //jshint ignore: line
        var self  = this;

        function add(facilityName, entityType, entityId, name, parentEntities) {
            var addedItemsCount = 0;
            var itemExistsCount = 0;
            //get item from cart
            var item = self.getItem(facilityName, entityType, entityId);

            if (typeof item === 'object') {
                $rootScope.$broadcast('Cart:itemExists', item);
                itemExistsCount++;
            } else {
                var newItem = new CartItem(facilityName, $sessionStorage.sessions[facilityName].userName, entityType, entityId, name);
                newItem.setParentEntities(parentEntities);

                $log.debug('newItem', newItem);

                $log.debug('newItem', self._cart);

                self._cart.items.push(newItem);
                addedItemsCount++;
                $rootScope.$broadcast('Cart:itemAdded', {added: addedItemsCount});
            }

            //remove child items
            var itemsToRemove = [];

            _.each(self._cart.items, function(cartItem) {
                //deal with items from the same facility
                if (!(cartItem.getFacilityName() === facilityName && cartItem.getEntityType() === entityType && cartItem.getEntityId() === entityId)) {
                    if (cartItem.getFacilityName() === facilityName) {
                        _.each(cartItem.getParentEntities(), function(parent) {
                            if (parent.entityType === entityType && parent.entityId === entityId) {
                                itemsToRemove.push(cartItem);
                            }
                        });
                    }
                }
            });

            _.each(itemsToRemove, function(item) {
                this.removeItem(item.getFacilityName(), item.getEntityType(), item.getEntityId());
            }, self);


            return {added: addedItemsCount, exists: itemExistsCount};
        }

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
         * @param {[type]} entityId     [description]
         * @param {[type]} name         [description]
         * @param {[type]} parentEntities      [description]
         */
        this.addItem = function(facilityName, entityType, entityId, name, parentEntities) {
            var result = add(facilityName, entityType, entityId, name, parentEntities);

            $rootScope.$broadcast('Cart:change', result);
        };

        this.restoreItem = function(facilityName, entityType, entityId, name, parentEntities) {
            add(facilityName, entityType, entityId, name, parentEntities);
        };




        /**
         * Remove an item from the cart
         *
         * @param  {[type]} facilityName [description]
         * @param  {[type]} entityType   [description]
         * @param  {[type]} entityId     [description]
         * @return {[type]}              [description]
         */
        this.removeItem = function(facilityName, entityType, entityId) {
            var removedItemsCount = 0;

            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (item.getFacilityName() === facilityName && item.getEntityId() === entityId && item.getEntityType() === entityType);
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
                    return (cartItem.getFacilityName() === item.facilityName && cartItem.getEntityId() === item.entityId && cartItem.getEntityType() === item.entityType);
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
            cart.items.splice(0, removedItemsCount);

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
         * @param  {[type]} entityId     [description]
         * @return {[type]}              [description]
         */
        this.getItem = function(facilityName, entityType, entityId) {
            var items = this.getCart().items;
            var result = false;

            _.each(items, function (item) {
                if  (item.getFacilityName() === facilityName && item.getEntityId() === entityId && item.getEntityType() === entityType) {
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
         * @param  {[type]}  entityId     [description]
         * @return {Boolean}              [description]
         */
        this.hasItem = function(facilityName, entityType, entityId) {
            var matchIndex = _.findIndex(this.getCart().items, function(item) {
                return (facilityName === item.getFacilityName() && entityId === item.getEntityId() && entityType === item.getEntityType());
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
            $log.debug('save called');
            RemoteStorageManager.setStore(this.getCart());
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
                var myItem = this.getItem(item.facilityName, item.entityType, item.entityId);

                if (typeof myItem === 'object') {
                    itemExistsCount++;
                } else {
                    var newItem = new CartItem(item.facilityName, item.userName, item.entityType, item.entityId, item.name);
                    newItem.setParentEntities(item.parentEntities);
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
            var def = $q.defer();

            //clear all
            _self.reset();

            //restore cart for each logged in session
            _.each($sessionStorage.sessions, function(session, key) {
                var facility = Config.getFacilityByName(APP_CONFIG, key);

                RemoteStorageManager.getUserStore(facility, session.userName).then(function(items) {
                    _self._restoreItems(items);
                    def.resolve({restored: true});
                }, function(error) {
                    def.reject({restored: false});
                    inform.add(error, {
                        'ttl': 4000,
                        'type': 'danger'
                    });
                });
            });

            $rootScope.$broadcast('Cart:Restored', {});

            return def.promise;
        };

        /**
         * Check whether a cart can be restored. (If there are logged in users)
         *
         * @return {Boolean} [description]
         */
        this.isRestorable = function() {
            //var cartSession = RemoteStorageManager.getLocalStorage();

            //must has at least one session
            if (_.size($sessionStorage.sessions) > 0) {
                return true;
            }

            return false;
        };

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


        this.submit = function(downloadRequests) {

            _.each(downloadRequests, function(downloadRequest) {
                var facility = Config.getFacilityByName(APP_CONFIG, downloadRequest.facilityName);

                var cartRequest = new CartRequest(
                    facility.facilityName,
                    $sessionStorage.sessions[downloadRequest.facilityName].userName,
                    $sessionStorage.sessions[downloadRequest.facilityName].sessionId,
                    facility.icatUrl,
                    downloadRequest.fileName,
                    downloadRequest.availability,
                    downloadRequest.transportType.type,
                    downloadRequest.transportType.url,
                    downloadRequest.email
                );

                TopcatManager.submitCart(facility, cartRequest).then(function(data){
                    if (downloadRequest.transportType.type === 'smartclient') {
                        SmartClientManager.getData($sessionStorage.sessions[downloadRequest.facilityName].sessionId, facility, data.value).then(function(){
                            //$log.debug('Job submitted to Smartclient', data);

                            //start a poll
                            SmartClientPollManager.createPoller(facility, $sessionStorage.sessions[downloadRequest.facilityName].userName, data.value);
                        }, function(error) {
                            inform.add($translate.instant('CART.SUBMIT.NOTIFY_MESSAGE.FAILURE', {'errorMEssage' : error}), {
                                'ttl': 4000,
                                'type': 'danger'
                            });
                        });
                    }

                    inform.add($translate.instant('CART.SUBMIT.NOTIFY_MESSAGE.SUCCESS'), {
                        'ttl': 4000,
                        'type': 'success'
                    });
                }, function(error) {
                    inform.add($translate.instant('CART.SUBMIT.NOTIFY_MESSAGE.ERROR', {'errorMEssage' : error}), {
                        'ttl': 4000,
                        'type': 'danger'
                    });
                });
            });
        };

        $rootScope.$on('Cart:change', function(){
            self.save();
        });

        $rootScope.$on('Logout:success', function(){
            if (self.isRestorable()) {
                self.restore();
            }

        });

        $rootScope.$on('SESSION:EXPIRED', function(event, data){
            if (typeof data.facilityName !== 'undefined' && typeof data.userName !== 'undefined') {
                self.removeUserItems(data.facilityName, data.userName);
            }
        });
    }
})();
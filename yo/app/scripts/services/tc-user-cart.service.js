

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUserCart', function($q, $rootScope, $timeout, helpers, tcIcatEntity){

    	this.create = function(attributes, user){
    		return new Cart(attributes, user);
    	};

        /**
         * @interface Cart
         */
        function Cart(attributes, user){
            _.merge(this, attributes);
            var facility = user.facility();
            var that = this;

            /**
             * Tests to see if item is already in Cart.
             *
             * @method
             * @name  Cart#isCartItem
             * @param  {string}  entityType can be 'investigation', 'dataset' or 'datafile'
             * @param  {number}  entityId the id of the entity
             * @return {boolean}
             */
            this.isCartItem = function(entityType, entityId){
                var out = false;
                entityType = entityType.toLowerCase();
                _.each(this.cartItems, function(cartItem){
                    if(cartItem.entityType == entityType && cartItem.entityId == entityId){
                        out = true;
                        return false;
                    }
                });
                return out;
            };

            this.getSize = helpers.overload({
                /**
                 * Gets the total size of the Cart.
                 *
                 * @method
                 * @name  Cart#getSize
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<number>} the total size in bytes (defered)
                 */
                'object': function(options){
                    var defered = $q.defer();
                    var out = 0;

                    helpers.throttle(10, 1, options.timeout, this.cartItems, function(cartItem){
                        return facility.icat().getSize(cartItem.entityType, cartItem.entityId,options).then(function(size){
                        	// Don't change total if getSize for some other cartItem has failed
                            if(out != -1 ) out += size;
                            defered.notify(out);
                        }, function(response){
                        	// error handler - getSize request failed
                        	var msg = response?' entity getSize failed: ' + response.code + ", " + response.message : ' response is null';
                        	console.log('cart item getSize failed: ' + msg);
                        	// use -1 for "size unknown"
                        	out = -1
                        	defered.reject(response);
                        });
                    }).then(function(){
                        defered.notify(out);
                        return defered.resolve(out);
                    });

                    return defered.promise;
                },

                /**
                 * Gets the total size of the Cart.
                 *
                 * @method
                 * @name  Cart#getSize
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<number>} the total size in bytes (defered)
                 */
                'promise': function(timeout){
                    return this.getSize({timeout: timeout});
                },

                /**
                 * Gets the total size of the Cart.
                 *
                 * @method
                 * @name  Cart#getSize
                 * @return {Promise<number>} the total size in bytes (defered)
                 */
                '': function(){
                    return this.getSize({});
                }
            });

            this.getDatafileCount = helpers.overload({
                /**
                 * The total number of datafiles being requested in this Cart.
                 *
                 * @method
                 * @name  Cart#getDatafileCount
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<number>} the total number of datafiles being requested (defered)
                 */
                'object': function(options){
                    var defered = $q.defer();
                    var out = 0;

                    helpers.throttle(10, 1, options.timeout, this.cartItems, function(cartItem){
                        if(cartItem.entityType == 'investigation' || cartItem.entityType == 'dataset'){
                            var entity = tcIcatEntity.create({entityType: cartItem.entityType, id: cartItem.entityId}, facility);
                            return entity.getDatafileCount(options).then(function(datafileCount){
                                out += datafileCount;
                                defered.notify(out); 
                            });
                        } else {
                            out++;
                            defered.notify(out);
                            return $q.resolve();
                        }
                    }).then(function(){
                        defered.notify(out);
                        return defered.resolve(out);
                    });

                    return defered.promise;
                },

                /**
                 * The total number of datafiles being requested in this Cart.
                 *
                 * @method
                 * @name  Cart#getDatafileCount
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<number>} the total number of datafiles being requested (defered)
                 */
                'promise': function(timeout){
                    return this.getDatafileCount({timeout: timeout});
                },

                /**
                 * The total number of datafiles being requested in this Cart.
                 *
                 * @method
                 * @name  Cart#getDatafileCount
                 * @return {Promise<number>} the total number of datafiles being requested (defered)
                 */
                '': function(){
                  return this.getDatafileCount({});
                }
            });

            _.each(this.cartItems, function(cartItem){
                /**
                 * @interface CartItem
                 */

                cartItem.facilityName = facility.config().name;

                /**
                 * Deletes this item from the cart and returns the updated Cart.
                 *
                 * @method
                 * @name  CartItem#delete
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>} the updated Cart (defered)
                 */
                cartItem.delete = helpers.overload({
                    'object': function(options){
                        return user.deleteCartItem(this.id, options);
                    },

                    /**
                     * Deletes this item from the cart and returns the updated Cart.
                     *
                     * @method
                     * @name  CartItem#delete
                     * @param  {Promise} timeout if resolved will cancel the request
                     * @return {Promise<Cart>} the updated Cart (defered)
                     */
                    'promise': function(timeout){
                        return this.delete({timeout: timeout});
                    },

                    /**
                     * Deletes this item from the cart and returns the updated Cart.
                     *
                     * @method
                     * @name  CartItem#delete
                     * @return {Promise<Cart>} the updated Cart (defered)
                     */
                    '': function(){
                        return this.delete({});
                    }
                });


                cartItem.entity = helpers.overload({
                    /**
                     * Gets the corresponding IcatEntity.
                     *
                     * @method
                     * @name  CartItem#entity
                     * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                     * @return {Promise<IcatEntity>}
                     */
                    'object': function(options){
                        return facility.icat().query([
                            "select ? from ? ? where ?.id = ?",
                            this.entityType.safe(),
                            helpers.capitalize(this.entityType).safe(),
                            this.entityType.safe(),
                            this.entityType.safe(),
                            this.entityId
                        ], options).then(function(entities){
                            return entities[0];
                        });
                    },

                    /**
                     * Gets the corresponding IcatEntity.
                     *
                     * @method
                     * @name  CartItem#entity
                     * @param  {Promise} timeout if resolved will cancel the request
                     * @return {Promise<IcatEntity>}
                     */
                    'promise': function(timeout){
                        return this.entity({timeout: timeout});
                    },

                    /**
                     * Gets the corresponding IcatEntity.
                     *
                     * @method
                     * @name  CartItem#entity
                     * @return {Promise<IcatEntity>}
                     */
                    '': function(){
                        return this.entity({});
                    }
                });

                cartItem.getSize = helpers.overload({
                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#getSize
                     * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    'object': function(options){
                        var that = this;

                        return this.entity(options).then(function(entity){
                            if(cartItem.entityType == 'datafile'){
                                that.size = entity.fileSize;
                                return $q.resolve(entity.fileSize);
                            } else {
                                return entity.getSize(options).then(function(size){
                                    that.size = size;
                                    return size;
                                });
                            }
                        });
                    },

                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#getSize
                     * @param  {Promise} timeout if resolved will cancel the request
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    'promise': function(timeout){
                        return this.getSize({timeout: timeout});
                    },

                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#getSize
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    '': function(){
                        return this.getSize({});
                    }
                });

                cartItem.getDatafileCount = helpers.overload({
                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#datafileCount
                     * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    'object': function(options){
                        var that = this;

                        return this.entity(options).then(function(entity){
                            if(cartItem.entityType == 'datafile'){
                                that.datafileCount = 1;
                                return $q.resolve(that.datafileCount);
                            } else {
                                return entity.getDatafileCount(options).then(function(datafileCount){
                                    that.datafileCount = datafileCount;
                                    return datafileCount;
                                });
                            }
                        });
                    },

                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#datafileCount
                     * @param  {Promise} timeout if resolved will cancel the request
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    'promise': function(timeout){
                        return this.getDatafileCount({timeout: timeout});
                    },

                    /**
                     * Gets the size of this item.
                     *
                     * @method
                     * @name  CartItem#datafileCount
                     * @return {Promise<number>} the size in bytes (defered)
                     */
                    '': function(){
                        return this.getDatafileCount({});
                    }
                });

            });

            

            $timeout(function(){
                var timeout = $q.defer();

                var stopListeningForCartOpen = $rootScope.$on('cart:open', function(){
                    stopListeningForCartOpen();
                    stopListeningForCartChange();
                    timeout.resolve();
                });

                var stopListeningForCartChange = $rootScope.$on('cart:change', function(){
                    stopListeningForCartOpen();
                    stopListeningForCartChange();
                    timeout.resolve();
                });

                helpers.throttle(10, 10, timeout.promise, that.cartItems, function(cartItem){
                    return cartItem.getSize({
                        timeout: timeout.promise,
                        bypassInterceptors: true
                    });
                });
            });
            

            helpers.mixinPluginMethods('cart', this);
        }

    });

})();

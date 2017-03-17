

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
                            out += size;
                            defered.notify(out);
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

                cartItem.delete = helpers.overload({
                    'object': function(options){
                        return user.deleteCartItem(this.id, options);
                    },
                    'promise': function(timeout){
                        return this.delete({timeout: timeout});
                    },
                    '': function(){
                        return this.delete({});
                    }
                });


                cartItem.entity = helpers.overload({
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
                    'promise': function(timeout){
                        return this.entity({timeout: timeout});
                    },
                    '': function(){
                        return this.entity({});
                    }
                });

                cartItem.getSize = helpers.overload({
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
                    'promise': function(timeout){
                        return this.getSize({timeout: timeout});
                    },
                    '': function(){
                        return this.getSize({});
                    }
                });

            });

            

            $timeout(function(){
                var timeout = $q.defer();

                var stopListeningForCartOpen = $rootScope.$on('cart:open', function(){
                    stopListeningForCartOpen();
                    stopListeningForCartChange();
                    timeout.resolve();
                    console.log('cart open');
                });

                var stopListeningForCartChange = $rootScope.$on('cart:change', function(){
                    stopListeningForCartOpen();
                    stopListeningForCartChange();
                    timeout.resolve();
                    console.log('cart change');
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

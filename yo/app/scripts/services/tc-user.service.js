

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUser', function($q, $rootScope, helpers, tcUserCart){

        this.create = function(facility){
            return new User(facility);
        };

        /**
         * @interface User
         */
        function User(facility){
            var that = this;

            this.facility = function(){
                return facility;
            };

            this.downloads = helpers.overload({
                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'array, object': function(queryOffset, options){
                    queryOffset = helpers.buildQuery(queryOffset);
                    queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().name) + (queryOffset ? " AND " + queryOffset.replace(/^\s*where\s*/, '') : "");

                    return this.get('downloads', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        queryOffset: queryOffset
                    }, options).then(function(downloads){
                        _.each(downloads, function(download){

                            download.delete = helpers.overload({
                                'object': function(options){
                                    return that.deleteDownload(this.id, options);
                                },
                                'promise': function(timeout){
                                    return this.delete({timeout: timeout});
                                },
                                '': function(){
                                    return this.delete({});
                                }
                            });

                        });

                        return downloads;
                    });
                },

                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise, array': function(timeout, queryOffset){
                    return this.downloads(queryOffset, {timeout: timeout});
                },

                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'array': function(queryOffset){
                    return this.downloads(queryOffset, {});
                },

                 /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {string} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise, string': function(timeout, queryOffset){
                    return this.downloads([queryOffset], {timeout: timeout});
                },

                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param  {string} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'string': function(queryOffset){
                    return this.downloads([queryOffset]);
                },

                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise': function(timeout){
                    return this.downloads({timeout: timeout});
                },

                /**
                 * Returns the current user's downloads. 
                 *
                 * @method
                 * @name  User#downloads
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                '': function(){
                    return this.downloads([], {});
                }
            });

            this.deleteDownload = helpers.overload({
                /**
                 * @method
                 * @name  User#deleteDownload
                 * @param  {string|number} id
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        id: id,
                        value: 'true'
                    }, options).then(function(){
                        $rootScope.$broadcast('download:change');
                    });
                },

                /**
                 * @method
                 * @name  User#deleteDownload
                 * @param  {string|number} id
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise}
                 */
                'string, promise': function(id, timeout){
                    return this.deleteDownload(id, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#deleteDownload
                 * @param  {string|number} id
                 * @return {Promise}
                 */
                'string': function(id){
                    return this.deleteDownload(id, {});
                },
                'number, object': function(id, options){
                    return this.deleteDownload("" + id, options);
                },
                'number, promise': function(id, timeout){
                    return this.deleteDownload("" + id, {timeout: timeout});
                },
                'number': function(id){
                    return this.deleteDownload("" + id, {});
                }
            });

            this.setDownloadStatus = helpers.overload({
                /**
                 * @method
                 * @name  User#setDownloadStatus
                 * @param  {string|number} id
                 * @param {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, string, object': function(id, status, options){
                    return this.put('download/' + id + '/status', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        value: status
                    }, options);
                },

                /**
                 * @method
                 * @name  User#setDownloadStatus
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @param  {string|number} id
                 * @param {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @return {Promise}
                 */
                'promise, string, string': function(timeout, id, status){
                    return this.setDownloadStatus(id, status, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#setDownloadStatus
                 * @param  {string|number} id
                 * @param {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @return {Promise}
                 */
                'string, string': function(id, status){
                    return this.setDownloadStatus(id, status, {});
                },
                'number, string, object': function(id, status, options){
                    return this.setDownloadStatus("" + id, status, options);
                },
                'promise, number, string': function(timeout, id, status){
                    return this.setDownloadStatus("" + id, status, {timeout: timeout});
                },
                'number, string': function(id, status){
                    return this.setDownloadStatus("" + id, status, {});
                }
            });

            var cartCache;

            this.clearCartCache = function(){
                console.log("user for " + facility.config().name + ": clearing cartCache");
                cartCache = null;
            }
            
            this.cart = helpers.overload({
                /**
                 * @method
                 * @name  User#cart
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'object': function(options){
                    if(cartCache){
                        console.log("user.cart(): cartCache exists, so using it");
                        var defered = $q.defer();
                        defered.resolve(cartCache);
                        return defered.promise;
                    }

                    return this.get('cart/' + facility.config().name, {
                        sessionId: facility.icat().session().sessionId
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        console.log("user.cart(): cartCache does not exist, so doing GET");
                        cartCache = cart;
                        return cart;
                    });
                },

                /**
                 * @method
                 * @name  User#cart
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<Cart>}
                 */
                'promise': function(timeout){
                    return this.cart({timeout: timeout})
                },

                /**
                 * @method
                 * @name  User#cart
                 * @return {Promise<Cart>}
                 */
                '': function(){
                    return this.cart({});
                }
            });

            this.addCartItems = helpers.overload({
                /**
                 * @method
                 * @name  User#addCartItems
                 * @param {array} items
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'array, object': function(items, options){
                    return this.cart(options).then(function(cart){
                        var filteredItems = [];
                        _.each(items, function(item){
                            if(!cart.isCartItem(item.entityType, item.entityId)){
                                filteredItems.push(item);
                            }
                        });

                        items = _.map(filteredItems, function(item){ return item.entityType + " " + item.entityId; }).join(',');
                        
                        if(items != ''){
                            return that.post('cart/' + facility.config().name + '/cartItems', {
                                sessionId: facility.icat().session().sessionId,
                                items: items
                            }, options).then(function(cart){
                                cart = tcUserCart.create(cart, that);
                                cartCache = cart;
                                $rootScope.$broadcast('cart:change');
                                return cart;
                            });
                        }

                        return cart;
                    });
                },

                /**
                 * @method
                 * @name  User#addCartItems
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @param {array} items
                 * @return {Promise<Cart>}
                 */
                'promise, array': function(timeout, items){
                    return this.addCartItems(items, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#addCartItems
                 * @param {array} items
                 * @return {Promise<Cart>}
                 */
                'array': function(items){
                    return this.addCartItems(items, {});
                }
            });

            this.addCartItem = helpers.overload({
                /**
                 * @method
                 * @name  User#addCartItem
                 * @param {string} entityType
                 * @param {number} entityId
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'string, number, object': function(entityType, entityId, options){
                    return this.addCartItems([{entityType: entityType, entityId: entityId}], options);
                },

                /**
                 * @method
                 * @name  User#addCartItem
                 * @param {string} entityType
                 * @param {number} entityId
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<Cart>}
                 */
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.addCartItem(entityType, entityId, {timeout: timeout})
                },

                /**
                 * @method
                 * @name  User#addCartItem
                 * @param {string} entityType
                 * @param {number} entityId
                 * @return {Promise<Cart>}
                 */
                'string, number': function(entityType, entityId){
                    return this.addCartItem(entityType, entityId, {});
                }
            });

            this.deleteCartItems = helpers.overload({
                /**
                 * @method
                 * @name  User#deleteCartItems
                 * @param {array} items
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'array, object': function(items, options){
                    if(typeof items[0] == 'object'){
                        items = _.map(items, function(item){ return item.entityType + " " + item.entityId; });
                    }

                    var currentItems = [];
                    var currentUrlLength;
                    var chunks = [];

                    while(items.length > 0){
                        currentUrlLength = this.deleteUrlLength('cart/' + facility.config().name + '/cartItems', {
                            sessionId: facility.icat().session().sessionId,
                            items: currentItems.concat([items[items.length - 1]]).join(',')
                        });

                        if(currentUrlLength < 1000){
                            currentItems.push(items.pop());
                        } else {
                            chunks.push(currentItems);
                            currentItems = [];
                        }
                    }

                    if(currentItems.length > 0) chunks.push(currentItems);

                    function deleteChunks(){
                        return that.delete('cart/' + facility.config().name + '/cartItems', {
                            sessionId: facility.icat().session().sessionId,
                            items: chunks.pop()
                        }, options).then(function(cart){
                            if(chunks.length > 0){
                                return deleteChunks();
                            } else {
                                cart = tcUserCart.create(cart, that);
                                cartCache = cart;
                                $rootScope.$broadcast('cart:change');
                                return $q.resolve(cart);
                            }
                        });
                    }

                    return deleteChunks();
                },

                /**
                 * @method
                 * @name  User#deleteCartItems
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param {array} items
                 * @return {Promise<Cart>}
                 */
                'promise, array': function(timeout, items){
                    return this.deleteCartItems(items, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#deleteCartItems
                 * @param {array} items
                 * @return {Promise<Cart>}
                 */
                'array': function(items){
                    return this.deleteCartItems(items, {});
                }
            });

            this.deleteCartItem = helpers.overload({
                /**
                 * @method
                 * @name  User#deleteCartItem
                 * @param {string|number} id
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'number, object': function(id, options){
                    return this.deleteCartItems([id], options);
                },

                /**
                 * @method
                 * @name  User#deleteCartItem
                 * @param {string|number} id
                 * @param {Promise} timeout if resolved will cancel the request
                 * @return {Promise<Cart>}
                 */
                'number, promise': function(id, timeout){
                    return this.deleteCartItem(id, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#deleteCartItem
                 * @param {string|number} id
                 * @return {Promise<Cart>}
                 */
                'number': function(id){
                    return this.deleteCartItem(id, {});
                },
                'string, number, object': function(entityType, entityId, options){
                    return this.deleteCartItems([{entityType: entityType, entityId: entityId}], options);
                },
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.deleteCartItem(entityType, entityId, {timeout: timeout});
                },
                'string, number': function(entityType, entityId){
                    return this.deleteCartItem(entityType, entityId, {});
                }
            });

            this.deleteAllCartItems = helpers.overload({
                /**
                 * @method
                 * @name  User#deleteAllCartItems
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'object': function(options){
                    return this.delete('cart/' + facility.config().name + '/cartItems', {
                        sessionId: facility.icat().session().sessionId,
                        items: "*"
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        cartCache = cart;
                        $rootScope.$broadcast('cart:change');
                        return cart;
                    });
                },

                /**
                 * @method
                 * @name  User#deleteAllCartItems
                 * @param {Promise} timeout if resolved will cancel the request
                 * @return {Promise<Cart>}
                 */
                'promise': function(timeout){
                    return this.deleteAllCartItems({timeout: timeout})
                },

                /**
                 * @method
                 * @name  User#deleteAllCartItems
                 * @return {Promise<Cart>}
                 */
                '': function(){
                    return this.deleteAllCartItems({});
                }
            });

            this.submitCart = helpers.overload({
                /**
                 * @method
                 * @name  User#submitCart
                 * @param  {string} fileName
                 * @param  {string} transport
                 * @param  {string} email
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<Cart>}
                 */
                'string, string, string, object': function(fileName, transport, email, options){
                    var transportTypeIndex = {};
                    _.each(facility.config().downloadTransportTypes, function(downloadTransportTypes){
                        transportTypeIndex[downloadTransportTypes.type] = downloadTransportTypes
                    })
                    var transportType = transportTypeIndex[transport];

                    return this.post('cart/' + facility.config().name + '/submit', {
                        sessionId: facility.icat().session().sessionId,
                        fileName: fileName,
                        transport: transport,
                        email: email,
                        zipType: transportType.zipType ? transportType.zipType : ''
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        cartCache = cart;
                        $rootScope.$broadcast('download:change');
                        $rootScope.$broadcast('cart:change');

                        return cart;
                    });
                },

                /**
                 * @method
                 * @name  User#submitCart
                 * @param  {string} fileName
                 * @param  {string} transport
                 * @param  {string} email
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<Cart>}
                 */
                'string, string, string, promise': function(fileName, transport, email, timeout){
                    return this.submitCart(fileName, transport, email, {timeout: timeout});
                },

                /**
                 * @method
                 * @name  User#submitCart
                 * @param  {string} fileName
                 * @param  {string} transport
                 * @param  {string} email
                 * @return {Promise<Cart>}
                 */
                'string, string, string': function(fileName, transport, email){
                    return this.submitCart(fileName, transport, email, {});
                }
            });

            helpers.generateRestMethods(this, facility.tc().config().topcatUrl + "/topcat/user/");

            helpers.mixinPluginMethods('user', this);
        }

    });

})();

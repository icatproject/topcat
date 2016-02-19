

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcUser', function($q, helpers){

    	this.create = function(facility){
    		return new User(facility);
    	};

        function User(facility){
            var that = this;

            this.downloads = helpers.overload({
                'object, object': function(params, options){
                    params.queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().facilityName) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

                    return this.get('downloads', _.merge({
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId
                    }, params), options).then(function(downloads){
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
                'promise, array': function(timeout, queryOffset){
                    return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {timeout: timeout});
                },
                'array': function(queryOffset){
                    return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {});
                },
                'promise, string': function(timeout, queryOffset){
                    return this.downloads([queryOffset], {timeout: timeout});
                },
                'string': function(queryOffset){
                    return this.downloads([queryOffset]);
                },
                'promise': function(timeout){
                    return this.downloads(params, {timeout: timeout});
                },
                '': function(){
                    return this.downloads({}, {});
                }
            });

            this.deleteDownload = helpers.overload({
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        id: id,
                        value: 'true'
                    }, options).then(function(){
                        $rootScope.$broadcast('download:change');
                    });
                },
                'string, promise': function(id, timeout){
                    return this.deleteDownload(id, {timeout: timeout});
                },
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

            var cartCache;
            this.cart = helpers.overload({
                'object': function(options){
                    if(cartCache){
                        var defered = $q.defer();
                        defered.resolve(cartCache);
                        return defered.promise;
                    }

                    return this.get('cart/' + facility.config().facilityName, {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId
                    }, options).then(function(cart){
                        extendCart(cart);
                        cartCache = cart;
                        return cart;
                    });
                },
                'promise': function(timeout){
                    return this.cart({timeout: timeout})
                },
                '': function(){
                    return this.cart({});
                }
            });

            this.addCartItem = helpers.overload({
                'string, number, object': function(entityType, entityId, options){
                    return this.cart(options).then(function(cart){
                        if(cart.isCartItem(entityType, entityId)){
                            return cart;
                        } else {
                            return that.post('cart/' + facility.config().facilityName + '/cartItem', {
                                icatUrl: facility.config().icatUrl,
                                sessionId: facility.icat().session().sessionId,
                                entityType: entityType,
                                entityId: entityId
                            }, options).then(function(cart){
                                extendCart(cart);
                                cartCache = cart;
                                $rootScope.$broadcast('cart:change');
                                return cart;
                            });
                        }
                    });
                },
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.addCartItem(entityType, entityId, {timeout: timeout})
                },
                'string, number': function(entityType, entityId){
                    return this.addCartItem(entityType, entityId, {});
                }
            });

            this.deleteCartItem = helpers.overload({
                'number, object': function(id, options){
                    return this.delete('cart/' + facility.config().facilityName + '/cartItem/' + id, {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId
                    }, options).then(function(cart){
                        extendCart(cart);
                        cartCache = cart;
                        $rootScope.$broadcast('cart:change');
                        return cart;
                    });
                },
                'number, promise': function(id, timeout){
                    return this.deleteCartItem(id, {timeout: timeout});
                },
                'number': function(id){
                    return this.deleteCartItem(id, {});
                },
                'string, number, object': function(entityType, entityId, options){
                    return this.cart(options).then(function(cart){
                        var promises = [];
                        _.each(cart.cartItems, function(cartItem){
                            if(cartItem.entityType == entityType && cartItem.entityId == entityId){
                                promises.push(that.deleteCartItem(cartItem.id, options));
                            }
                        });
                        return $q.all(promises).then(function(){
                            return cart;
                        });
                    });
                },
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.deleteCartItem(entityType, entityId, {timeout: timeout});
                },
                'string, number': function(entityType, entityId){
                    return this.deleteCartItem(entityType, entityId, {});
                }
            });

            this.deleteAllCartItems = helpers.overload({
                'object': function(options){
                    return this.cart(options).then(function(cart){
                        var promises = [];

                        _.each(cart.cartItems, function(cartItem){
                            promises.push(cartItem.delete(options));
                        });

                        return $q.all(promises).then(function(){
                            return that.cart(options);
                        });
                    });
                },
                'promise': function(timeout){
                    return this.deleteAllCartItems({timeout: timeout})
                },
                '': function(){
                    return this.deleteAllCartItems({});
                }
            });

            this.submitCart = helpers.overload({
                'string, string, string, object': function(fileName, transport, email, options){
                    var transportTypeIndex = {};
                    _.each(facility.config().downloadTransportType, function(downloadTransportType){
                        transportTypeIndex[downloadTransportType.type] = downloadTransportType
                    })
                    var transportType = transportTypeIndex[transport];

                    return this.post('cart/' + facility.config().facilityName + '/submit', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        fileName: fileName,
                        transport: transport,
                        email: email,
                        zipType: transportType.zipType ? transportType.zipType : '',
                        transportUrl: transportType.url
                    }, options).then(function(cart){
                        extendCart(cart);
                        cartCache = cart;
                        $rootScope.$broadcast('download:change');
                        $rootScope.$broadcast('cart:change');
                        return cart;
                    });
                },
                'string, string, string, promise': function(fileName, transport, email, timeout){
                    return this.submitCart(fileName, transport, email, {timeout: timeout});
                },
                'string, string, string': function(fileName, transport, email){
                    return this.submitCart(fileName, transport, email, {});
                }
            });

            function extendCart(cart){

                cart.isCartItem = function(entityType, entityId){
                    var out = false;
                    _.each(cart.cartItems, function(cartItem){
                        if(cartItem.entityType == entityType && cartItem.entityId == entityId){
                            out = true;
                            return false;
                        }
                    });
                    return out;
                };

                _.each(cart.cartItems, function(cartItem){
                    cartItem.facilityName = facility.config().facilityName;

                    cartItem.delete = helpers.overload({
                        'object': function(options){
                            return that.deleteCartItem(this.id, options);
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
                            return facility.icat().entity(helpers.capitalize(this.entityType), ["where ?.id = ?", this.entityType.safe(), this.entityId], options);
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
                                return entity.getSize(options).then(function(size){
                                    that.size = size;
                                    return size;
                                });
                            });
                        },
                        'promise': function(timeout){
                            return this.getSize({timeout: timeout});
                        },
                        '': function(){
                            return this.getSize({});
                        }
                    });

                    cartItem.getStatus = helpers.overload({
                        'object': function(options){
                            var that = this;
                            return this.entity(options).then(function(entity){
                                return entity.getStatus(options).then(function(status){
                                    that.status = status;
                                    return status;
                                });
                            });
                        },
                        'promise': function(timeout){
                            return this.getStatus({timeout: timeout});
                        },
                        '': function(){
                            return this.getStatus({});
                        }
                    });

                });
            }

            helpers.generateRestMethods(this, facility.tc.topcatApiPath + 'user/');
        }

	});

})();

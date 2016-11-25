

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUser', function($q, $rootScope, helpers, tcUserCart){

    	this.create = function(facility){
    		return new User(facility);
    	};

        function User(facility){
            var that = this;

            this.facility = function(){
                return facility;
            };

            this.downloads = helpers.overload({
                'object, object': function(params, options){
                    params.queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().name) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

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
                    return this.downloads({queryOffset: helpers.buildQuery([queryOffset])}, {timeout: timeout});
                },
                'string': function(queryOffset){
                    return this.downloads({queryOffset: helpers.buildQuery([queryOffset])}, {});
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

            this.setDownloadStatus = helpers.overload({
                'string, string, object': function(id, status, options){
                    return this.put('download/' + id + '/status', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        value: status
                    }, options);
                },
                'promise, string, string': function(timeout, id, status){
                    return this.setDownloadStatus(id, status, {timeout: timeout});
                },
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
            this.cart = helpers.overload({
                'object': function(options){
                    if(cartCache){
                        var defered = $q.defer();
                        defered.resolve(cartCache);
                        return defered.promise;
                    }

                    return this.get('cart/' + facility.config().name, {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
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

            this.addCartItems = helpers.overload({
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
                                icatUrl: facility.config().icatUrl,
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
                'promise, array': function(timeout, items){
                    return this.addCartItems(items, {timeout: timeout});
                },
                'array': function(items){
                    return this.addCartItems(items, {});
                }
            });

            this.addCartItem = helpers.overload({
                'string, number, object': function(entityType, entityId, options){
                    return this.addCartItems([{entityType: entityType, entityId: entityId}], options);
                },
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.addCartItem(entityType, entityId, {timeout: timeout})
                },
                'string, number': function(entityType, entityId){
                    return this.addCartItem(entityType, entityId, {});
                }
            });

            this.deleteCartItems = helpers.overload({
                'array, object': function(items, options){
                    if(typeof items[0] == 'object'){
                        items = _.map(items, function(item){ return item.entityType + " " + item.entityId; });
                    }
                    items = items.join(',');

                    return this.delete('cart/' + facility.config().name + '/cartItems', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        items: items
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        cartCache = cart;
                        $rootScope.$broadcast('cart:change');
                        return cart;
                    });
                },
                'promise, array': function(timeout, items){
                    return this.deleteCartItems(items, {timeout: timeout});
                },
                'array': function(items){
                    return this.deleteCartItems(items, {});
                }
            });

            this.deleteCartItem = helpers.overload({
                'number, object': function(id, options){
                    return this.deleteCartItems([id], options);
                },
                'number, promise': function(id, timeout){
                    return this.deleteCartItem(id, {timeout: timeout});
                },
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
                'object': function(options){
                    return this.delete('cart/' + facility.config().name + '/cartItems', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        items: "*"
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        cartCache = cart;
                        $rootScope.$broadcast('cart:change');
                        return cart;
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
                    _.each(facility.config().downloadTransportTypes, function(downloadTransportTypes){
                        transportTypeIndex[downloadTransportTypes.type] = downloadTransportTypes
                    })
                    var transportType = transportTypeIndex[transport];

                    return this.post('cart/' + facility.config().name + '/submit', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        fileName: fileName,
                        transport: transport,
                        email: email,
                        zipType: transportType.zipType ? transportType.zipType : '',
                        transportUrl: transportType.idsUrl
                    }, options).then(function(cart){
                        cart = tcUserCart.create(cart, that);
                        cartCache = cart;
                        $rootScope.$broadcast('download:change');
                        $rootScope.$broadcast('cart:change');

                        that.downloads(["where download.id = ?", cart.downloadId]).then(function(downloads){
                            var download = downloads[0];
                            if(download.transport === 'https' && download.status == 'COMPLETE'){
                                var url = download.transportUrl + '/ids/getData?preparedId=' + download.preparedId + '&outname=' + download.fileName;
                                var iframe = $('<iframe>').attr('src', url).css({
                                    position: 'absolute',
                                    left: '-1000000px',
                                    height: '1px',
                                    width: '1px'
                                });

                                $('body').append(iframe);
                            }
                        });

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

            helpers.generateRestMethods(this, facility.tc().config().topcatUrl + "/topcat/user/");

            helpers.mixinPluginMethods('user', this);
        }

    });

})();

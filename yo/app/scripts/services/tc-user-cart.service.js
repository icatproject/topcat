

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcUserCart', function($q, helpers){

    	this.create = function(attributes, user){
    		return new Cart(attributes, user);
    	};
          
        function Cart(attributes, user){
            _.merge(this, attributes);
            var facility = user.facility();

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
                'object': function(options){
                    var investigationIds = [];
                    var datasetIds = [];
                    var datafileIds = [];

                    _.each(this.cartItems, function(cartItem){
                        if(cartItem.entityType == 'investigation') investigationIds.push(cartItem.entityId);
                        if(cartItem.entityType == 'dataset') datasetIds.push(cartItem.entityId);
                        if(cartItem.entityType == 'datafile') datafileIds.push(cartItem.entityId);
                    });

                    return user.facility().ids().getSize(investigationIds, datasetIds, datafileIds, options);
                },
                'promise': function(timeout){
                    return this.getSize({timeout: timeout});
                },
                '': function(){
                    return this.getSize({});
                }
            });

            _.each(this.cartItems, function(cartItem){
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
                        return facility.icat().entity(this.entityType, ["where ?.id = ?", this.entityType.safe(), this.entityId], options);
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

                if(cartItem.entityType == 'investigation' || cartItem.entityType == 'dataset'){
                    cartItem.getSize({
                        bypassInterceptors: true
                    });
                }

            });

            helpers.mixinPluginMethods('cart', this);
        }

    });

})();

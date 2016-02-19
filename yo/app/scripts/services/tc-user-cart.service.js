

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcUserCart', function(helpers){

    	this.create = function(attributes, user){
    		return new Cart(attributes, user);
    	};
          
        function Cart(attributes, user){
            _.merge(this, attributes);
            var facility = user.facility();

            this.isCartItem = function(entityType, entityId){
                var out = false;
                _.each(this.cartItems, function(cartItem){
                    if(cartItem.entityType == entityType && cartItem.entityId == entityId){
                        out = true;
                        return false;
                    }
                });
                return out;
            };

            _.each(this.cartItems, function(cartItem){
                cartItem.facilityName = facility.config().facilityName;

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

    });

})();

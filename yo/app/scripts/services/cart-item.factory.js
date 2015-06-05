(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('CartItem', CartItem);

    CartItem.$inject = ['$log'];

    function CartItem($log) { //jshint ignore: line

        var item = function (facilityName, entityType, id, name, size, availability) {
            size = size || null;
            availability = availability || null;

            this.setFacilityName(facilityName);
            this.setEntityType(entityType);
            this.setId(id);
            this.setName(name);
            this.setSize(size);
            this.setAvailablility(availability);
        };

        item.prototype.setFacilityName = function(facilityName){
            if (facilityName) {
                this._facilityName = facilityName;
            } else {
                throw new Error('A facility name must be provided');
            }
        };

        item.prototype.getEntityType = function(){
            return this._entityType;
        };

        item.prototype.setEntityType = function(entityType){
            if (entityType) {
                this._entityType = entityType;
            } else {
                throw new Error('An entity type must be provided');
            }
        };

        item.prototype.getFacilityName = function(){
            return this._facilityName;
        };

        item.prototype.setId = function(id){
            if (id) {
                this._id = id;
            } else {
                throw new Error('A id must be provided');
            }
        };

        item.prototype.getId = function(){
            return this._id;
        };


        item.prototype.setName = function(name){
            if (name) {
                this._name = name;
            } else {
                throw new Error('A name must be provided');
            }
        };
        item.prototype.getName = function(){
            return this._name;
        };

        item.prototype.setSize = function(size){
            this._size = size;
        };

        item.prototype.getSize = function(){
            return this._size;
        };


        item.prototype.setAvailablility = function(availability){
            this._availability = availability;
        };

        item.prototype.getAvailablility = function(){
            return this._availability;
        };

        item.prototype.toObject = function() {
            return {
                facilityName: this.getFaciliyName(),
                entityType: this.getentityType(),
                id: this.getId(),
                name: this.getName(),
                size: this.getSize(),
                availability: this.getAvailability()
            };
        };

        return item;
    }
})();
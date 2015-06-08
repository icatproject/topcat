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

        item.prototype.getFacilityName = function(){
            return this.facilityName;
        };

        item.prototype.setFacilityName = function(facilityName){
            if (facilityName) {
                this.facilityName = facilityName;
            } else {
                throw new Error('A facility key must be provided');
            }
        };

        item.prototype.getEntityType = function(){
            return this.entityType;
        };

        item.prototype.setEntityType = function(entityType){
            if (entityType) {
                this.entityType = entityType;
            } else {
                throw new Error('An entity type must be provided');
            }
        };

        item.prototype.setId = function(id){
            if (id) {
                this.id = id;
            } else {
                throw new Error('A id must be provided');
            }
        };

        item.prototype.getId = function(){
            return this.id;
        };


        item.prototype.setName = function(name){
            if (name) {
                this.name = name;
            } else {
                throw new Error('A name must be provided');
            }
        };
        item.prototype.getName = function(){
            return this.name;
        };

        item.prototype.setSize = function(size){
            this.size = size;
        };

        item.prototype.getSize = function(){
            return this.size;
        };


        item.prototype.setAvailablility = function(availability){
            this.availability = availability;
        };

        item.prototype.getAvailablility = function(){
            return this.availability;
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
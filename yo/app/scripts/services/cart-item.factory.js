(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('CartItem', CartItem);

    CartItem.$inject = [];

    function CartItem() {

        var item = function (facilityName, userName, entityType, entityId, name, parentEntities) {
            this.setFacilityName(facilityName);
            this.setUserName(userName);
            this.setEntityType(entityType);
            this.setEntityId(entityId);
            this.setName(name);
            this.setParentEntities(parentEntities);
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

        item.prototype.setUserName = function(userName){
            this.userName = userName;
        };

        item.prototype.getUserName = function(){
            return this.userName;
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

        item.prototype.setEntityId = function(entityId){
            if (entityId) {
                this.entityId = entityId;
            } else {
                throw new Error('An entityId must be provided');
            }
        };

        item.prototype.getEntityId = function(){
            return this.entityId;
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

        item.prototype.setParentEntities = function(parentEntities){
            this.parentEntities = parentEntities;
        };

        item.prototype.getParentEntities = function(){
            return this.parentEntities;
        };



        item.prototype.toObject = function() {
            return {
                facilityName: this.getFacilityName(),
                userName: this.getUserName(),
                entityType: this.getEntityType(),
                entityId: this.getEntityId(),
                name: this.getName(),
                parentEntities: this.getParentEntities()
            };
        };

        return item;
    }
})();
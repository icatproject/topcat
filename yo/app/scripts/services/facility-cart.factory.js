(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('FacilityCart', FacilityCart);

    FacilityCart.$inject = ['$filter', 'APP_CONFIG', 'Config', '$log'];

    function FacilityCart($filter, APP_CONFIG, Config, $log) { //jshint ignore: line

        var cart = function (facilityName) {
            this.facilityName = facilityName;
            this.availability = null;
            this.size = null;
            this.items = [];
            this.fileName = this.getDefaultName();
            this.transportType = null;
        };

        cart.prototype.getFacilityName = function(){
            return this.facilityName;
        };

        cart.prototype.setFacilityName = function(facilityName){
            this.facilityName = facilityName;
        };

        cart.prototype.getAvailability = function(){
            return this.availability;
        };

        cart.prototype.setAvailability = function(availability){
            this.availability = availability;
        };

        cart.prototype.getSize = function(){
            return this.size;
        };

        cart.prototype.setSize = function(size){
            this.size = size;
        };

        cart.prototype.getItems = function(){
            return this.items;
        };

        cart.prototype.setItems = function(items){
            this.items = items;
        };

        cart.prototype.getFileName = function(){
            return this.fileName;
        };

        cart.prototype.setFileName = function(fileName){
            this.fileName = fileName;
        };

        cart.prototype.getTransportType = function(){
            return this.fileName;
        };

        cart.prototype.setTransportType = function(transportType){
            this.transportType = transportType;
        };

        cart.prototype.addItem = function(item){
            return this.items.push(item);
        };

        cart.prototype.getItemCount = function(){
            return this.items.length;
        };

        cart.prototype.getDefaultName = function() {
            var date = new Date();
            var dataFormat = 'yyyy-mm-dd_hh-mm-ss';

            var dateString = $filter('date')(date, dataFormat);

            return this.facilityName + '_' + dateString;
        };

        cart.prototype.calcSize = function() {
            var size = 0;
            var isUnset = false;

            _.each(this.items, function(item) {
                if (item.getSize() === null) {
                    isUnset = true;
                    return false;
                } else {
                    size = size + item.getSize();
                }
            });

            if (isUnset === true) {
                return null;
            } else {
                return  size;
            }
        };

        cart.prototype.calcAvailability = function() {
            $log.debug('calcAvailability called');
            var isAvailable = true;
            var isUnset = false;

            _.each(this.items, function(item) {
                var status = item.getAvailability();
                $log.debug('status', status);


                if (status === null) {
                    isUnset = true;
                    return false;
                } else {
                    if (status === 'ARCHIVED') {
                        isAvailable = false;
                        return false;
                    }
                }
            });

            if (isUnset === true) {
                $log.debug('availability is null');
                return null;
            } else {
                if (isAvailable === true) {
                    return 'ONLINE';
                } else {
                    return 'ARCHIVED';
                }
            }
        };

        cart.prototype.getDownloadTransportType = function() {
            return Config.getDownloadTransportTypeByFacilityName(APP_CONFIG, this.facilityName);
        };

        cart.prototype.getDataSelection = function() {
            var params = {};

            _.each(this.getItems(), function(item) {
                if (typeof params[item.entityType + 'Ids'] === 'undefined') {
                    params[item.entityType + 'Ids'] = [];
                }

                $log.debug(item);

                params[item.entityType + 'Ids'].push(item.entityId);
            });

            _.each(params, function(param, key) {
                params[key] = param.join(',');
            });

            return params;
        };

        return cart;
    }
})();
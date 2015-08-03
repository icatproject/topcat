(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('CartRequest', CartRequest);

    CartRequest.$inject = ['$log'];

    function CartRequest($log) { //jshint ignore: line

        var cart = function (facilityName, userName, sessionId, icatUrl, fileName, status, transport, transportUrl, email) {
            this.setFacilityName(facilityName);
            this.setUserName(userName);
            this.setSessionId(sessionId);
            this.setIcatUrl(icatUrl);
            this.setFileName(fileName);
            this.setStatus(status);
            this.setTransport(transport);
            this.setTransportUrl(transportUrl);
            this.setEmail(email);
        };

        cart.prototype.getFacilityName = function(){
            return this.facilityName;
        };

        cart.prototype.setFacilityName = function(facilityName){
            if (facilityName) {
                this.facilityName = facilityName;
            } else {
                throw new Error('A facility key must be provided');
            }
        };

        cart.prototype.getUserName = function(){
            return this.userName;
        };

        cart.prototype.setUserName = function(userName){
            this.userName = userName;
        };

        cart.prototype.getSessionId = function(){
            return this.sessionId;
        };

        cart.prototype.setSessionId = function(sessionId){
            if (sessionId) {
                this.sessionId = sessionId;
            } else {
                throw new Error('A sessionId type must be provided');
            }
        };

        cart.prototype.getIcatUrl = function(){
            return this.icatUrl;
        };

        cart.prototype.setIcatUrl = function(icatUrl){
            if (icatUrl) {
                this.icatUrl = icatUrl;
            } else {
                throw new Error('An icatUrl must be provided');
            }
        };

        cart.prototype.getFileName = function(){
            return this.fileName;
        };

        cart.prototype.setFileName = function(fileName){
            if (fileName) {
                this.fileName = fileName;
            } else {
                throw new Error('A fileName must be provided');
            }
        };

        cart.prototype.getStatus = function(){
            return this.status;
        };

        cart.prototype.setStatus = function(status){
            this.status = status;
        };

        cart.prototype.getTransport = function(){
            return this.transport;
        };

        cart.prototype.setTransport = function(transport){
            this.transport = transport;
        };

        cart.prototype.getTransportUrl = function(){
            return this.transportUrl;
        };

        cart.prototype.setTransportUrl = function(transportUrl){
            this.transportUrl = transportUrl;
        };

        cart.prototype.getEmail = function(){
            return this.email;
        };

        cart.prototype.setEmail = function(email){
            this.email = email;
        };

        cart.prototype.toObject = function() {
            return {
                facilityName: this.getFacilityName(),
                userName: this.getUserName(),
                sessionId: this.getSessionId(),
                icatUrl: this.getIcatUrl(),
                fileName: this.getFileName(),
                status: this.getStatus(),
                transport: this.getTransport(),
                transportUrl: this.getTransportUrl,
                email: this.getEmail()
            };
        };

        return cart;
    }
})();
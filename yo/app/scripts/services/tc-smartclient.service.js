


(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcSmartclient', function($q, helpers){

    	this.create = function(facility){
    		return new Smartclient(facility);
    	};

    	function Smartclient(facility){

    		var idsUrl = _.select(facility.config().downloadTransportTypes, function(downloadTransportType){
    			return downloadTransportType.type == 'smartclient';
    		});

    		idsUrl = idsUrl && idsUrl[0] ? idsUrl[0].idsUrl : undefined;


    		this.ping = helpers.overload({
    			"object": function(options){
    				var defered = $q.defer();

    				this.get('ping', options).then(function(){
    					defered.resolve(true);
    				}, function(){
    					defered.resolve(false);
    				});

    				return defered.promise;
    			},
    			"promise": function(timeout){
    				return this.ping({timeout: timeout});
    			},
    			"": function(){
    				return this.ping({});
    			}
    		});

    		this.getData = helpers.overload({
    			"string, object": function(preparedId, options){
    				return this.post('getData', {
    					json: JSON.stringify({
    						idsUrl: idsUrl,
    						preparedIds: [preparedId]
    					})
    				}, options);
    			},
    			"promise, string": function(timeout, preparedId){
    				return this.getData(preparedId, {timeout: timeout});
    			},
    			"string": function(preparedId){
    				return this.getData(preparedId, {});
    			}
    		});

    		this.isReady = helpers.overload({
    			"string, object": function(preparedId, options){
    				var defered = $q.defer();

    				this.get('isReady', {
    					json: JSON.stringify({
    						idsUrl: idsUrl,
    						preparedIds: [preparedId]
    					})
    				}, options).then(function(response){
    					defered.resolve(response && response[0] && response[0].toGet == 0);
    				}, function(response){
    					defered.reject(response);
    				});

    				return defered.promise;
    			},
    			"promise, string": function(timeout, preparedId){
    				return this.isReady(preparedId, {timeout: timeout});
    			},
    			"string": function(preparedId){
    				return this.isReady(preparedId, {});
    			}
    		});

    		helpers.generateRestMethods(this, 'http://localhost:8888/');
    	}

 	});

})();



(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcSmartclient', function($q, helpers){

    	this.create = function(facility){
    		return new Smartclient(facility);
    	};

        /**
         * @interface Smartclient
         */
    	function Smartclient(facility){

    		var idsUrl = _.select(facility.config().downloadTransportTypes, function(downloadTransportType){
    			return downloadTransportType.type == 'smartclient';
    		});

    		idsUrl = idsUrl && idsUrl[0] ? idsUrl[0].idsUrl : undefined;

    		this.isEnabled = function(){
    			return idsUrl !== undefined;
    		};

    		this.ping = helpers.overload({
    			"object": function(options){
    				var defered = $q.defer();

                    if(this.isEnabled()){
        				this.get('ping', {}, options).then(function(){
        					defered.resolve(true);
        				}, function(error){
        					defered.resolve(false);
        				});
                    } else {
                        defered.resolve(false);
                    }

    				return defered.promise;
    			},
    			"promise": function(timeout){
    				return this.ping({timeout: timeout});
    			},
    			"": function(){
    				return this.ping({});
    			}
    		});

    		this.login = helpers.overload({
    			"object": function(options){
    				return this.post('login', {
    					json: JSON.stringify({
    						sessionId: facility.icat().session().sessionId,
    						idsUrl: idsUrl
    					})
    					
    				}, options);
    			},
    			"promise": function(timeout){
    				return this.login({timeout: timeout});
    			},
    			"": function(){
    				return this.login({});
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

    		this.status = helpers.overload({
    			"string, object": function(preparedId, options){
    				return this.get('status', {
    					json: JSON.stringify({
    						idsUrl: idsUrl,
    						preparedIds: [preparedId]
    					})
    				}, options);
    			},
    			"promise, string": function(timeout, preparedId){
    				return this.status(preparedId, {timeout: timeout});
    			},
    			"string": function(preparedId){
    				return this.status(preparedId, {});
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
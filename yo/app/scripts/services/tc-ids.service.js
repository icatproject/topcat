

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcIds', function(helpers, tcCache){

    	this.create = function(facility){
    		return new Ids(facility);
    	};

    	function Ids(facility){
        var that = this;
        var cache;

        this.cache = function(){
          if(!cache) cache = tcCache.create('ids:' + facility.config().facilityName);
          return cache;
        };

    		this.version = function(){
    			var out = $q.defer();
    			this.get('getApiVersion').then(function(version){
    				out.resolve(version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

    		this.getSize = helpers.overload({
    			'string, number, object': function(type, id, options){
            var key = 'getSize:' + type + ":" + id;
            return this.cache().getPromise(key, function(){
              var idsParamName = helpers.uncapitalize(type) + "Ids";
              var params = {
                server: facility.config().icatUrl,
                sessionId: facility.icat().session().sessionId
              };
              params[idsParamName] = id;
              return that.get('getSize', params,  options).then(function(size){
                return parseInt('' + size);
              });
            });
    			},
    			'string, number, promise': function(type, id, timeout){
    				return this.getSize(type, id, {timeout: timeout});
    			},
    			'string, number': function(type, id){
    				return this.getSize(type, id, {});
    			},
    			'array, array, array, object': function(investigationIds, datasetIds, datafileIds, options){
            investigationIds = investigationIds.join(',');
            datasetIds = datasetIds.join(',');
            datafileIds = datafileIds.join(',');
            var key = 'getSize:investigationIds:' + investigationIds + 'datasetIds:' + datasetIds + 'datafileIds:' + datafileIds;
            return this.cache().getPromise(key, function(){
      				var params = {
      					server: facility.config().icatUrl,
      					sessionId: facility.icat().session().sessionId
      				};

      				if(investigationIds != '') params.investigationIds = investigationIds;
      				if(datasetIds != '') params.datasetIds = datasetIds;
      				if(datafileIds != '') params.datafileIds = datafileIds;

      				return that.get('getSize', params,  options).then(function(size){
      					return parseInt('' + size);
      				});
            });
    			},
    			'promise, array, array, array': function(timeout, investigationIds, datasetIds, datafileIds){
    				return this.getSize(investigationIds, datasetIds, datafileIds, {timeout: timeout});
    			},
    			'array, array, array': function(investigationIds, datasetIds, datafileIds){
    				return this.getSize(investigationIds, datasetIds, datafileIds, {});
    			}
    		});

    		this.getStatus = helpers.overload({
    			'string, number, object': function(type, id, options){
    				var idsParamName = helpers.uncapitalize(type) + "Ids";
    				var params = {
    					server: facility.config().icatUrl,
    					sessionId: facility.icat().session().sessionId
    				};
    				params[idsParamName] = id;

            var key = 'getStatus:' + type + ":" + id;

            return this.cache().getPromise(key, 10 * 60 * 60, function(){
              return that.get('getStatus', params,  options).then(function(status){
                return status;
              });
            });
    			},
    			'string, number, promise': function(type, id, timeout){
    				return this.getStatus(type, id, {timeout: timeout});
    			},
    			'string, number': function(type, id){
    				return this.getStatus(type, id, {});
    			}
    		});


    		helpers.generateRestMethods(this, facility.config().idsUrl + '/ids/');
    	}
		

	});

})();

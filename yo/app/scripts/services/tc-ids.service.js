

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIds', function($q, helpers, tcCache){

    	this.create = function(facility){
    		return new Ids(facility);
    	};

    	function Ids(facility){
        var that = this;
        var cache;

        this.facility = function(){
          return facility;
        };

        this.cache = function(){
          if(!cache) cache = tcCache.create('ids:' + facility.config().name);
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
              options.lowPriority = true;
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
            investigationIds = _.map(investigationIds, function(v){return v});
            datasetIds = _.map(datasetIds, function(v){return v});
            datafileIds = _.map(datafileIds, function(v){return v});
            var key = 'getSize:investigationIds:' + investigationIds.join(',') + 'datasetIds:' + datasetIds.join(',') + 'datafileIds:' + datafileIds.join(',');
            return this.cache().getPromise(key, function(){
      				var defered = $q.defer();
              var out = 0;
              var promises = [];
              while(investigationIds.length > 0 || datasetIds.length > 0 || datafileIds.length > 0){
                
                var params = {
                  server: facility.config().icatUrl,
                  sessionId: facility.icat().session().sessionId
                };

                var currentInvestigationIds = [];
                var currentDatasetIds = [];
                var currentDatafileIds = [];

                while(investigationIds.length > 0){
                  if(currentInvestigationIds.join(',').length > 900) break;
                  currentInvestigationIds.push(investigationIds.pop());
                }
                currentInvestigationIds = currentInvestigationIds.join(',');

                while(datasetIds.length > 0){
                  if((currentInvestigationIds + currentDatasetIds.join(',')).length > 900) break;
                  currentDatasetIds.push(datasetIds.pop());
                }
                currentDatasetIds = currentDatasetIds.join(',');

                while(datafileIds.length > 0){
                  if((currentInvestigationIds + currentDatasetIds + currentDatafileIds.join(',')).length > 900) break;
                  currentDatafileIds.push(datafileIds.pop());
                }
                currentDatafileIds = currentDatafileIds.join(',');

                if(currentInvestigationIds != '') params.investigationIds = currentInvestigationIds;
                if(currentDatasetIds != '') params.datasetIds = currentDatasetIds;
                if(currentDatafileIds != '') params.datafileIds = currentDatafileIds;

                options.lowPriority = true;
                promises.push(that.get('getSize', params,  options).then(function(size){
                  out = out + parseInt(size);
                }));
              }
              $q.all(promises).then(function(){
                defered.resolve(out);
              });
              return defered.promise;
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
            var key = 'getStatus:' + type + ":" + id;
            return this.cache().getPromise(key, function(){
              var idsParamName = helpers.uncapitalize(type) + "Ids";
              var params = {
                server: facility.config().icatUrl,
                sessionId: facility.icat().session().sessionId
              };
              params[idsParamName] = id;
              options.lowPriority = true;
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
          },
          'array, array, array, object': function(investigationIds, datasetIds, datafileIds, options){
            investigationIds = _.map(investigationIds, function(v){return v});
            datasetIds = _.map(datasetIds, function(v){return v});
            datafileIds = _.map(datafileIds, function(v){return v});
            var key = 'getStatus:investigationIds:' + investigationIds.join(',') + 'datasetIds:' + datasetIds.join(',') + 'datafileIds:' + datafileIds.join(',');
            return this.cache().getPromise(key, function(){
              var defered = $q.defer();
              var out = [];
              var promises = [];
              while(investigationIds.length > 0 || datasetIds.length > 0 || datafileIds.length > 0){
                
                var params = {
                  server: facility.config().icatUrl,
                  sessionId: facility.icat().session().sessionId
                };

                var currentInvestigationIds = [];
                var currentDatasetIds = [];
                var currentDatafileIds = [];

                while(investigationIds.length > 0){
                  if(currentInvestigationIds.join(',').length > 900) break;
                  currentInvestigationIds.push(investigationIds.pop());
                }
                currentInvestigationIds = currentInvestigationIds.join(',');

                while(datasetIds.length > 0){
                  if((currentInvestigationIds + currentDatasetIds.join(',')).length > 900) break;
                  currentDatasetIds.push(datasetIds.pop());
                }
                currentDatasetIds = currentDatasetIds.join(',');

                while(datafileIds.length > 0){
                  if((currentInvestigationIds + currentDatasetIds + currentDatafileIds.join(',')).length > 900) break;
                  currentDatafileIds.push(datafileIds.pop());
                }
                currentDatafileIds = currentDatafileIds.join(',');

                if(currentInvestigationIds != '') params.investigationIds = currentInvestigationIds;
                if(currentDatasetIds != '') params.datasetIds = currentDatasetIds;
                if(currentDatafileIds != '') params.datafileIds = currentDatafileIds;

                options.lowPriority = true;
                promises.push(that.get('getStatus', params,  options).then(function(status){
                  out.push(status);
                }));
              }
              $q.all(promises).then(function(){
                if(_.select(out, function(s){ return s == 'ARCHIVED'; }).length > 0){
                  defered.resolve('ARCHIVED');
                } else if(_.select(out, function(s){ return s == 'RESTORING'; }).length > 0){
                  defered.resolve('RESTORING');
                } else {
                  defered.resolve('ONLINE');
                }
              });
              return defered.promise;
            });
          },
          'promise, array, array, array': function(timeout, investigationIds, datasetIds, datafileIds){
            return this.getStatus(investigationIds, datasetIds, datafileIds, {timeout: timeout});
          },
          'array, array, array': function(investigationIds, datasetIds, datafileIds){
            return this.getStatus(investigationIds, datasetIds, datafileIds, {});
          }
        });


    		helpers.generateRestMethods(this, facility.config().idsUrl + '/ids/');
    	}
		

	});

})();

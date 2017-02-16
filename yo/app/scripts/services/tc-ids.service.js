

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIds', function($q, helpers, tcCache){

    	this.create = function(facility, url){
    		return new Ids(facility, url);
    	};

    	function Ids(facility, url){
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
                icatUrl: facility.config().icatUrl,
                sessionId: facility.icat().session().sessionId
              };
              params[idsParamName] = id;
              options.lowPriority = true;
              return facility.tc().get('user/getSize', params, options).then(function(size){
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
            var key = 'getSize:investigationIds:' + investigationIds.join(',') + 'datasetIds:' + datasetIds.join(',') + 'datafileIds:' + datafileIds.join(',');
            return this.cache().getPromise(key, function(){
              var out = 0;
              var currentInvestigationIds = [];
              var currentDatasetIds = [];
              var currentDatafileIds  = [];
              var promises = [];

              options.lowPriority = true;

              while(investigationIds.length > 0 || datasetIds.length > 0 || datafileIds.length > 0){
                while(investigationIds.length > 0 && urlLengthIsOk(currentInvestigationIds.concat([investigationIds[0]]), currentDatasetIds, currentDatafileIds)){
                  currentInvestigationIds.push(investigationIds.shift());
                }

                while(datasetIds.length > 0 && urlLengthIsOk(currentInvestigationIds, currentDatasetIds.concat([datasetIds[0]]), currentDatafileIds)){
                  currentDatasetIds.push(datasetIds.shift());
                }

                while(datafileIds.length > 0 && urlLengthIsOk(currentInvestigationIds, currentDatasetIds, currentDatafileIds.concat([datafileIds[0]]))){
                  currentDatafileIds.push(datafileIds.shift());
                }

                var params = generateParams(currentInvestigationIds, currentDatasetIds, currentDatafileIds)

                promises.push(facility.tc().get('user/getSize', params, options).then(function(size){
                  out = out + parseInt(size);
                }));

                currentInvestigationIds = [];
                currentDatasetIds = [];
                currentDatafileIds  = [];
              }

              function urlLengthIsOk(investigationIds, datasetIds, datafileIds){
                return facility.tc().getUrlLength('getSize', generateParams(investigationIds, datasetIds, datafileIds)) <= 1024;
              }

              function generateParams(investigationIds, datasetIds, datafileIds){
                var out = {
                  icatUrl: facility.config().icatUrl,
                  sessionId: facility.icat().session().sessionId
                };

                if(investigationIds.length > 0) out.investigationIds = investigationIds.join(',');
                if(datasetIds.length > 0) out.datasetIds = datasetIds.join(',');
                if(datafileIds.length > 0) out.datafileIds = datafileIds.join(',');

                return out
              }

              return $q.all(promises).then(function(){
                return out;
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

        this.isTwoLevel = helpers.overload({
          'object': function(options){
            return this.get('isTwoLevel', {}, options).then(function(isTwoLevel){
              return isTwoLevel == 'true';
            });
          },
          'promise': function(timeout){
            return this.isTwoLevel({timeout: timeout});
          },
          '': function(){
            return this.isTwoLevel({});
          }
        });

        this.upload = helpers.overload({
          'number, array, object': function(datasetId, files, options){
            var promises = [];

            options.queryParams = {
              sessionId: facility.icat().session().sessionId,
              datasetId: datasetId,
              datafileFormatId: facility.config().idsUploadDatafileFormatId,
            }

            options.headers =  {
              'Content-Type': 'application/octet-stream'
            };

            options.transformRequest = [];

            _.each(files, function(file){
              options.queryParams.name = file.name
              promises.push(that.put('put', file.data, options));
            });
            
            return $q.all(promises);
          }
          ,
          'promise, number, array': function(timeout, datasetId, files){
            return this.upload(datasetId, files, {timeout: timeout});
          },
          'number, array': function(datasetId, files){
            return this.upload(datasetId, files, {});
          }
        });


    		helpers.generateRestMethods(this, url + '/ids/');

        helpers.mixinPluginMethods('ids', this);
    	}
		

	});

})();

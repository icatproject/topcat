

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIds', function($q, helpers, tcCache){

    	this.create = function(facility, url){
    		return new Ids(facility, url);
    	};

      /**
       * @interface IDS
       */
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

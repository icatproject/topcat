

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


        /**
         * Returns the version of the IDS server
         *
         * @method
         * @name IDS#version
         * @return {Promise<string>}
         */
    		this.version = function(){
    			var out = $q.defer();
    			this.get('getApiVersion').then(function(version){
    				out.resolve(version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

        this.isTwoLevel = helpers.overload({
          /**
           * Returns whether or not the IDS is two level.
           *
           * @method
           * @name IDS#isTwoLevel
           * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
           * @return {Promise<boolean>}
           */
          'object': function(options){
            return this.get('isTwoLevel', {}, options).then(function(isTwoLevel){
              return isTwoLevel == 'true';
            });
          },

          /**
           * Returns whether or not the IDS is two level.
           *
           * @method
           * @name IDS#isTwoLevel
           * @param {Promise} timeout if resolved will cancel the request
           * @return {Promise<boolean>}
           */
          'promise': function(timeout){
            return this.isTwoLevel({timeout: timeout});
          },

          /**
           * Returns whether or not the IDS is two level.
           *
           * @method
           * @name IDS#isTwoLevel
           * @return {Promise<boolean>}
           */
          '': function(){
            return this.isTwoLevel({});
          }
        });

        this.upload = helpers.overload({
          /**
           * Uploads files to a dataset.
           *
           * @method
           * @name IDS#upload
           * @param {number} datasetId the id of the dataset you wish to upload to 
           * @param {File[]} files the files to be uploaded
           * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
           * @return {Promise}
           */
          'number, array, object': function(datasetId, files, options){
            var defered = $q.defer();

            files = _.clone(files);

            var queryParams = {
              sessionId: facility.icat().session().sessionId,
              datasetId: datasetId,
              datafileFormatId: facility.config().idsUploadDatafileFormatId,
            };


            var datafileIds = [];

            function upload(){
              if(files.length > 0){
                var file = files.shift();

                queryParams.name = file.name;

                var flow = new Flow({
                  uploadMethod: 'PUT',
                  method: 'octet',
                  headers: {'Content-Type': 'application/octet-stream'},
                  target: url + '/ids/put?' + helpers.urlEncode(queryParams),
                  testChunks: false
                });

                flow.addFile(file);

                flow.on('complete', function(){
                  upload();
                });

                flow.on('error', function(message){
                  defered.reject(JSON.parse(message));
                });

                flow.upload();

              } else {
                defered.resolve(datafileIds);
              }
            }
            upload();
            
            return defered.promise;
          }

          /**
           * Uploads files to a dataset.
           *
           * @method
           * @name IDS#upload
           * @param {number} datasetId the id of the dataset you wish to upload to 
           * @param {File[]} files the files to be uploaded
           * @param {Promise} timeout if resolved will cancel the request
           * @return {Promise}
           */
          ,
          'promise, number, array': function(timeout, datasetId, files){
            return this.upload(datasetId, files, {timeout: timeout});
          },

          /**
           * Uploads files to a dataset.
           *
           * @method
           * @name IDS#upload
           * @param {number} datasetId the id of the dataset you wish to upload to 
           * @param {File[]} files the files to be uploaded
           * @return {Promise}
           */
          'number, array': function(datasetId, files){
            return this.upload(datasetId, files, {});
          }
        });


    		helpers.generateRestMethods(this, url + '/ids/');

        helpers.mixinPluginMethods('ids', this);
    	}
		

	});

})();

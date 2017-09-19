

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIds', function($q, $interval, helpers, tcCache){

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


        var chunkSize = 100000;
        if(FileReader && !(new FileReader()).readAsBinaryString) chunkSize = 1000;

        /**
         * Uploads files to a dataset.
         *
         * @method
         * @name IDS#upload
         * @param {number} datasetId the id of the dataset you wish to upload to 
         * @param {File[]} files the files to be uploaded
         * @return {Promise}
         */
        this.upload = function(datasetId, files){
          var defered = $q.defer();

          files = _.clone(files);

          var queryParams = {
            idsUrl: url,
            sessionId: facility.icat().session().sessionId,
            datasetId: datasetId,
            datafileFormatId: facility.config().idsUploadDatafileFormatId,
          };


          var datafileIds = [];

          function upload(){
            if(files.length > 0){
              var file = files.shift();
              var dataUploaded = 0;

              _.each(files, function(file){
                file.percentageUploaded = 0;
              });

              queryParams.name = file.name;
              queryParams.contentLength = file.size;

              var topcatUrl = facility.tc().config().topcatUrl;
              if(topcatUrl.match(/^https:\/\//)){
                topcatUrl = "wss://" + topcatUrl.replace(/^https:\/\//, '');
              } else {
                topcatUrl = "ws://" + topcatUrl.replace(/^http:\/\//, '');
              }

              var currentUrl = topcatUrl + "/topcat/user/upload?" + _.map(queryParams, function(v, k){ return encodeURIComponent(k) + "=" + encodeURIComponent(v) }).join('&');

              var connection = new WebSocket(currentUrl);

              var chunkIndex = 0;

              var readChunk = function(){
                if(chunkIndex * chunkSize > file.size){
                  return;
                }

                var from = chunkIndex * chunkSize;
                var to = (chunkIndex + 1) * chunkSize;
                if(to > file.size) to = file.size;
                var chunk = file.slice(from, to);

                var reader = new FileReader();
    
                reader.onload = function(e) {
                  var binary;
                  if(reader.readAsBinaryString){
                    binary = reader.result;
                  } else {
                    binary = "";
                    var bytes = new Uint8Array(reader.result);
                    var length = bytes.byteLength;
                    for (var i = 0; i < length; i++) {
                      binary += String.fromCharCode(bytes[i]);
                    }
                  }
                  connection.send(binary);
                  dataUploaded += binary.length;
                  file.percentageUploaded = _.round(dataUploaded / (file.size / 100), 2);
                  chunkIndex++;
                  readChunk();
                };
                
                if(reader.readAsBinaryString){
                  reader.readAsBinaryString(chunk);
                } else {
                  reader.readAsArrayBuffer(chunk);
                }
  
              };

              connection.onmessage = function(response){
                datafileIds.push(JSON.parse(response.data).id);
                connection.close();
                upload();
              };

              connection.onopen = function(){
                readChunk();
              };

            } else {
              defered.resolve(datafileIds);
            }
          }
          upload();
          
          return defered.promise;
        };


    		helpers.generateRestMethods(this, url + '/ids/');

        helpers.mixinPluginMethods('ids', this);
    	}
		

	});

})();

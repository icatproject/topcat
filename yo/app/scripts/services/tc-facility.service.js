

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcFacility', function($sessionStorage, helpers, tcIcat, tcIds, tcUser, tcAdmin, tcSmartclient, APP_CONFIG){

    	this.create = function(tc, name, APP_CONFIG){
    		return new Facility(tc, name);
    	};

        /**
         * @interface Facility
         */
        function Facility(tc, name){
            var icat;
            var ids;
            var admin;
            var user;
            var smartclient;
            
            /**
             * Returns the facility specific configuration from topcat.json
             *
             * @method
             * @name  Facility#config
             * @return {object}
             */
            this.config = function(){
                var out = _.select(APP_CONFIG.facilities, function(facility){ return name == facility.name; })[0];
                var sessions = $sessionStorage.sessions || {};
                var session = sessions[name] || {};
                var id = session.facilityId;
                var idsUploadDatafileFormatId = session.idsUploadDatafileFormatId;
                var idsUploadDatasetTypeId = session.idsUploadDatasetTypeId;
                if(id) out.id = id;
                if(idsUploadDatafileFormatId) out.idsUploadDatafileFormatId = idsUploadDatafileFormatId;
                if(idsUploadDatasetTypeId) out.idsUploadDatasetTypeId = idsUploadDatasetTypeId
                return out; 
            }

            this.tc = function(){
                return tc;
            };

           /**
            * Returns an object that represents the facility's Icat.
            * 
            * @method
            * @name  Facility#icat
            * @return {Icat}
            */
            this.icat = function(){
                if(!icat) icat = tcIcat.create(this);
                return icat;
            };

            /**
            * Returns an object that represents the facility's primary Ids.
            * 
            * @method
            * @name  Facility#ids
            * @return {IDS}
            */
            this.ids = function(){
                if(!ids) ids = tcIds.create(this, this.config().idsUrl);
                return ids;
            };

            /**
            * Returns an object that represents the facility's Ids for a particular transport type.
            * 
            * @method
            * @name  Facility#downloadTransportTypeIds
            * @param name {string} the name of the transport type e.g. 'https', 'globus' or 'smartclient'
            * @return {IDS}
            */
            var downloadTransportTypeIdsIndex;
            this.downloadTransportTypeIds = function(type){
                if(!downloadTransportTypeIdsIndex){
                    downloadTransportTypeIdsIndex = {};
                    _.each(this.config().downloadTransportTypes, function(transportType){
                        downloadTransportTypeIdsIndex[transportType.type] = tcIds.create(this, transportType.idsUrl);
                    })
                }
                return downloadTransportTypeIdsIndex[type];
            };

            /**
             * Returns an object that represents a user with admin access.
             *
             * @method
             * @name Facility#admin
             * @return {Admin} 
             */
            this.admin = function(){
                if(!admin) admin = tcAdmin.create(this);
                return admin;
            }

            /**
             * Returns an object that represents a general user.
             *
             * @method
             * @name Facility#user
             * @return {User} 
             */
            this.user = function(){
                if(!user) user = tcUser.create(this);
                return user;
            }

             /**
             * Returns an object that represents a smartclient.
             *
             * @method
             * @name Facility#smartclient
             * @return {Smartclient} 
             */
            this.smartclient = function(){
              if(!smartclient) smartclient = tcSmartclient.create(this);
              return smartclient;
            };

            helpers.mixinPluginMethods('facility', this);

        }

	});

})();

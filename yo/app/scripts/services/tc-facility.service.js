

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcFacility', function($sessionStorage, helpers, tcIcat, tcIds, tcUser, tcAdmin, tcSmartclient, APP_CONFIG){

    	this.create = function(tc, name, APP_CONFIG){
    		return new Facility(tc, name);
    	};

        function Facility(tc, name){
            var icat;
            var ids;
            var admin;
            var user;
            var smartclient;
            
            this.config = function(){
                var out = _.select(APP_CONFIG.facilities, function(facility){ return name == facility.name; })[0];
                var sessions = $sessionStorage.sessions || {};
                var id = (sessions[name] || {}).facilityId;
                if(id) out.id = id;
                return out; 
            }

            this.tc = function(){
                return tc;
            };

            this.icat = function(){
                if(!icat) icat = tcIcat.create(this);
                return icat;
            };

            this.ids = function(){
                if(!ids) ids = tcIds.create(this);
                return ids;
            };

            this.admin = function(){
                if(!admin) admin = tcAdmin.create(this);
                return admin;
            }

            this.user = function(){
                if(!user) user = tcUser.create(this);
                return user;
            }

            this.smartclient = function(){
              if(!smartclient) smartclient = tcSmartclient.create(this);
              return smartclient;
            };

        }

	});

})();

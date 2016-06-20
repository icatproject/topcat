

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcIcat', function($sessionStorage, $rootScope, $q, helpers, tcIcatEntity, tcIcatQueryBuilder, tcCache){

    	this.create = function(facility){
    		return new Icat(facility);
    	};

    	function Icat(facility){
            var that = this;
            var cache;

            this.cache = function(){
              if(!cache) cache = tcCache.create('icat:' + facility.config().name);
              return cache;
            };

    		this.facility = function(){
    			return facility;
    		};

    		this.version = function(){
    			var out = $q.defer();
    			this.get('version').then(function(data){
    				out.resolve(data.version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

    		this.session = function(){
    			var facilityName = facility.config().name;
    			if($sessionStorage.sessions && $sessionStorage.sessions[facilityName]){
    				return $sessionStorage.sessions[facilityName];
    			}
    			return {};
    		};

    		this.refreshSession = function(){
    			return this.put('session/' + this.session().sessionId);
    		};	

    		this.login = function(plugin, username, password){
                if(username === undefined) username = "anon";

    			var params = {
    				json: JSON.stringify({
                        plugin: plugin,
                        credentials: [
                            {username: username},
                            {password: password}
                        ]
                    })
    			};
    			return this.post('session', params).then(function(response){
    				if(!$sessionStorage.sessions) $sessionStorage.sessions = {};
    				var facilityName = facility.config().name;

    				$sessionStorage.sessions[facilityName] = {
    					sessionId: response.sessionId,
    					userName: plugin + '/' + username
    				}

                    var promises = [];

                    var name = facility.config().name;
                    if(name){
                      promises.push(that.query([
                        "SELECT facility FROM Facility facility WHERE facility.name = ?", name
                      ]).then(function(results){
                        var facility = results[0];
                        if(facility){
                          $sessionStorage.sessions[facilityName].facilityId = facility.id;
                        } else {
                          throw "Could not find facility by name '" + name + "'";
                        }
                      }));
                    }

    				promises.push(facility.admin().isValidSession(response.sessionId).then(function(isAdmin){
                        $sessionStorage.sessions[facilityName].isAdmin = isAdmin;
                    }));

                    promises.push(that.entity('user', ["where user.name = ?", username]).then(function(user){
                        $sessionStorage.sessions[facilityName].fullName = user.fullName;
                    }));

                    return $q.all(promises).then(function(){
                      $rootScope.$broadcast('session:change');
                    });

    			});
    		};

            this.logout = helpers.overload({
            	'boolean': function(isSoft){
            		var promises = [];
            		if(!isSoft && this.session().sessionId){
            			promises.push(this.delete('session/' + this.session().sessionId, {
    		            	server: facility.config().icatUrl
    		            }));
            		}

            		delete $sessionStorage.sessions[facility.config().name];
    				    $sessionStorage.$apply();

            		return $q.all(promises).then(function(){
            			$rootScope.$broadcast('session:change');
            		}, function(){
            			$rootScope.$broadcast('session:change');
            		});
            	},
            	'': function(){
            		return this.logout(false);
            	}
            });


            this.query = helpers.overload({
            	'array, object': function(query, options){    	
    	        	var defered = $q.defer();
                    var query = helpers.buildQuery(query);
                    var key = "query:" + query;

    	        	this.cache().getPromise(key, 10 * 60 * 60, function(){
                        return that.get('entityManager', {
                            sessionId: that.session().sessionId,
                            query: query,
                            server: facility.config().icatUrl
                        }, options);
                    }).then(function(results){
                    	defered.resolve(_.map(results, function(result){
                    		var type = _.keys(result)[0];
                    		if(helpers.typeOf(result) != 'object' || !type) return result;
                				var out = result[type];
                				out.entityType = helpers.uncapitalize(type);
                				out = tcIcatEntity.create(out, facility);
                				return out;
                			}));
                    }, function(response){
                    	defered.reject(response);
                    });

                    return defered.promise;
            	},
            	'promise, array': function(timeout, query){
    	        	return this.query(query, {timeout: timeout});
    	        },
    	        'promise, string': function(timeout, query){
    	        	return this.query([query], {timeout: timeout});
    	        },
            	'array': function(query){
    	        	return this.query(query, {});
    	        },
    	        'string': function(query){
    	        	return this.query([query], {});
    	        }
            });


            this.entities = helpers.overload({
            	'string, array, object': function(type, query, options){
            		return this.query([[
            			'select ' + type + ' from ' + helpers.capitalize(type) + ' ' + type
            		], query], options);
            	},
            	'string, promise, array': function(type, timeout, query){
            		return this.entities(type, query, {timeout: timeout});
            	},
            	'string, promise, string': function(type, timeout, query){
            		return this.entities(type, [query], {timeout: timeout});
            	},
            	'string, array': function(type, query){
            		return this.entities(type, query, {});
            	},
            	'string, string': function(type, query){
            		return this.entities(type, [query], {});
            	},
            	'string, promise': function(type, timeout){
            		return this.entities(type, [], {timeout: timeout});
            	},
            	'string': function(type){
            		return this.entities(type, [], {});
            	}
            });

            this.entity = function(){
          		var out = $q.defer();
          		this.entities.apply(this, arguments).then(function(results){
          			out.resolve(results[0]);
          		}, function(results){
          			out.reject(results);
          		});
          		return out.promise;
          	};

          	this.queryBuilder = function(entityType){
        		return tcIcatQueryBuilder.create(this, entityType);
        	};

          	helpers.generateRestMethods(this, facility.config().icatUrl + '/icat/');
        }


	});

})();

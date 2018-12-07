

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIcat', function($sessionStorage, $rootScope, $q, helpers, tcIcatEntity, tcIcatQueryBuilder, tcCache){

    	this.create = function(facility){
    		return new Icat(facility);
    	};

        /**
         * @interface Icat
         **/
    	function Icat(facility){
            var that = this;
            var cache;
            var tc = facility.tc();

            this.cache = function(){
              if(!cache) cache = tcCache.create('icat:' + facility.config().name);
              return cache;
            };

            /**
             * Returns the Facility that this particular Icat belongs to.
             * 
             * @method
             * @name  Icat#facility
             * @return {Facility}
             */
    		this.facility = function(){
    			return facility;
    		};

            /**
             * Returns the current version of this Icat.
             *
             * @method
             * @name Icat#version
             * @return {Promise<string>}
             */
    		this.version = function(){
    			var out = $q.defer();
    			this.get('version').then(function(data){
    				out.resolve(data.version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

            /**
             * Returns and object containing the data stored within this session.
             *
             * @method
             * @name Icat#session
             * @return {object}
             */
    		this.session = function(){
    			var facilityName = facility.config().name;
    			if($sessionStorage.sessions && $sessionStorage.sessions[facilityName]){
    				return $sessionStorage.sessions[facilityName];
    			}
    			return {};
    		};


            /**
             * Refreshes the session's time to live.
             *
             * @method
             * @name Icat#refreshSession
             * @return {Promise}
             */
    		this.refreshSession = function(){
    			return this.put('session/' + this.session().sessionId);
    		};

            this.login = helpers.overload({
                /**
                 * Logs a user into this Icat with a username and password.
                 *
                 * @method
                 * @name Icat#login
                 * @param {string} plugin The type of authentication mechanism
                 * @param {object} credentials
                 * @return {Promise}
                 */
                'string, object': function(plugin, credentials){
                    var params = {
                        plugin: plugin,
                        credentials: []
                    };

                    _.each(credentials, function(value, name){
                        var credential = {};
                        credential[name] = value;
                        params.credentials.push(credential);
                    });

                    params = {json: JSON.stringify(params)};

                    $rootScope.$broadcast('session:changing');

                    return this.post('session', params).then(function(response){
                        if(!$sessionStorage.sessions) $sessionStorage.sessions = {};
                        var facilityName = facility.config().name;
                        var sessionId = response.sessionId;

                        $sessionStorage.sessions[facilityName] = { sessionId: sessionId }

                        return that.get('session/' + response.sessionId).then(function(response){
                            var username = response.userName;

                            $sessionStorage.sessions[facilityName].username = username;
                            $sessionStorage.sessions[facilityName].plugin = plugin;

                            var promises = [];

                            var name = facility.config().name;
                            promises.push(that.query([
                                "SELECT facility FROM Facility facility WHERE facility.name = ?", name
                            ]).then(function(results){
                                var facility = results[0];
                                if(facility){
                                    $sessionStorage.sessions[facilityName].facilityId = facility.id;
                                } else {
                                    console.error("Could not find facility by name '" + name + "'");
                                }
                            }));

                            var idsUploadDatasetType = facility.config().idsUploadDatasetType;
                            if(idsUploadDatasetType){
                                promises.push(that.query([
                                    "SELECT datasetType FROM DatasetType datasetType, datasetType.facility as facility", 
                                    "WHERE facility.name = ?", name,
                                    "AND datasetType.name = ?", idsUploadDatasetType
                                ]).then(function(results){
                                    var datasetType = results[0];
                                    if(datasetType){
                                        $sessionStorage.sessions[facilityName].idsUploadDatasetTypeId = datasetType.id;
                                    } else {
                                        console.error("Could not find IDS upload dataset type with name '" + idsUploadDatasetType + "'");
                                    }
                                }));
                            }

                            var idsUploadDatafileFormat = facility.config().idsUploadDatafileFormat;
                            if(idsUploadDatafileFormat){
                                promises.push(that.query([
                                    "SELECT datasetType FROM DatafileFormat datasetType, datasetType.facility as facility", 
                                    "WHERE facility.name = ?", name,
                                    "AND datasetType.name = ?", idsUploadDatafileFormat
                                ]).then(function(results){
                                    var datasetType = results[0];
                                    if(datasetType){
                                        $sessionStorage.sessions[facilityName].idsUploadDatafileFormatId = datasetType.id;
                                    } else {
                                        console.error("Could not find IDS upload datafile format with name '" + idsUploadDatafileFormat + "'");
                                    }
                                }));
                            }

                            promises.push(facility.admin().isValidSession(sessionId).then(function(isAdmin){
                                $sessionStorage.sessions[facilityName].isAdmin = isAdmin;
                                if(isAdmin) document.cookie = "isAdmin=true";
                            }));

                            promises.push(that.query(["select user from User user where user.name = ?", username]).then(function(users){
                                if(users[0]){
                                    $sessionStorage.sessions[facilityName].fullName = users[0].fullName;
                                } else {
                                    $sessionStorage.sessions[facilityName].fullName = username;
                                }
                            }));

                            var completedDownloads = {};
                            _.each(tc.userFacilities(), function(facility){
                                promises.push(facility.user().downloads(["where download.isDeleted = false"]).then(function(downloads){
                                    _.each(downloads, function(download){
                                        var key = facility.config().name + ":" + download.id;
                                        completedDownloads[key] = true;
                                    });
                                }));
                            });

                            return $q.all(promises).then(function(){
                                $rootScope.$broadcast('session:change');
                                $rootScope.$broadcast('session:changed', completedDownloads);
                                $rootScope.$broadcast('session:add', that);
                            });
                        });

                    }, function(response){
                        // Initial POST /session failed; if response is empty, ICAT may be down
                        var reason = response?response.message:'ICAT may be down';
                        console.error('Failed to log into ICAT for ' + facility.config().name + ': ' + reason);
                        return $q.reject({message: 'Unable to log into ' + facility.config().name + ': ' + reason});
                    });

                },
                /**
                 * Logs a user into this Icat with a username and password.
                 *
                 * @method
                 * @name Icat#login
                 * @param {string} plugin The type of authentication mechanism
                 * @param {string} username
                 * @param {string} password
                 * @return {Promise}
                 */
                'string, string, string': function(plugin, username, password){
                    return this.login(plugin, {
                        username: username,
                        password: password
                    });
                }
            });

            this.logout = helpers.overload({
                /**
                 * Destroys a users session
                 * 
                 * @method
                 * @name  Icat#logout
                 * @param {boolean} isSoft if true the session will only be destroyed in the browser but not on the server
                 * @return Promise
                 */
            	'boolean': function(isSoft){
            		var promises = [];
            		if(!isSoft && this.session().sessionId){
            			promises.push(this.delete('session/' + this.session().sessionId, {
    		            	server: facility.config().icatUrl
    		            }));
            		}

                    $rootScope.$broadcast('session:remove', this);

            		delete $sessionStorage.sessions[facility.config().name];
    				$sessionStorage.$apply();

                    if(tc.adminFacilities().length == 0){
                        document.cookie = 'isAdmin=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                    }

                    // Clear the facility user's cartCache so it can't leak to the next user
                    facility.user().clearCartCache();

            		return $q.all(promises).then(function(){
            			$rootScope.$broadcast('session:change');
            		}, function(){
            			$rootScope.$broadcast('session:change');
            		});
            	},
                /**
                 * Destroys a users session
                 * 
                 * @method
                 * @name  Icat#logout
                 * @return Promise
                 */
            	'': function(){
            		return this.logout(false);
            	}
            });

            this.query = helpers.overload({
                /**
                 * Performs Icat JPQL queries and returns the results.
                 *
                 * @method
                 * @name Icat#query
                 * @param  {array} query the jqpl expression
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<IcatEntity[]>} a deferred array of entities
                 * @example
                 * 
                 * var investigationId = 3456;
                 * var defered = $q.defer();
                 * var page = 2;
                 * var pageSize = 10;
                 * 
                 * tc.icat('LILS').query([
                 *     "select dataset from Dataset dataset, dataset.investigation as investigation",
                 *     "where id = ?", investigationId,
                 *     "limit ?, ?", page, pageSize
                 * ], {
                 *     cache: false,
                 *     timeout: defered.promise
                 * }).then(function(datasets){
                 *      console.log(datasets);
                 * });
                 */
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
                            /**
                             *  The type of entity e.g. 'investigation', 'facility' or 'facilityCycle' etc...
                             * 
                             * @name  IcatEntity#entityType
                             * @type {string}
                             */
            				out.entityType = helpers.uncapitalize(type);
            				out = tcIcatEntity.create(out, facility);
            				return out;
                		}));
                    }, function(response){
                    	defered.reject(response);
                    });

                    return defered.promise;
            	},

                /**
                 * Performs Icat JPQL queries and returns the results.
                 *
                 * @method
                 * @name Icat#query
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {array} query the jqpl expression
                 * @return {Promise<IcatEntity[]>} a deferred array of entities
                 * @example
                 * 
                 * var investigationId = 3456;
                 * var defered = $q.defer();
                 * var page = 2;
                 * var pageSize = 10;
                 * 
                 * tc.icat('LILS').query(defered.promise, [
                 *     "select dataset from Dataset dataset, dataset.investigation as investigation",
                 *     "where id = ?", investigationId,
                 *     "limit ?, ?", page, pageSize
                 * ]).then(function(datasets){
                 *      console.log(datasets);
                 * });
                 */
            	'promise, array': function(timeout, query){
    	        	return this.query(query, {timeout: timeout});
    	        },

                /**
                 * Performs Icat JPQL queries and returns the results.
                 *
                 * @method
                 * @name Icat#query
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {string} query the jqpl expression
                 * @return {Promise<IcatEntity[]>} a deferred array of entities
                 * @example
                 * 
                 * var defered = $q.defer();
                 * var query = "select dataset from Dataset dataset, dataset.investigation as investigation where investigation.id = 2342";
                 * 
                 * tc.icat('LILS').query(defered.promise, query).then(function(datasets){
                 *      console.log(datasets);
                 * });
                 */
    	        'promise, string': function(timeout, query){
    	        	return this.query([query], {timeout: timeout});
    	        },

                /**
                 * Performs Icat JPQL queries and returns the results.
                 *
                 * @method
                 * @name Icat#query
                 * @param  {array} query the jqpl expression
                 * @return {Promise<IcatEntity[]>} a deferred array of entities
                 * @example
                 * 
                 * var investigationId = 3456;
                 * var page = 2;
                 * var pageSize = 10;
                 * 
                 * tc.icat('LILS').query([
                 *     "select dataset from Dataset dataset, dataset.investigation as investigation",
                 *     "where id = ?", investigationId,
                 *     "limit ?, ?", page, pageSize
                 * ]).then(function(datasets){
                 *      console.log(datasets);
                 * });
                 */
            	'array': function(query){
    	        	return this.query(query, {});
    	        },

                /**
                 * Performs Icat JPQL queries and returns the results.
                 *
                 * @method
                 * @name Icat#query
                 * @param  {string} query the jqpl expression
                 * @return {Promise<IcatEntity[]>} a deferred array of entities
                 * @example
                 * 
                 * var query = "select dataset from Dataset dataset, dataset.investigation as investigation where investigation.id = 2342";
                 * 
                 * tc.icat('LILS').query(query).then(function(datasets){
                 *      console.log(datasets);
                 * });
                 */
    	        'string': function(query){
    	        	return this.query([query], {});
    	        }
            });
            
            this.write = helpers.overload({
                /**
                 * Creates or updates entities for this Icat.
                 * 
                 * @method
                 * @name Icat#write
                 * @param  {object[]} entities an array of entities to be written to this Icat
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<number[]>} a deferred array of entity ids that have been created
                 */
                'array, object': function(entities, options){
                    return this.post('entityManager', {
                        sessionId: this.session().sessionId,
                        entities: JSON.stringify(entities)
                    }, options);
                },
                /**
                 * Creates or updates entities for this Icat.
                 * 
                 * @method
                 * @name Icat#write
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {object[]} entities an array of entities to be written to this Icat
                 * @return {Promise<number[]>} a deferred array of entity ids that have been created
                 */
                'promise, array': function(timeout, entities){
                    return this.write(entities, {timeout: timeout});
                },
                /**
                 * Creates or updates entities for this Icat.
                 * 
                 * @method
                 * @name Icat#write
                 * @param  {object[]} entities an array of entities to be written to this Icat
                 * @return {Promise<number[]>} a deferred array of entity ids that have been created
                 */
                'array': function(entities){
                    return this.write(entities, {});
                }
            });

            /**
             * Returns a new IcatQueryBuilder for this Icat.
             *
             * @method
             * @name Icat#queryBuilder
             * @param  {string} entityType the type of entities you wish to return e.g. 'dataset' or 'investigation' etc
             * @return {IcatQueryBuilder}
             */
          	this.queryBuilder = function(entityType){
        		return tcIcatQueryBuilder.create(this, entityType);
        	};

            this.getSize = helpers.overload({
                /**
                 * Gets the total file size of a particular entity.
                 *
                 * @method
                 * @name  Icat#getSize
                 * @param  {string} entityType the type of entity can be 'investigation', 'dataset' or 'datafile'
                 * @param  {number} entityId the id of the entity
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<number>} the defered size in bytes
                 */
                'string, number, object': function(entityType, entityId, options){
                    var key = 'getSize:' + entityType + ":" + entityId;
                    // Allow config to set a lifetime for Investigation entries - issue #394
                    var lifetime = 0;
                    if (entityType == 'investigation'){
                    	var investigationLifetime = tc.config().investigationSizeCacheLifetimeSeconds;
                	    lifetime = investigationLifetime ? investigationLifetime : 0;
                    }
                    return this.cache().getPromise(key, lifetime, function(){
                      var params = {
                        facilityName: facility.config().name,
                        sessionId: that.session().sessionId,
                        entityType: entityType,
                        entityId: entityId
                      };
                      options.lowPriority = true;
                      return facility.tc().get('user/getSize', params, options).then(function(size){
                        return parseInt('' + size);
                      });
                    });
                },

                /**
                 * Gets the total file size of a particular entity.
                 *
                 * @method
                 * @name  Icat#getSize
                 * @param  {string} entityType the type of entity can be 'investigation', 'dataset' or 'datafile'
                 * @param  {number} entityId the id of the entity
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise<number>} the defered size in bytes
                 */
                'string, number, promise': function(entityType, entityId, timeout){
                    return this.getSize(entityType, entityId, {timeout: timeout});
                },

                /**
                 * Gets the total file size of a particular entity.
                 *
                 * @method
                 * @name  Icat#getSize
                 * @param  {string} entityType the type of entity can be 'investigation', 'dataset' or 'datafile'
                 * @param  {number} entityId the id of the entity
                 * @return {Promise<number>} the defered size in bytes
                 */
                'string, number': function(entityType, entityId){
                    return this.getSize(entityType, entityId, {});
                }
            });

          	helpers.generateRestMethods(this, facility.config().icatUrl + '/icat/');

            helpers.mixinPluginMethods('icat', this);
        }

	});

})();

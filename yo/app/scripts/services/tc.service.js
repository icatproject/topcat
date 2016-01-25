
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tc', function($sessionStorage, $http, $q, $state, APP_CONFIG){
    	var tc = this;
    	var facilities = {};

    	this.facility = function(facilityName){
    		if(!facilities[facilityName]) facilities[facilityName] = new Facility(facilityName);
    		return facilities[facilityName];
    	};

    	this.facilities = function(){
    		return _.map(APP_CONFIG.facilities, function(facility){ return tc.facility(facility.facilityName); });
    	};

    	this.config = function(){ return APP_CONFIG.site; }

    	this.version = function(){
  			var out = $q.defer();
  			this.get('version').then(function(data){
  				out.resolve(data.value);
  			}, function(){ out.reject(); });
  			return out.promise;
	    };

		this.search = overload({
			'array, object, object': function(facilityNames, query, options){
				var defered = $q.defer();
				var promises = [];
				var results = [];
				var entityType = query.target;
				var entityInstanceName = instanceNameFromEntityType(entityType);
				_.each(facilityNames, function(facilityName){
					var facility = tc.facility(facilityName);
					var icat = facility.icat();

					promises.push(icat.get('lucene/data', {
					    sessionId: icat.session().sessionId,
					    query: JSON.stringify(query),
					    maxCount: 300
					}, options).then(function(data){
						if(data.length > 0){
							var ids = [];
							var scores = {};
							_.each(data, function(result){
								var entity = result[entityType];
								ids.push(entity.id);
								scores[entity.id] = result.score;
							});

							var promises = [];
							_.each(_.chunk(ids, 100), function(ids){
								var query = [
									function(){
										if(entityType == 'Investigation'){
											return 'select investigation from Investigation investigation';
										} else if(entityType == 'Dataset') {
											return 'select dataset from Dataset dataset';
										} else {
											return 'select datafile from Datafile datafile';
										}
									},
									'where ?.id in (?)', entityInstanceName.safe(), ids.join(', ').safe(),
									function(){
										if(entityType == 'Dataset'){
											return 'include dataset.investigation';
										} else if(entityType == 'Datafile') {
											return 'include datafile.dataset.investigation';
										}
									}
								];
								promises.push(icat.query(query, options).then(function(_results){
									var _results = _.map(_results, function(result){
										result.facilityName = facilityName;
										result.score = scores[result.id];
										extendEntity(result, facility, options);
										return result;
									});

									results = _.sortBy(_.flatten([results, _results]), 'score').reverse();
									defered.notify(results);
								}));
							})
							return $q.all(promises);
						}

					}));
				});

				$q.all(promises).then(function(){
					defered.resolve(results);
				}, function(response){
					defered.reject(response);
				});

				return defered.promise;
			},
			'array, promise, object': function(facilityNames, timeout, query){
				return this.search(facilityNames, query, {timeout: timeout});
			},
			'array, object': function(facilityNames, query){
				return this.search(facilityNames, query, {});
			},
			'array, string, string': function(facilityNames, target, text){
				return this.search(facilityNames, {target: target, text: text}, {});
			}
		});

		this.icat = function(facilityName){ return this.facility(facilityName).icat(); };

		this.ids = function(facilityName){ return this.facility(facilityName).ids(); };

		this.admin = function(facilityName){ return this.facility(facilityName).admin(); };

		this.adminFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().isAdmin; });
		};

    	function Facility(facilityName){
    		var icat;
    		var ids;
    		var admin;
    		
    		this.config = function(){ return APP_CONFIG.facilities[facilityName]; }

    		this.icat = function(){
    			if(!icat) icat = new Icat(this);
    			return icat;
    		};

    		this.ids = function(){
    			if(!ids) ids = new Ids(this);
    			return ids;
    		};

    		this.admin = function(){
    			if(!admin) admin = new Admin(this);
    			return admin;
    		}

    	}

    	function Admin(facility){
    		var that = this;

    		this.isValidSession = overload({
    			'string, object': function(sessionId, options){
	    			return this.get('isValidSession', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: sessionId
	    			});
	    		},
				'string, promise': function(sessionId, timeout){
					return this.isValidSession(sessionId, {timeout: timeout});
				},
				'string': function(sessionId){
					return this.isValidSession(sessionId, {});
				},
				'promise': function(timeout){
					return this.isValidSession(facility.icat().session().sessionId, {timeout: timeout});
				},
	    		'': function(){
	    			return this.isValidSession(facility.icat().session().sessionId, {});
	    		}
    		});

    		this.downloads = overload({
    			'object, object': function(params, options){
    				params.queryOffset = "where download.facilityName = " + jpqlSanitize(facility.config().facilityName) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

    				return this.get('downloads', _.merge({
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId
	    			}, params), options).then(function(downloads){
    					_.each(downloads, function(download){

    						download.delete = overload({
	    						'object': function(options){
	    							return that.deleteDownload(this.id, options);
	    						},
	    						'promise': function(timeout){
	    							return this.delete(this.id, {timeout: timeout});
	    						},
	    						'': function(){
	    							return this.delete(this.id, {});
	    						}
	    					});

    						download.restore = overload({
	    						'object': function(options){
	    							return that.restoreDownload(this.idd, options);
	    						},
	    						'promise': function(timeout){
	    							return this.restore(this.id, {timeout: timeout});
	    						},
	    						'': function(){
	    							return this.restore(this.id, {});
	    						}
	    					});

    					});

    					return downloads;
	    			});
    			},
    			'promise, array': function(timeout, queryOffset){
    				return this.downloads({queryOffset: buildQuery(queryOffset)}, {timeout: timeout});
    			},
    			'array': function(queryOffset){
    				return this.downloads({queryOffset: buildQuery(queryOffset)}, {});
    			},
    			'promise, string': function(timeout, queryOffset){
    				return this.downloads([queryOffset], {timeout: timeout});
    			},
    			'string': function(queryOffset){
    				return this.downloads([queryOffset]);
    			},
    			'promise': function(timeout){
    				return this.downloads(params, {timeout: timeout});
    			},
	    		'': function(){
	    			return this.downloads({}, {});
	    		}
    		});

			this.deleteDownload = overload({
				'string, object': function(id, options){
					return this.put('download/' + id + '/isDeleted', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId,
	    				id: id,
	    				value: 'true'
	    			}, options);
				},
				'string, promise': function(id, timeout){
					return this.deleteDownload(id, {timeout: timeout});
				},
				'string': function(id){
					return this.deleteDownload(id, {});
				},
				'number, object': function(id, options){
					return this.deleteDownload("" + id, options);
				},
				'number, promise': function(id, timeout){
					return this.deleteDownload("" + id, {timeout: timeout});
				},
				'number': function(id){
					return this.deleteDownload("" + id, {});
				}
			});

			this.restoreDownload = overload({
				'string, object': function(id, options){
					return this.put('download/' + id + '/isDeleted', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId,
	    				id: id,
	    				value: 'false'
	    			}, options);
				},
				'string, promise': function(id, timeout){
					return this.restoreDownload(id, {timeout: timeout});
				},
				'string': function(id){
					return this.restoreDownload(id, {});
				},
				'number, object': function(id, options){
					return this.restoreDownload("" + id, options);
				},
				'number, promise': function(id, timeout){
					return this.restoreDownload("" + id, {timeout: timeout});
				},
				'number': function(id){
					return this.restoreDownload("" + id, {});
				}
			});

			this.setDownloadStatus = overload({
				'string, string, object': function(id, status, options){
					return this.put('download/' + id + '/status', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId,
	    				id: id,
	    				value: status
	    			}, options);
				},
				'string, string, promise': function(id, status, timeout){
					return this.setDownloadStatus(id, status, {timeout: timeout});
				},
				'string, string': function(id, status){
					return this.setDownloadStatus(id, status, {});
				},
				'number, string, object': function(id, status, options){
					return this.setDownloadStatus("" + id, status, options);
				},
				'number, string, promise': function(id, status, timeout){
					return this.setDownloadStatus("" + id, status, {timeout: timeout});
				},
				'number, string': function(id, status){
					return this.setDownloadStatus("" + id, status, {});
				}
			});

			generateRestMethods.call(this, topcatApiPath + 'admin/');
		}

    	function Icat(facility){

    		this.version = function(){
    			var out = $q.defer();
    			this.get('version').then(function(data){
    				out.resolve(data.version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

    		this.session = function(){
    			var facilityName = facility.config().facilityName;
    			if($sessionStorage.sessions && $sessionStorage.sessions[facilityName]){
    				return $sessionStorage.sessions[facilityName];
    			}
    			return {};
    		};

    		this.login = function(plugin, username, password){
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
    				var facilityName = facility.config().facilityName;
    				$sessionStorage.sessions[facilityName] = {
    					sessionId: response.sessionId,
    					userName: plugin + '/' + username
    				}
    			});

    		};

    		this.logout = function(){
	            return this.delete('session/' + this.config().sessionId, {
	            	server: facility.config().icatUrl
	            });
	        };

	        this.query = overload({
	        	'array, object': function(query, options){    	
		        	var defered = $q.defer();
		    
		        	this.get('entityManager', {
	                    sessionId: this.session().sessionId,
	                    query: buildQuery(query),
	                    server: facility.config().icatUrl
	                }, options).then(function(results){
	                	defered.resolve(_.map(results, function(result){
	                		var type = _.keys(result)[0];
	                		if(typeOf(result) != 'object' || !type) return result;
    	        				var out = result[type];
    	        				out.entityType = type;
    	        				extendEntity(out, facility, options);
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


	        this.entities = overload({
	        	'string, array, object': function(type, query, options){
	        		return this.query([[
	        			'select ' + instanceNameFromEntityType(type) + ' from ' + type + ' ' + instanceNameFromEntityType(type)
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

    		generateRestMethods.call(this, facility.config().icatUrl + '/icat/');
    	}

    	function Ids(facility){
    		this.version = function(){
    			var out = $q.defer();
    			this.get('getApiVersion').then(function(version){
    				out.resolve(version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

    		this.getSize = overload({
    			'string, number, object': function(type, id, options){
    				var idsParamName = instanceNameFromEntityType(type) + "Ids";
    				var params = {
    					server: facility.config().icatUrl,
    					sessionId: facility.icat().session().sessionId
    				};
    				params[idsParamName] = id;
    				return this.get('getSize', params,  options);
    			},
    			'string, number, promise': function(type, id, timeout){
    				return this.getSize(type, id, {timeout: timeout});
    			},
    			'string, number': function(type, id){
    				return this.getSize(type, id, {});
    			}
    		});

    		generateRestMethods.call(this, facility.config().idsUrl + '/ids/');
    	}

    	function generateRestMethods(prefix){
			
			defineMethod.call(this, 'get');
			defineMethod.call(this, 'delete');
			defineMethod.call(this, 'post');
			defineMethod.call(this, 'put');

			function defineMethod(methodName){
				this[methodName] = overload({
					'string, string, object': function(offset, params, options){
						if(methodName.match(/post|put/)){
							if(!options.headers) options.headers = {};
							if(!options.headers['Content-Type']) options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
						}
						if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
						var url = prefix + offset;
						if(methodName.match(/get|delete/) && params !== '') url += '?' + params;
						var out = $q.defer();
						var args = [url];
						if(methodName.match(/post|put/)) args.push(params);
						args.push(options);
						$http[methodName].apply($http, args).then(function(response){
							out.resolve(response.data);
						}, function(response){
							out.reject(response.data);
						});
						return out.promise;
		    		},
					'string, object, object': function(offset, params, options){
						return this[methodName].call(this, offset, urlEncode(params), options)
		    		},
		    		'string, promise, object': function(offset, timeout, params){
		    			return this[methodName].call(this, offset, params, {timeout: timeout});
		    		},
		    		'string, object': function(offset, params){
		    			return this[methodName].call(this, offset, params, {});
		    		},
		    		'string, promise': function(offset, timeout){
		    			return this[methodName].call(this, offset, {}, {timeout: timeout});
		    		},
		    		'string': function(offset){
		    			return this[methodName].call(this, offset, {}, {});
		    		}
				});
			}

		}

		var topcatApiPath = this.config().topcatApiPath;
		if(!topcatApiPath.match(/^https:\/\//)) topcatApiPath = '/' + topcatApiPath;
		if(!topcatApiPath.match(/\/$/)) topcatApiPath = topcatApiPath + '/';
		generateRestMethods.call(this, topcatApiPath);

		(function(){
			var methods = {
	            get: $http.get,
	            delete: $http.delete,
	            post: $http.post,
	            put: $http.put
	        };

	        _.each(methods, function(method, name){
	            $http[name] = function(){
	                return extendPromise(method.apply(this, arguments));
	            };
	        });

	        var deferMethod = $q.defer;
	        $q.defer = function(){
	        	var out = deferMethod.apply(this, arguments);
	        	extendPromise(out.promise);
	        	return out;
	        };

	        function extendPromise(promise){
				promise.log = function(){
		            return this.then(function(data){
		                console.log('(success)', data); 
		            }, function(data){
		                console.log('(error)', data);   
		            }, function(data){
		                console.log('(notify)', data);  
		            });
		        };

		        var then = promise.then;
		        promise.then = function(){
		        	return extendPromise(then.apply(this, arguments));
		        };

		        return promise;
			}

	    })();

	    function extendEntity(entity, facility, options){
			var icat = facility.icat();
			var facilityName = facility.config().facilityName;

			if(entity.investigationInstruments && entity.investigationInstruments.length > 0){
				entity.firstInstrumentName = entity.investigationInstruments[0].instrument.fullName;
				entity.instrumentNames = _.map(entity.investigationInstruments, function(investigationInstrument){
					return investigationInstrument.instrument.fullName;
				}).join("\n");
			}

			entity.getSize = overload({
				'object': function(options){
					var that = this;
					return facility.ids().getSize(this.entityType, this.id, options).then(function(size){
						that.size = size;
						return size;
					});
				},
				'promise': function(timeout){
					return this.getSize({timeout: timeout});
				},
				'': function(){
					return this.getSize({});
				}
			});

			var parentFunctions = {
				Datafile: function(datafile, options){
					var defered = $q.defer();
					var dataset = datafile.dataset;
					if(dataset){
						defered.resolve(_.merge(dataset, {entityType: 'Dataset'}));
					} else {
						icat.entity('Dataset', [
							', dataset.datafiles datafile',
							'where datafile.id = ?', datafile.id
						], options).then(function(dataset){
							datafile.dataset = dataset;
							defered.resolve(dataset);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise
				},
				Dataset: function(dataset, options){
					var defered = $q.defer();
					var investigation = dataset.investigation;
					if(investigation){
						defered.resolve(_.merge(investigation, {entityType: 'Investigation'}));
					} else {
						icat.entity('Investigation', [
							', investigation.datasets dataset',
							'where dataset.id = ?', dataset.id
						], options).then(function(investigation){
							dataset.investigation = investigation;
							defered.resolve(investigation);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise
				},
				Investigation: function(investigation, options){
					var defered = $q.defer();
					var facilityCycle = investigation.facilityCycle;
					if(facilityCycle){
						defered.resolve(facilityCycle);
					} else {
						icat.entity('FacilityCycle', [
							', facilityCycle.facility facility,',
							'facility.investigations investigation',
							'where facility.id = ?', facility.config().facilityId,
							'and investigation.id = ?', investigation.id,
							'and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate'
						], options).then(function(facilityCycle){
							investigation.facilityCycle = facilityCycle;
							defered.resolve(facilityCycle);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise;
				},
				FacilityCycle: function(facilityCycle, childEntity, options){
					if(!_.includes(['Investigation', 'Dataset', 'Datafile'], childEntity.entityType)){
						return resolvedPromise(null);
					}
					return childEntity.thisOrParent('Investigation').then(function(investigation){
						var defered = $q.defer();
						var instrument = facilityCycle.instrument;
						if(instrument){
							defered.resolve(instrument);
						} else {
							icat.entity('Instrument', [
								', instrument.investigationInstruments investigationInstrument,',
								'investigationInstrument.investigation investigation,',
								'instrument.facility facility',
								'where facility.id = ?', facility.config().facilityId,
								'and investigation.id = ?', investigation.id,
							], options).then(function(instrument){
								facilityCycle.instrument = instrument;
								defered.resolve(instrument);
							}, function(response){
								defered.reject(response);
							});
						}
						return defered.promise;
					});
				}
	
			};


			var parent; 
			entity.parent = overload({
				'string, object, object': function(entityType, childEntity, options){
					return this.parent(childEntity, options).then(function(entity){
						if(!entity || entity.entityType == entityType){
							return entity;
						} else {
							return entity.parent(entityType, childEntity, options);
						}
					});
				},
				'string, promise': function(entityType, timeout){
					return this.parent(entityType, entity, {timeout: timeout});	
				},
				'string': function(entityType){
					return this.parent(entityType, entity, {});	
				},
				'object, object': function(childEntity, options){
					var defered = $q.defer();
					if(parent !== undefined){
						defered.resolve(parent);
					} else {
						var parentFunction = parentFunctions[this.entityType];
						if(parentFunction){
							parentFunction(this, childEntity, options).then(function(_parent){
								parent = _parent;
								defered.resolve(parent);
							});
						} else {
							parent = null;
							defered.resolve(parent);
						}
					}
					return defered.promise;
				},
				'object': function(childEntity){
					return this.parent(childEntity, options);	
				},
				'object, promise': function(childEntity, timeout){
					return this.parent(childEntity, {timeout: timeout});	
				},
				'promise': function(timeout){
					return this.parent(entity, {timeout: timeout});	
				},
				'': function(){
					return this.parent(entity, {});	
				}
			});

			entity.thisOrParent = function(entityType){
				if(this.entityType == entityType){
					return resolvedPromise(this);
				} else {
					return this.parent(entityType);
				}
			};
				

			entity.ancestors = function(){
				var defered = $q.defer();
				var out = [];
				var childEntity = this;
				function parent(){
					this.parent(childEntity).then(function(entity){
						if(entity){
							out.push(entity);
							parent.call(entity);
						} else {
							defered.resolve(out);
						}
					});
				}
				parent.call(this);
				return defered.promise;
			};

			entity.thisAndAncestors = function(){
				var that = this;
				return this.ancestors().then(function(ancestors){
					return _.flatten([that, ancestors]);
				});
			};

			entity.stateParams = function(){
				return this.thisAndAncestors().then(function(thisAndAncestors){
					var out = {};
					_.each(thisAndAncestors, function(entity){
						out[instanceNameFromEntityType(entity.entityType) + "Id"] = entity.id;
						if(entity.entityType == 'Investigation') out['proposalId'] = entity.name;
					});
					return _.merge(out, {facilityName: facilityName});
				});

			};

			entity.browse = function(){
				var that = this;
				this.stateParams().then(function(params){
					var state = [];
					var hierarchy =  facility.config().hierarchy;
					for(var i in hierarchy){
						state.push(hierarchy[i]);
						if (('' + hierarchy[i - 1]).toLowerCase() == that.entityType.toLowerCase()) break;
					}
					state = "home.browse.facility." + state.join('-');
					$state.go(state, params);
				});
			};

			if(entity.dataset){
				entity.dataset.entityType = "Dataset";
				extendEntity(entity.dataset, facility, options);
			}
			if(entity.investigation){
				entity.investigation.entityType = "Investigation";
				extendEntity(entity.investigation, facility, options);
			}
		}

		function resolvedPromise(value){
			var defered = $q.defer();
			defered.resolve(value);
			return defered.promise;
		}

  	});

	function typeOf(data){
		var out = typeof data;
		if(out == 'object'){
			if(data instanceof Array) return 'array';
			if(data.then instanceof Function) return 'promise';
		}
		return out;
	}

	function overload(variations){

		return function(){
			var that = this;
			var args = arguments;
			var argTypeOfs = _.map(args,  function(arg){ return typeOf(arg); });
			var found = false;
			var out;
			if(!variations.default){
				variations.default = function(){
					throw "Could not satisfy overloaded function '" + argTypeOfs.join(', ') + "'.";
				};
			}

			_.each(variations, function(fn, pattern){
				if(pattern == 'default') return false;
				pattern = pattern.trim().split(/\s*,\s*/);
				found = _.isEqual(argTypeOfs, pattern);
				if(found){
					out = fn.apply(that, args);
					return false;
				}
			});

			if(argTypeOfs.length == 0 && variations['']){
				out = variations[''].apply(that, args);
			} else if(!found){
				out = variations.default.apply(that, args);
			}

			return out;
		};
	}

	function urlEncode(o){
		var out = [];
		_.each(o, function(value, key){
			out.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
		});
		return out.join('&');
	}

	function jpqlSanitize(data){
		if(typeof data == 'string' && !data.isSafe){
			return "'" + data.replace(/'/g, "''") + "'";
		}
		return data;
	}

	function buildQuery(query){
		while(true){
        	query = _.map(query, function(i){
        		if(typeOf(i) == 'function') i = i.call(this);
        		return i;
        	});
        	query = _.flatten(query);
        	var isFunction = _.select(query, function(i){ return typeOf(i) == 'function'; }).length > 0;
        	var isArray = _.select(query, function(i){ return typeOf(i) == 'array'; }).length > 0;
        	if(!isFunction && !isArray) break;
        }

        query = _.select(query, function(i){ return i !== undefined; });

        try {
        	var _query = [];
        	for(var i = 0; i < query.length; i++){
        		var expression = [];
        		var fragments = query[i].split(/\?/);
        		for(var j in fragments){
        			expression.push(fragments[j]);
        			if(j < fragments.length - 1){
        				i++;
        				expression.push(jpqlSanitize(query[i]));
        			}
        		}
        		_query.push(expression.join(''));
        	}
        } catch(e) {
        	console.error("can't build query", query, e)
        }
        return _query.join(' ');
	}

	String.prototype.safe = function(){
		return new SafeString(this);
	};

	function SafeString(value){
		this.value = value;
	}

	SafeString.prototype.toString = function(){
		return this.value;
	};

	function instanceNameFromEntityType(entityType){
		return entityType.replace(/^(.)/, function(s){ return s.toLowerCase(); });
	}

})();
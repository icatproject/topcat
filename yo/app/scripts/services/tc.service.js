
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tc', function($sessionStorage, $http, $q, APP_CONFIG){
    	var that = this;
    	var facilities = {};

    	this.facility = function(facilityName){
    		if(!facilities[facilityName]) facilities[facilityName] = new Facility(facilityName);
    		return facilities[facilityName];
    	};

    	this.facilities = function(){
    		return _.map(APP_CONFIG.facilities, function(facility){ return that.facility(facility.facilityName); });
    	};

    	this.config = function(){ return APP_CONFIG.site; }

    	this.version = function(){
			var out = $q.defer();
			this.get('version').then(function(data){
				out.resolve(data.value);
			}, function(){ out.reject(); });
			return extendPromise(out.promise);
		};

		this.icat = function(facilityName){ return this.facility(facilityName).icat(); };

		this.ids = function(facilityName){ return this.facility(facilityName).ids(); };

    	function Facility(facilityName){
    		var icat;
    		var ids;

    		this.config = function(){ return APP_CONFIG.facilities[facilityName]; }

    		this.icat = function(){
    			if(!icat) icat = new Icat(this);
    			return icat;
    		};

    		this.ids = function(){
    			if(!ids) ids = new Ids(this);
    			return ids;
    		};
    	}

    	function Icat(facility){

    		this.version = function(){
    			var out = $q.defer();
    			this.get('version').then(function(data){
    				out.resolve(data.version);
    			}, function(){ out.reject(); });
    			return extendPromise(out.promise);
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

	        this.query = overload(this, {
	        	'array, object': function(query, options){
	        		while(_.select(query, function(i){ return typeOf(i) == 'array'; }).length > 0){
		        		query = _.flatten(query);
		        	}

		        	query = _.map(query, function(i){
		        		if(typeOf(i) == 'function') i = i.call(this);
		        		return i;
		        	});

		        	while(_.select(query, function(i){ return typeOf(i) == 'array'; }).length > 0){
		        		query = _.flatten(query);
		        	}

		        	var _query = [];
		        	for(var i = 0; i < query.length; i++){
		        		var fragments = query[i].split(/\?/);
		        		for(var j in fragments){
		        			_query.push(fragments[j]);
		        			if(j < fragments.length - 1){
		        				i++;
		        				_query.push(jpqlSanitize(query[i]));
		        			}
		        		}
		        	}
		        	return this.get('entityManager', {
	                    sessionId: this.session().sessionId,
	                    query: _query.join(' '),
	                    server: facility.config().icatUrl
	                }, options);
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


	        this.entities = overload(this, {
	        	'string, array, object': function(type, query, options){
	        		var out = $q.defer();
	        		var instanceName = type.replace(/^./, function(s){ return s.toLowerCase(); })
	        		this.query([[
	        			'select ' + instanceName + ' from ' + type + ' ' + instanceName
	        		], query], options).then(function(results){
	        			out.resolve(_.map(results, function(result){
	        				var out = result[type];
	        				out.getSize = overload(out, {
	        					'object': function(options){
	        						var that = this;
	        						return extendPromise(facility.ids().getSize(type, this.id, options).then(function(size){
			        					that.size = size;
			        				}));
	        					},
	        					'promise': function(timeout){
	        						return this.getSize({timeout: timeout});
	        					},
	        					'': function(){
	        						return this.getSize({});
	        					}
	        				});

	        				return out;
	        			}));
	        		}, function(results){
	        			out.reject(results);
	        		});
	        		return extendPromise(out.promise);
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
        		return extendPromise(out.promise);
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

    		this.getSize = overload(this, {
    			'string, number, object': function(type, id, options){
    				var idsParamName = type.replace(/^./, function(s){ return s.toLowerCase(); }) + "Ids";
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
				this[methodName] = overload(this, {
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
						return extendPromise(out.promise);
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

    });

	function typeOf(data){
		var out = typeof data;
		if(out == 'object'){
			if(data instanceof Array) return 'array';
			if(data instanceof Function) return 'function';
			if(data.then instanceof Function) return 'promise';
		}
		return out;
	}

	function overload(_this, variations){
		return function(){
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
					out = fn.apply(_this, args);
					return false;
				}
			});

			if(argTypeOfs.length == 0 && variations['']){
				out = variations.default.apply(_this, args);
			} else if(!found){
				out = variations.default.apply(_this, args);
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

	function extendPromise(promise){
		promise.log = function(){
			return this.then(function(data){
				console.log(data);	
			}, function(data){
				console.error(data);	
			});
		};
		return promise;
	}

	function jpqlSanitize(data){
		if(typeof data == 'string'){
			return "'" + data.replace(/'/g, "''") + "'";
		}
		return data;
	}

})();
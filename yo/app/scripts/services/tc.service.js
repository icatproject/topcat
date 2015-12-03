
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tc', function($sessionStorage, $http, $q, APP_CONFIG){
    	var that = this;
    	var facilities = {};
    	var cancellerStack = [];

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
	        	'array': function(query){
		        	if(typeof query == 'string') query = [query];

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
	                    query: _query.join(''),
	                    server: facility.config().icatUrl
	                });
		        },
		        'string': function(query){
		        	return this.query([query]);
		        }
	        });

	        this.query = function(query){
	        	if(typeof query == 'string') query = [query];

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
                });
	        };

	        this.entities = overload(this, {
	        	'string, array': function(type, query){
	        		var out = $q.defer();
	        		var instanceName = type.replace(/^./, function(s){ return s.toLowerCase(); })
	        		this.query([[
	        			'select ' + instanceName + ' from ' + type + ' ' + instanceName
	        		], query]).then(function(results){
	        			out.resolve(_.map(results, function(result){
	        				return result[type];
	        			}));
	        		}, function(results){
	        			out.reject(results);
	        		});
	        		return extendPromise(out.promise);
	        	},
	        	'string, string': function(type, query){
	        		return this.entities(type, [query]);
	        	},
	        	'string': function(type){
	        		return this.entities(type, []);
	        	}
	        });

			

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

    		generateRestMethods.call(this, facility.config().idsUrl + '/ids/');
    	}

    	function generateRestMethods(prefix){
			this.get = overload(this, {
				'string, object, object': function(offset, params, options){
					if(!options.headers) options.headers = {};
					if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
					params = urlEncode(params);
					var url = prefix + offset;
					if(params !== '') url += '?' + params;
	    			var out = $q.defer();
					$http.get(url, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return extendPromise(out.promise);

	    		},
	    		'string, object': function(offset, params){
	    			return this.get(offset, params, {});
	    		},
	    		'string': function(offset){
	    			return this.get(offset, {}, {});
	    		}
			});

			this.delete = overload(this, {
				'string, object, object': function(offset, params, options){
					if(!options.headers) options.headers = {};
					if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
					params = urlEncode(params);
					var url =  prefix + offset;
					if(params !== '') url += '?' + params;
	    			var out = $q.defer();
					$http.delete(url, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return extendPromise(out.promise);
	    		},
	    		'string, object': function(offset, params){
	    			return this.delete(offset, params, {});
	    		},
	    		'string': function(offset){
	    			return this.delete(offset, {}, {});
	    		}
			});

			this.post = overload(this, {
				'string, string, object': function(offset, params, options){
					if(!options.headers) options.headers = {};
					if(!options.headers['Content-Type']) options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
					if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
					var url = prefix + offset;
					var out = $q.defer();
					$http.post(url, params, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return extendPromise(out.promise);
	    		},
				'string, object, object': function(offset, params, options){
					return this.post(offset, urlEncode(params), options)
	    		},
	    		'string, object': function(offset, params){
	    			return this.post(offset, params, {});
	    		},
	    		'string': function(offset){
	    			return this.post(offset, {}, {});
	    		}
			});

			this.put = overload(this, {
				'string, string, object': function(offset, params, options){
					if(!options.headers) options.headers = {};
					if(!options.headers['Content-Type']) options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
					if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
					var url = prefix + offset;
					var out = $q.defer();
					$http.put(url, params, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return extendPromise(out.promise);
	    		},
				'string, object, object': function(offset, params, options){
					return this.put(offset, urlEncode(params), options)
	    		},
	    		'string, object': function(offset, params){
	    			return this.put(offset, params, {});
	    		},
	    		'string': function(offset){
	    			return this.put(offset, {}, {});
	    		}
			});
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

			if(!found){
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
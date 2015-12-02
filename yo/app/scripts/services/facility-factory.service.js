
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('FacilityFactory', function($sessionStorage, $http, $q, APP_CONFIG){
    	var that = this;
    	var facilities = {};

    	this.facility = function(facilityName){
    		if(!facilities[facilityName]) facilities[facilityName] = new Facility(facilityName);
    		return facilities[facilityName];
    	};

    	this.facilities = function(){
    		return _.map(APP_CONFIG.facilities, function(facility){ return that.facility(facility.facilityName); });
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
    		generateRestMethods.call(this, facility, '/icat/');

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
    			return this.post('session', params).then(function(){
    					
    			});
    		};

    	}

    	function Ids(facility){
    		generateRestMethods.call(this, facility, '/ids/');

    		this.version = function(){
    			var out = $q.defer();
    			this.get('getApiVersion').then(function(version){
    				out.resolve(version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};
    	}

    	function generateRestMethods(facility, prefix){
			this.get = overload(this, {
				'string, object, object': function(offset, params, options){
					if(!options.headers) options.headers = {};
					if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
					params = urlEncode(params);
					var url = facility.config().icatUrl + prefix + offset;
					if(params !== '') url += '?' + params;
	    			var out = $q.defer();
					$http.get(url, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return out.promise;

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
					var url = facility.config().icatUrl + prefix + offset;
					if(params !== '') url += '?' + params;
	    			var out = $q.defer();
					$http.delete(url, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return out.promise;
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
					var url = facility.config().icatUrl + prefix + offset;
					var out = $q.defer();
					$http.post(url, params, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return out.promise;
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
					var url = facility.config().icatUrl + prefix + offset;
					var out = $q.defer();
					$http.put(url, params, options).then(function(response){
						out.resolve(response.data);
					}, function(response){
						out.reject(response.data);
					});
					return out.promise;
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

    });

	function overload(_this, variations){
		return function(){
			var args = arguments;
			var argTypeOfs = _.map(args,  function(arg){ return typeof arg; });
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

})();
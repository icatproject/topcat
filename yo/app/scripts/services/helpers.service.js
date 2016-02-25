
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('helpers', function($http, $q){
    	var helpers = this;

    	this.completePartialFromDate = function(date){
            var segments = date.split(/[-:\s\/]+/);
            var year = segments[0];
            var month = segments[1] || "01";
            var day = segments[2] || "01";
            var hours = segments[3] || "00";
            var minutes = segments[4] || "00";
            var seconds = segments[5] || "00";

            year = year + '0000'.slice(year.length, 4);
            month = month + '00'.slice(month.length, 2);
            day = day + '00'.slice(day.length, 2);
            hours = hours + '00'.slice(hours.length, 2);
            minutes = minutes + '00'.slice(minutes.length, 2);
            seconds = seconds + '00'.slice(seconds.length, 2);

            if(parseInt(month) == 0) month = '01';
            if(parseInt(day) == 0) day = '01';

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        };

        this.completePartialToDate = function(date){
            var segments = date.split(/[-:\s\/]+/);
            var year = segments[0] || "";
            var month = segments[1] || "";
            var day = segments[2] || "";
            var hours = segments[3] || "23";
            var minutes = segments[4] || "59";
            var seconds = segments[5] || "59";
            year = year + '9999'.slice(year.length, 4);
            month = month + '99'.slice(month.length, 2);
            day = day + '99'.slice(day.length, 2);
            hours = hours + '33'.slice(hours.length, 2);
            minutes = minutes + '99'.slice(minutes.length, 2);
            seconds = seconds + '99'.slice(seconds.length, 2);

            if(parseInt(month) > 12) month = '12';
            var daysInMonth = new Date(year, day, 0).getDate();
            if(parseInt(day) > daysInMonth) day = daysInMonth;

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        };

        this.typeOf = function(data){
			var out = typeof data;
			if(out == 'object'){
				if(data instanceof Array) return 'array';
				if(data.then instanceof Function) return 'promise';
			}
			return out;
		}

		this.overload = function(variations){

			return function(){
				var that = this;
				var args = arguments;
				var argTypeOfs = _.map(args,  function(arg){ return helpers.typeOf(arg); });
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

		this.jpqlSanitize = function(data){
			if(typeof data == 'string' && !data.isSafe){
				return "'" + data.replace(/'/g, "''") + "'";
			}
			return data;
		};

		this.buildQuery = function(query){
			while(true){
	        	query = _.map(query, function(i){
	        		if(helpers.typeOf(i) == 'function') i = i.call(this);
	        		return i;
	        	});
	        	query = _.flatten(query);
	        	var isFunction = _.select(query, function(i){ return helpers.typeOf(i) == 'function'; }).length > 0;
	        	var isArray = _.select(query, function(i){ return helpers.typeOf(i) == 'array'; }).length > 0;
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
	        				expression.push(helpers.jpqlSanitize(query[i]));
	        			}
	        		}
	        		_query.push(expression.join(''));
	        	}
	        } catch(e) {
	        	console.error("can't build query", query, e)
	        }
	        return _query.join(' ');
		};

		this.urlEncode = function(o){
			var out = [];
			_.each(o, function(value, key){
				out.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
			});
			return out.join('&');
		};

		this.uncapitalize = function(text){
			return text.replace(/^(.)/, function(s){ return s.toLowerCase(); });
		}

		this.capitalize = function(text){
			return text.replace(/^(.)/, function(s){ return s.toUpperCase(); });
		}

		this.generateRestMethods = function(that, prefix){
			
			defineMethod.call(that, 'get');
			defineMethod.call(that, 'delete');
			defineMethod.call(that, 'post');
			defineMethod.call(that, 'put');

			function defineMethod(methodName){
				this[methodName] = helpers.overload({
					'string, string, object': function(offset, params, options){
						options = _.clone(options);
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
						return this[methodName].call(this, offset, helpers.urlEncode(params), options)
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

		};

		this.resolvedPromise = function(value){
			var defered = $q.defer();
			defered.resolve(value);
			return defered.promise;
		};

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

    });

	String.prototype.safe = function(){
		return new SafeString(this);
	};

	function SafeString(value){
		this.isSafe = true;
		this.value = value;
	}

	SafeString.prototype.toString = function(){
		return this.value;
	};

})();
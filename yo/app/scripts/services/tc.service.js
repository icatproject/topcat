
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tc', function($sessionStorage, $q, $state, $timeout, $rootScope, helpers, icatEntityFactory, APP_CONFIG){
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

		this.search = helpers.overload({
			'array, object, object': function(facilityNames, query, options){
				var defered = $q.defer();
				var promises = [];
				var results = [];
				query.target = query.target.replace(/^./, function(c){ return c.toUpperCase(); });
				var entityType = query.target;
				var entityInstanceName = helpers.uncapitalize(entityType);
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
								ids.push(result.id);
								scores[result.id] = result.score;
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
										if(entityType == 'Investigation'){
											return 'include investigation.investigationInstruments.instrument';
										} else if(entityType == 'Dataset'){
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
										result = icatEntityFactory.create(result, facility);
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

		this.user = function(facilityName){ return this.facility(facilityName).user(); };

		this.adminFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().isAdmin; });
		};

		this.userFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId !== undefined; });
		};

		this.nonUserFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId === undefined; });
		};

		this.purgeSessions = function(){
	    	var promises = [];

	    	_.each(this.userFacilities(), function(facility){
	    		var icat = facility.icat();
	    		promises.push(icat.get('session/' + icat.session().sessionId).then(function(){}, function(response){
	    			console.log(response);
	    			if(response.code == "SESSION"){
	    				return icat.logout();
	    			}
	    		}));
	    	});

	    	return $q.all(promises);
	    };

    	function Facility(facilityName){
    		var icat;
    		var ids;
    		var admin;
    		var user;
    		
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

    		this.user = function(){
    			if(!user) user = new User(this);
    			return user;
    		}

    	}

    	function Admin(facility){
    		var that = this;

    		this.isValidSession = helpers.overload({
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

    		this.downloads = helpers.overload({
    			'object, object': function(params, options){
    				params.queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().facilityName) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

    				return this.get('downloads', _.merge({
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId
	    			}, params), options).then(function(downloads){
    					_.each(downloads, function(download){

    						download.delete = helpers.overload({
	    						'object': function(options){
	    							return that.deleteDownload(this.id, options);
	    						},
	    						'promise': function(timeout){
	    							return this.delete({timeout: timeout});
	    						},
	    						'': function(){
	    							return this.delete({});
	    						}
	    					});

    						download.restore = helpers.overload({
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

	    					download.getSize = helpers.overload({
								'object': function(options){
									var that = this;

									var investigationIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'investigation'}), function(item){ return item.entityId});
									var datasetIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'dataset'}), function(item){ return item.entityId});
									var datafileIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'datafile'}), function(item){ return item.entityId});

									return facility.ids().getSize(investigationIds, datasetIds, datafileIds, options).then(function(size){
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


    					});

    					return downloads;
	    			});
    			},
    			'promise, array': function(timeout, queryOffset){
    				return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {timeout: timeout});
    			},
    			'array': function(queryOffset){
    				return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {});
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

			this.deleteDownload = helpers.overload({
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

			this.restoreDownload = helpers.overload({
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

			this.setDownloadStatus = helpers.overload({
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

			helpers.generateRestMethods(this, topcatApiPath + 'admin/');
		}

		function User(facility){
			var that = this;

			this.downloads = helpers.overload({
    			'object, object': function(params, options){
    				params.queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().facilityName) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

    				return this.get('downloads', _.merge({
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId
	    			}, params), options).then(function(downloads){
    					_.each(downloads, function(download){

    						download.delete = helpers.overload({
	    						'object': function(options){
	    							return that.deleteDownload(this.id, options);
	    						},
	    						'promise': function(timeout){
	    							return this.delete({timeout: timeout});
	    						},
	    						'': function(){
	    							return this.delete({});
	    						}
	    					});

    					});

    					return downloads;
	    			});
    			},
    			'promise, array': function(timeout, queryOffset){
    				return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {timeout: timeout});
    			},
    			'array': function(queryOffset){
    				return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {});
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

			this.deleteDownload = helpers.overload({
				'string, object': function(id, options){
					return this.put('download/' + id + '/isDeleted', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId,
	    				id: id,
	    				value: 'true'
	    			}, options).then(function(){
	    				$rootScope.$broadcast('download:change');
	    			});
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

			var cartCache;
			this.cart = helpers.overload({
				'object': function(options){
					if(cartCache){
						var defered = $q.defer();
						defered.resolve(cartCache);
						return defered.promise;
					}

					return this.get('cart/' + facility.config().facilityName, {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId
	    			}, options).then(function(cart){
	    				extendCart(cart);
	    				cartCache = cart;
	    				return cart;
	    			});
				},
				'promise': function(timeout){
					return this.cart({timeout: timeout})
				},
				'': function(){
					return this.cart({});
				}
			});

			this.addCartItem = helpers.overload({
				'string, number, object': function(entityType, entityId, options){
					return this.cart(options).then(function(cart){
						if(cart.isCartItem(entityType, entityId)){
							return cart;
						} else {
							return that.post('cart/' + facility.config().facilityName + '/cartItem', {
			    				icatUrl: facility.config().icatUrl,
			    				sessionId: facility.icat().session().sessionId,
			    				entityType: entityType,
			    				entityId: entityId
			    			}, options).then(function(cart){
			    				extendCart(cart);
			    				cartCache = cart;
			    				$rootScope.$broadcast('cart:change');
			    				return cart;
			    			});
			    		}
					});
				},
				'string, number, promise': function(entityType, entityId, timeout){
					return this.addCartItem(entityType, entityId, {timeout: timeout})
				},
				'string, number': function(entityType, entityId){
					return this.addCartItem(entityType, entityId, {});
				}
			});

			this.deleteCartItem = helpers.overload({
				'number, object': function(id, options){
					return this.delete('cart/' + facility.config().facilityName + '/cartItem/' + id, {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId
	    			}, options).then(function(cart){
	    				extendCart(cart);
	    				cartCache = cart;
	    				$rootScope.$broadcast('cart:change');
	    				return cart;
	    			});
				},
				'number, promise': function(id, timeout){
					return this.deleteCartItem(id, {timeout: timeout});
				},
				'number': function(id){
					return this.deleteCartItem(id, {});
				},
				'string, number, object': function(entityType, entityId, options){
					return this.cart(options).then(function(cart){
						var promises = [];
						_.each(cart.cartItems, function(cartItem){
							if(cartItem.entityType == entityType && cartItem.entityId == entityId){
								promises.push(that.deleteCartItem(cartItem.id, options));
							}
						});
						return $q.all(promises).then(function(){
							return cart;
						});
					});
				},
				'string, number, promise': function(entityType, entityId, timeout){
					return this.deleteCartItem(entityType, entityId, {timeout: timeout});
				},
				'string, number': function(entityType, entityId){
					return this.deleteCartItem(entityType, entityId, {});
				}
			});

			this.deleteAllCartItems = helpers.overload({
				'object': function(options){
					return this.cart(options).then(function(cart){
						var promises = [];

						_.each(cart.cartItems, function(cartItem){
							promises.push(cartItem.delete(options));
						});

						return $q.all(promises).then(function(){
							return that.cart(options);
						});
					});
				},
				'promise': function(timeout){
					return this.deleteAllCartItems({timeout: timeout})
				},
				'': function(){
					return this.deleteAllCartItems({});
				}
			});

			this.submitCart = helpers.overload({
				'string, string, string, object': function(fileName, transport, email, options){
					var transportTypeIndex = {};
					_.each(facility.config().downloadTransportType, function(downloadTransportType){
						transportTypeIndex[downloadTransportType.type] = downloadTransportType
					})
					var transportType = transportTypeIndex[transport];

					return this.post('cart/' + facility.config().facilityName + '/submit', {
	    				icatUrl: facility.config().icatUrl,
	    				sessionId: facility.icat().session().sessionId,
	    				fileName: fileName,
	    				transport: transport,
	    				email: email,
	    				zipType: transportType.zipType ? transportType.zipType : '',
	    				transportUrl: transportType.url
	    			}, options).then(function(cart){
	    				extendCart(cart);
	    				cartCache = cart;
	    				$rootScope.$broadcast('download:change');
	    				$rootScope.$broadcast('cart:change');
	    				return cart;
	    			});
				},
				'string, string, string, promise': function(fileName, transport, email, timeout){
					return this.submitCart(fileName, transport, email, {timeout: timeout});
				},
				'string, string, string': function(fileName, transport, email){
					return this.submitCart(fileName, transport, email, {});
				}
			});

			function extendCart(cart){

				cart.isCartItem = function(entityType, entityId){
					var out = false;
					_.each(cart.cartItems, function(cartItem){
						if(cartItem.entityType == entityType && cartItem.entityId == entityId){
							out = true;
							return false;
						}
					});
					return out;
				};

				_.each(cart.cartItems, function(cartItem){
					cartItem.facilityName = facility.config().facilityName;

					cartItem.delete = helpers.overload({
						'object': function(options){
							return that.deleteCartItem(this.id, options);
						},
						'promise': function(timeout){
							return this.delete({timeout: timeout});
						},
						'': function(){
							return this.delete({});
						}
					});


					cartItem.entity = helpers.overload({
						'object': function(options){
							return facility.icat().entity(helpers.capitalize(this.entityType), ["where ?.id = ?", this.entityType.safe(), this.entityId], options);
						},
						'promise': function(timeout){
							return this.entity({timeout: timeout});
						},
						'': function(){
							return this.entity({});
						}
					});

					cartItem.getSize = helpers.overload({
						'object': function(options){
							var that = this;
							return this.entity(options).then(function(entity){
								return entity.getSize(options).then(function(size){
									that.size = size;
									return size;
								});
							});
						},
						'promise': function(timeout){
							return this.getSize({timeout: timeout});
						},
						'': function(){
							return this.getSize({});
						}
					});

					cartItem.getStatus = helpers.overload({
						'object': function(options){
							var that = this;
							return this.entity(options).then(function(entity){
								return entity.getStatus(options).then(function(status){
									that.status = status;
									return status;
								});
							});
						},
						'promise': function(timeout){
							return this.getStatus({timeout: timeout});
						},
						'': function(){
							return this.getStatus({});
						}
					});

				});
			}

			helpers.generateRestMethods(this, topcatApiPath + 'user/');
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

    				return facility.admin().isValidSession(response.sessionId).then(function(isAdmin){
	                    $sessionStorage.sessions[facilityName].isAdmin = isAdmin;
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

	        		delete $sessionStorage.sessions[facility.config().facilityName];
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
		    
		        	this.get('entityManager', {
	                    sessionId: this.session().sessionId,
	                    query: helpers.buildQuery(query),
	                    server: facility.config().icatUrl
	                }, options).then(function(results){
	                	defered.resolve(_.map(results, function(result){
	                		var type = _.keys(result)[0];
	                		if(helpers.typeOf(result) != 'object' || !type) return result;
    	        				var out = result[type];
    	        				out.entityType = type;
    	        				out = icatEntityFactory.create(out, facility);
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
	        			'select ' + helpers.uncapitalize(type) + ' from ' + type + ' ' + helpers.uncapitalize(type)
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

    		helpers.generateRestMethods(this, facility.config().icatUrl + '/icat/');
    	}

    	function Ids(facility){
    		this.version = function(){
    			var out = $q.defer();
    			this.get('getApiVersion').then(function(version){
    				out.resolve(version);
    			}, function(){ out.reject(); });
    			return out.promise;
    		};

    		this.getSize = helpers.overload({
    			'string, number, object': function(type, id, options){
    				var idsParamName = helpers.uncapitalize(type) + "Ids";
    				var params = {
    					server: facility.config().icatUrl,
    					sessionId: facility.icat().session().sessionId
    				};
    				params[idsParamName] = id;
    				return this.get('getSize', params,  options).then(function(size){
    					return parseInt('' + size);
    				});
    			},
    			'string, number, promise': function(type, id, timeout){
    				return this.getSize(type, id, {timeout: timeout});
    			},
    			'string, number': function(type, id){
    				return this.getSize(type, id, {});
    			},
    			'array, array, array, object': function(investigationIds, datasetIds, datafileIds, options){
    				var params = {
    					server: facility.config().icatUrl,
    					sessionId: facility.icat().session().sessionId
    				};

    				investigationIds = investigationIds.join(',');
    				datasetIds = datasetIds.join(',');
    				datafileIds = datafileIds.join(',');

    				if(investigationIds != '') params.investigationIds = investigationIds;
    				if(datasetIds != '') params.datasetIds = datasetIds;
    				if(datafileIds != '') params.datafileIds = datafileIds;

    				return this.get('getSize', params,  options).then(function(size){
    					return parseInt('' + size);
    				});
    			},
    			'promise, array, array, array': function(timeout, investigationIds, datasetIds, datafileIds){
    				return this.getSize(investigationIds, datasetIds, datafileIds, {timeout: timeout});
    			},
    			'array, array, array': function(investigationIds, datasetIds, datafileIds){
    				return this.getSize(investigationIds, datasetIds, datafileIds, {});
    			}
    		});

    		this.getStatus = helpers.overload({
    			'string, number, object': function(type, id, options){
    				var idsParamName = helpers.uncapitalize(type) + "Ids";
    				var params = {
    					server: facility.config().icatUrl,
    					sessionId: facility.icat().session().sessionId
    				};
    				params[idsParamName] = id;
    				return this.get('getStatus', params,  options).then(function(status){
    					return status;
    				});
    			},
    			'string, number, promise': function(type, id, timeout){
    				return this.getStatus(type, id, {timeout: timeout});
    			},
    			'string, number': function(type, id){
    				return this.getStatus(type, id, {});
    			}
    		});


    		helpers.generateRestMethods(this, facility.config().idsUrl + '/ids/');
    	}

		var topcatApiPath = this.config().topcatApiPath;
		if(!topcatApiPath.match(/^https:\/\//)) topcatApiPath = '/' + topcatApiPath;
		if(!topcatApiPath.match(/\/$/)) topcatApiPath = topcatApiPath + '/';
		helpers.generateRestMethods(this, topcatApiPath);

  	});

})();


(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIcatEntity', function($http, $q, $rootScope, $state, $injector, helpers, icatSchema, plugins){
    	var tcIcatEntity = this;

    	this.create = function(attributes, facility){
    		return new IcatEntity(attributes, facility);
    	};

    	/**
    	 * Represents the Icat entities as described in the {@link https://repo.icatproject.org/site/icat/server/4.8.0/schema.html|Icat schema documentation} where it describes the attributes for each IcatEntity.
    	 *
         * @interface IcatEntity
         */
		function IcatEntity(attributes, facility){
			_.merge(this, attributes);
			var that = this;
			var icat = facility.icat();
			var facilityName = facility.config().name;

			this.facility = facility;

			if(this.investigationInstruments && this.investigationInstruments.length > 0){
				this.firstInstrumentName = this.investigationInstruments[0].instrument.fullName;
				this.instrumentNames = _.map(this.investigationInstruments, function(investigationInstrument){
					return investigationInstrument.instrument.fullName;
				}).join("\n");
			}

			if(this.entityType == 'investigation'){
				this.getDatasetCount = helpers.overload({
					/**
					 *	Returns the total number of datasets for this investigation. Only applies to investigations. 
					 * 
					 * @method
					 * @name IcatEntity#getDatasetCount
					 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
					 * @return {Promise<number>} the deferred number of datasets in this investigation
					 */
					'object': function(options){
						var that = this;
						options.lowPriority = true;

						var query = "select count(dataset) from Dataset dataset, dataset.investigation as investigation where investigation.id = ?";
			
						var key = 'getDatasetCount:' + this.entityType + ":" + this.id;
            			return icat.cache().getPromise(key, function(){ return icat.query([query, that.id], options); }).then(function(response){
							/**
							 * The total number of datasets for this investigation. Only applies to investigations and only gets set after calling <code>getDatasetCount()</code>.
							 * 
							 * @name  IcatEntity#datasetCount
							 * @type {number}
							 */
							that.datasetCount = response[0];
							return that.datasetCount;
						});
					},

					/**
					 *	Returns the total number of datasets for this investigation. Only applies to investigations. 
					 * 
					 * @method
					 * @name IcatEntity#getDatasetCount
					 * @param  {Promise} timeout if resolved will cancel the request
					 * @return {Promise<number>} the deferred number of datasets in this investigation
					 */
					'promise': function(timeout){
						return this.getDatasetCount({timeout: timeout});
					},

					/**
					 *	Returns the total number of datasets for this investigation. Only applies to investigations. 
					 * 
					 * @method
					 * @name IcatEntity#getDatasetCount
					 * @return {Promise<number>} the deferred number of datasets in this investigation
					 */
					'': function(){
						return this.getDatasetCount({});
					}
				});
			}

			if(this.entityType.match(/^(investigation|dataset)$/)){
				this.getSize = helpers.overload({
					/**
					 *	Returns the total size of an investigation or dataset. Only applies to investigations and datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getSize
					 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
					 * @return {Promise<number>} the deferred total size of this investigation or dataset
					 */
					'object': function(options){
						var that = this;
						this.isGettingSize = true;
						return icat.getSize(this.entityType, this.id, options).then(function(size){
							/**
							 * total size of an investigation or dataset. Only applies to investigations and datasets, and only gets set after calling <code>getSize()</code>.
							 * 
							 * @name  IcatEntity#size
							 * @type {number}
							 */
							that.size = size;
							that.isGettingSize = false;
							return size;
						});
					},

					/**
					 *	Returns the total size of an investigation or dataset. Only applies to investigations and datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getSize
					 * @param  {Promise} timeout if resolved will cancel the request
					 * @return {Promise<number>} the deferred total size of this investigation or dataset
					 */
					'promise': function(timeout){
						return this.getSize({timeout: timeout});
					},

					/**
					 *	Returns the total size of an investigation or dataset. Only applies to investigations and datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getSize
					 * @return {Promise<number>} the deferred total size of this investigation or dataset
					 */
					'': function(){
						return this.getSize({});
					}
				});

				this.getDatafileCount = helpers.overload({
					/**
					 *	Returns the total number of datafiles for this investigation or dataset. Only applies to investigations or datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getDatafileCount
					 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
					 * @return {Promise<number>} the deferred number of datafiles in this investigation or dataset
					 */
					'object': function(options){
						var that = this;
						options.lowPriority = true;

						var query;
						if(this.entityType == 'investigation'){
							query = "select count(datafile) from Datafile datafile, datafile.dataset as dataset, dataset.investigation as investigation where investigation.id = ?";
						} else {
							query = "select count(datafile) from Datafile datafile, datafile.dataset as dataset where dataset.id = ?";
						}

						var key = 'getDatafileCount:' + this.entityType + ":" + this.id;
            			return icat.cache().getPromise(key, function(){ return icat.query([query, that.id], options); }).then(function(response){
							/**
							 * total number of datafiles for this investigation or dataset. Only applies to investigations or datasets, and only gets set after calling <code>getDatfileCount()</code>.
							 * 
							 * @name  IcatEntity#datasetCount
							 * @type {number}
							 */
							that.datafileCount = response[0];
							return that.datafileCount;
						});
					},

					/**
					 *	Returns the total number of datafiles for this investigation or dataset. Only applies to investigations or datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getDatafileCount
					 * @param  {Promise} timeout if resolved will cancel the request
					 * @return {Promise<number>} the deferred number of datafiles in this investigation or dataset
					 */
					'promise': function(timeout){
						return this.getDatafileCount({timeout: timeout});
					},

					/**
					 *	Returns the total number of datafiles for this investigation or dataset. Only applies to investigations or datasets. 
					 * 
					 * @method
					 * @name IcatEntity#getDatafileCount
					 * @return {Promise<number>} the deferred number of datafiles in this investigation or dataset
					 */
					'': function(){
						return this.getDatafileCount({});
					}
				});

			}

			if(this.entityType == 'datafile'){
				this.size = this.fileSize;
			}

			if(this.entityType.match(/^(investigation|dataset|datafile)$/)){
				this.getStatus = helpers.overload({
					'object': function(options){
						var that = this;
						return facility.ids().getStatus(this.entityType, this.id, options).then(function(status){
							that.status = status;
							return status;
						});
					},
					'promise': function(timeout){
						return this.getStatus({timeout: timeout});
					},
					'': function(){
						return this.getStatus({});
					}
				});
			}

			var parentFunctions = {
				datafile: function(datafile, options){
					var defered = $q.defer();
					var dataset = datafile.dataset;
					if(dataset){
						defered.resolve(_.merge(dataset, {entityType: 'dataset'}));
					} else {
						icat.query([
							'select dataset from Dataset dataset',
							', dataset.datafiles datafile',
							'where datafile.id = ?', datafile.id
						], options).then(function(datasets){
							datafile.dataset = datasets[0];
							defered.resolve(dataset[0]);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise
				},
				dataset: function(dataset, options){
					var defered = $q.defer();
					var investigation = dataset.investigation;
					if(investigation){
						defered.resolve(_.merge(investigation, {entityType: 'investigation'}));
					} else {
						icat.query([
							'select investigation from Investigation investigation',
							', investigation.datasets dataset',
							'where dataset.id = ?', dataset.id
						], options).then(function(investigations){
							dataset.investigation = investigations[0];
							defered.resolve(investigations[0]);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise
				},
				investigation: function(investigation, options){
					var defered = $q.defer();
					var facilityCycle = investigation.facilityCycle;
					if(facilityCycle){
						defered.resolve(facilityCycle);
					} else {
						icat.query([
							'select facilityCycle from FacilityCycle facilityCycle',
							', facilityCycle.facility facility,',
							'facility.investigations investigation',
							'where facility.id = ?', facility.config().id,
							'and investigation.id = ?', investigation.id,
							'and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate'
						], options).then(function(facilityCycles){
							investigation.facilityCycle = facilityCycles[0];
							defered.resolve(facilityCycles[0]);
						}, function(response){
							defered.reject(response);
						});
					}
					return defered.promise;
				},
				facilityCycle: function(facilityCycle, childEntity, options){
					if(!_.includes(['investigation', 'dataset', 'datafile'], childEntity.entityType)){
						return $q.resolve(null);
					}
					return childEntity.thisOrParent('investigation').then(function(investigation){
						var defered = $q.defer();
						var instrument = facilityCycle.instrument;
						if(instrument){
							defered.resolve(instrument);
						} else {
							icat.query([
								'select instrument from Instrument instrument',
								', instrument.investigationInstruments investigationInstrument,',
								'investigationInstrument.investigation investigation,',
								'instrument.facility facility',
								'where facility.id = ?', facility.config().id,
								'and investigation.id = ?', investigation.id,
							], options).then(function(instruments){
								facilityCycle.instrument = instruments[0];
								defered.resolve(instruments[0]);
							}, function(response){
								defered.reject(response);
							});
						}
						return defered.promise;
					});
				}
	
			};


			var parent; 
			this.parent = helpers.overload({
				'string, object, object': function(entityType, childEntity, options){
					return this.parent(childEntity, options).then(function(entity){
						if(!entity || entity.entityType == entityType){
							return entity;
						} else {
							return entity.parent(entityType, childEntity, options);
						}
					});
				},
				'string, object, promise': function(entityType, childEntity, timeout){
					return this.parent(entityType, childEntity, {timeout: timeout});	
				},
				'string, object': function(entityType, childEntity){
					return this.parent(entityType, childEntity, {});
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
					return this.parent(childEntity, {});	
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

			this.thisOrParent = function(entityType){
				if(this.entityType == entityType){
					return $q.resolve(this);
				} else {
					return this.parent(entityType, this);
				}
			};
				

			this.ancestors = function(){
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

			this.thisAndAncestors = function(){
				var that = this;
				return this.ancestors().then(function(ancestors){
					return _.flatten([that, ancestors]);
				});
			};

			this.stateParams = function(){
				if($state.current.name.match(/^home\.browse\.facility\./)){
					var out = _.clone($state.params);
					delete out.uiGridState;
					out[helpers.uncapitalize(this.entityType) + "Id"] = this.id;
					return $q.resolve(out);
				} else {
					return this.thisAndAncestors().then(function(thisAndAncestors){
						var out = {};
						_.each(thisAndAncestors, function(entity){
							out[entity.entityType + "Id"] = entity.id;
							if(entity.entityType == 'investigation') out['proposalId'] = entity.name;
						});
						return _.merge(out, {facilityName: facilityName});
					});
				}
			};

			this.browse = function(){
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

			if(this.dataset){
				this.dataset.entityType = "dataset";
				this.dataset = tcIcatEntity.create(this.dataset, facility);
			}
			if(this.investigation){
				this.investigation.entityType = "investigation";
				this.investigation = tcIcatEntity.create(this.investigation, facility);
			}

			this.addToCart = helpers.overload({
				'object': function(options){
					return facility.user().cart(options).then().then(function(_cart){
						return facility.user().addCartItem(that.entityType.toLowerCase(), that.id, options).then(function(cart){
							if(cart.cartItems.length > _cart.cartItems.length){
								$rootScope.$broadcast('cart:add');
							}
							return cart;
						});
					});
					
				},
				'promise': function(timeout){
					return this.addToCart({timeout: timeout});
				},
				'': function(){
					return this.addToCart({});
				}
			});

			this.deleteFromCart = helpers.overload({
				'object': function(options){
					$rootScope.$broadcast('cart:delete');
					return facility.user().deleteCartItem(this.entityType.toLowerCase(), this.id, options);
				},
				'promise': function(timeout){
					return this.deleteFromCart({timeout: timeout});
				},
				'': function(){
					return this.deleteFromCart({});
				}
			});

			var findCache = {};

			this.find = function(expression){
				if(findCache[expression]) return findCache[expression];
				if(expression == '') return [];

				var out = [];
				var matches;
				var variable;
				var predicate;
				var entityField;

				if(matches = expression.match(/^([^\[]+)\[(.*)\]\.([^\.]+)$/)){
					variable = matches[1];
					predicate = matches[2];
					entityField = matches[3];
				} else if(matches = expression.match(/^([^\[]+)\[(.*)\]$/)){
					variable = matches[1];
					predicate = matches[2];
				} else if(matches = expression.match(/^([^\.]+)\.([^\.]+)$/)){
					variable = matches[1];
					entityField = matches[2];
				} else {
					entityField = expression;
				}

				

				var that = this;
				var variablePath = [];

				if(variable !== undefined && icatSchema.variableEntityTypes[variable] != this.entityType){
					var variablePaths = icatSchema.entityTypes[this.entityType].variablePaths;
					if(!variablePaths){
						throw "Unknown expression for find(): " + expression;
					}

					variablePath = _.clone(variablePaths[variable]);
					if(!variablePath){
						throw "Unknown expression for find(): " + expression;
					}
				}

				traverse(this);
				function traverse(entity){
					if(variablePath.length == 0){
						if(!predicate || eval(predicate)){
							if(entityField){
								var value = entity[entityField];
								if(value !== undefined){
									out.push(value);
								}
							} else {

								out.push(entity);
							}
						}
					} else {
						var fieldName = variablePath.shift();
						entity = entity[fieldName];
						if(entity instanceof Array){
							_.each(entity, function(entity){
								traverse(entity);
							});
						} else {
							traverse(entity);
						}
						variablePath.unshift(fieldName);
					}
				}

				out =_.uniq(out);

				findCache[expression] = out;

				return out;
			};

			var children = {};
			_.each(icatSchema.entityTypes[this.entityType].variablePaths, function(path, variableName){
				var entityType = icatSchema.variableEntityTypes[variableName];
				if(path.length == 1){
					children[entityType] = path[0];
				}
			});

			_.each(children, function(name, entityType){
				if(that[name] instanceof Array){
					that[name] = _.map(that[name], function(child){
						child.entityType = entityType;
						return tcIcatEntity.create(child, facility);
					});
				} else if(typeof that[name] == 'object') {
					that[name].entityType = entityType;
					that[name] = tcIcatEntity.create(that[name], facility);
				}
			});

			_.each(plugins, function(plugin){
              if(plugin.extend && plugin.extend.entities && plugin.extend.entities[that.entityType]){
                $injector.invoke(plugin.extend.entities[that.entityType], that);
              }
            });

		}

	});

})();

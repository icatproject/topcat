

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
					/**
					 *	Returns whether or not this investigation, dataset or datafile is available or not. Only applies to investigations, datasets or datafiles on a two tier IDS. 
					 * 
					 * @method
					 * @name IcatEntity#getStatus
					 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
					 * @return {Promise<string>} the deferred availability of this investigation, dataset or datafile. Can be either 'ONLINE', 'ARCHIVE' or 'RESTORING'
					 */
					'object': function(options){
						var that = this;
						return facility.ids().getStatus(this.entityType, this.id, options).then(function(status){
							that.status = status;
							return status;
						});
					},

					/**
					 *	Returns whether or not this investigation, dataset or datafile is available or not. Only applies to investigations, datasets or datafiles on a two tier IDS. 
					 * 
					 * @method
					 * @name IcatEntity#getStatus
					 * @param  {Promise} timeout if resolved will cancel the request
					 * @return {Promise<string>} the deferred availability of this investigation, dataset or datafile. Can be either 'ONLINE', 'ARCHIVE' or 'RESTORING'
					 */
					'promise': function(timeout){
						return this.getStatus({timeout: timeout});
					},

					/**
					 *	Returns whether or not this investigation, dataset or datafile is available or not. Only applies to investigations, datasets or datafiles on a two tier IDS. 
					 * 
					 * @method
					 * @name IcatEntity#getStatus
					 * @return {Promise<string>} the deferred availability of this investigation, dataset or datafile. Can be either 'ONLINE', 'ARCHIVE' or 'RESTORING'
					 */
					'': function(){
						return this.getStatus({});
					}
				});
			}

			this.realHierarchy = function(){
				var out = _.clone(facility.config().hierarchy);
				var position = _.indexOf(out, 'proposal');
				if(position >= -1){
					if(out[position + 1] == 'investigation'){
						out.splice(position, 1);
					} else {
						out[position] = 'investigation';
					}
				}
				return out;
			};

			this.parent = function(){
				var hierarchy = this.realHierarchy();
				var positionInHierarchy = _.indexOf(hierarchy, this.entityType);
				var parentEntityType = hierarchy[positionInHierarchy - 1];
				if(parentEntityType != 'facility'){
					var queryBuilder = icat.queryBuilder(parentEntityType);
					queryBuilder.where(["?.id = ?", this.entityType.safe(), this.id]);
					return queryBuilder.run().then(function(entities){
						return entities[0];
					});
				} else {
					return $q.resolve(null);
				}
			};

			this.ancestors = function(){
				var defered = $q.defer();
				var out = [];

				function parent(entity){
					entity.parent().then(function(entity){
						if(entity){
							out.push(entity);
							parent(entity);
						} else {
							defered.resolve(out);
						}
					});
				}
				parent(this);

				return defered.promise;
			};

			this.stateParams = function(){
				var that = this;
				return this.ancestors().then(function(ancestors){
					var out = [];
					_.each(ancestors.concat([that]), function(entity){
						if(entity.entityType == 'investigation'){
							out["proposalId"] = entity.name;
						}
						out[entity.entityType + "Id"] = entity.id;
					});
					return _.merge(out, {facilityName: facilityName});
				});
			};

			/**
			 * Redirects the user the to the relevant position in the "Browse" hierarchy.
			 * 
			 * @method
			 * @name IcatEntity#browse
			 */
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
				/**
				 *	Adds this entity to the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#addToCart
				 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
				 * @return {Promise<Cart>} the updated cart
				 */
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

				/**
				 *	Adds this entity to the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#addToCart
				 * @param  {Promise} timeout if resolved will cancel the request
				 * @return {Promise<Cart>} the updated cart
				 */
				'promise': function(timeout){
					return this.addToCart({timeout: timeout});
				},

				/**
				 *	Adds this entity to the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#addToCart
				 * @return {Promise<Cart>} the updated cart
				 */
				'': function(){
					return this.addToCart({});
				}
			});

			this.deleteFromCart = helpers.overload({
				/**
				 *	Deletes this entity from the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#deleteFromCart
				 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
				 * @return {Promise<Cart>} the updated cart
				 */
				'object': function(options){
					$rootScope.$broadcast('cart:delete');
					return facility.user().deleteCartItem(this.entityType.toLowerCase(), this.id, options);
				},

				/**
				 *	Deletes this entity from the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#addToCart
				 * @param  {Promise} timeout if resolved will cancel the request
				 * @return {Promise<Cart>} the updated cart
				 */
				'promise': function(timeout){
					return this.deleteFromCart({timeout: timeout});
				},

				/**
				 *	Deletes this entity from the Cart and returns the updated Cart. Only applies to investigations, datasets or datafiles. 
				 * 
				 * @method
				 * @name IcatEntity#addToCart
				 * @return {Promise<Cart>} the updated cart
				 */
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

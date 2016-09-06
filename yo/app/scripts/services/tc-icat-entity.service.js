

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIcatEntity', function($http, $q, $rootScope, $state, helpers, icatSchema){
    	var tcIcatEntity = this;

    	this.create = function(attributes, facility){
    		return new IcatEntity(attributes, facility);
    	};

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

			if(this.entityType.match(/^(investigation|dataset)$/)){
				this.getSize = helpers.overload({
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
			}

			if(this.entityType == 'datafile'){
				this.size = this.fileSize;

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
						icat.entity('dataset', [
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
				dataset: function(dataset, options){
					var defered = $q.defer();
					var investigation = dataset.investigation;
					if(investigation){
						defered.resolve(_.merge(investigation, {entityType: 'investigation'}));
					} else {
						icat.entity('investigation', [
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
				investigation: function(investigation, options){
					var defered = $q.defer();
					var facilityCycle = investigation.facilityCycle;
					if(facilityCycle){
						defered.resolve(facilityCycle);
					} else {
						icat.entity('facilityCycle', [
							', facilityCycle.facility facility,',
							'facility.investigations investigation',
							'where facility.id = ?', facility.config().id,
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
				facilityCycle: function(facilityCycle, childEntity, options){
					if(!_.includes(['investigation', 'dataset', 'datafile'], childEntity.entityType)){
						return helpers.resolvedPromise(null);
					}
					return childEntity.thisOrParent('investigation').then(function(investigation){
						var defered = $q.defer();
						var instrument = facilityCycle.instrument;
						if(instrument){
							defered.resolve(instrument);
						} else {
							icat.entity('instrument', [
								', instrument.investigationInstruments investigationInstrument,',
								'investigationInstrument.investigation investigation,',
								'instrument.facility facility',
								'where facility.id = ?', facility.config().id,
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
					return helpers.resolvedPromise(this);
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
					return helpers.resolvedPromise(out);
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

			this.find = function(expression){
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

		}

	});

})();



(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('icatEntityFactory', function($http, $q, $rootScope, $state, helpers){
    	var icatEntityFactory = this;

    	this.create = function(attributes, facility){
    		return new IcatEntity(attributes, facility);
    	};

		function IcatEntity(attributes, facility){
			_.merge(this, attributes);
			var that = this;
			var icat = facility.icat();
			var facilityName = facility.config().facilityName;

			if(this.investigationInstruments && this.investigationInstruments.length > 0){
				this.firstInstrumentName = this.investigationInstruments[0].instrument.fullName;
				this.instrumentNames = _.map(this.investigationInstruments, function(investigationInstrument){
					return investigationInstrument.instrument.fullName;
				}).join("\n");
			}

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
						return helpers.resolvedPromise(null);
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
					return this.parent(entityType);
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
				return this.thisAndAncestors().then(function(thisAndAncestors){
					var out = {};
					_.each(thisAndAncestors, function(entity){
						out[helpers.uncapitalize(entity.entityType) + "Id"] = entity.id;
						if(entity.entityType == 'Investigation') out['proposalId'] = entity.name;
					});
					return _.merge(out, {facilityName: facilityName});
				});

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
				this.dataset.entityType = "Dataset";
				this.dataset = icatEntityFactory.create(this.dataset, facility);
			}
			if(this.investigation){
				this.investigation.entityType = "Investigation";
				this.investigation = icatEntityFactory.create(this.investigation, facility);
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

		}

	});

})();

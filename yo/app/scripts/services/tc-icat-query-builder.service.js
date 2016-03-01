

(function() {
    'use strict';

    var app = angular.module('angularApp');

    var paths = {
    	facilityCycle: {
    		facility: 'facilityCycle.facility',
    		investigation: 'facilityCycle.facility.investigations',
    		dataset: 'facilityCycle.facility.investigations.datasets',
    		datafile: 'facilityCycle.facility.investigations.datafiles',
    		investigationParameter: 'facilityCycle.facility.investigations.parameters',
    		investigationParameterType: 'facilityCycle.facility.investigations.parameters.type',
    		datasetParameter: 'facilityCycle.facility.investigations.datasets.parameters',
    		datasetParameterType: 'facilityCycle.facility.investigations.datasets.parameters.type',
    		datafileParameter: 'facilityCycle.facility.investigations.datasets.datafiles.parameters',
    		datafileParameterType: 'facilityCycle.facility.investigations.datasets.datafiles.parameters.type',
    		investigationInstrument: 'facilityCycle.facility.instruments.investigationInstruments',
            instrument: 'facilityCycle.facility.instruments'
    	},
    	instrument: {
    		facility: 'instrument.facility',
    		investigation: 'instrument.investigationInstruments.investigation',
    		dataset: 'instrument.investigationInstruments.investigation.datasets',
    		datafile: 'instrument.investigationInstruments.investigation.datasets.datafiles',
    		investigationParameter: 'instrument.investigationInstruments.investigation.parameters',
    		investigationParameterType: 'instrument.investigationInstruments.investigation.parameters.type',
    		datasetParameter: 'instrument.investigationInstruments.investigation.dataset.parameters',
    		datasetParameterType: 'instrument.investigationInstruments.investigation.dataset.parameters.type',
    		datafileParameter: 'instrument.investigationInstruments.investigation.datasets.datafiles.parameters',
    		datafileParameterType: 'instrument.investigationInstruments.investigation.datasets.datafiles.parameters.type',
            investigationInstrument: 'investigation.investigationInstruments',
    		facilityCycle: 'instrument.facility.facilityCycles'
    	},
    	investigation: {
    		facility: 'investigation.facility',
    		dataset: 'investigation.datasets',
    		datafile: 'investigation.datasets.datafiles',
    		investigationParameter: 'investigation.parameters',
    		investigationParameterType: 'investigation.parameters.type',
    		datasetParameter: 'investigation.dataset.parameters',
    		datasetParameterType: 'investigation.dataset.parameters.type',
    		datafileParameter: 'investigation.datasets.datafiles.parameters',
    		datafileParameterType: 'investigation.datasets.datafiles.parameters.type',
    		facilityCycle: 'investigation.facility.facilityCycles',
            investigationInstrument: 'investigation.investigationInstruments',
    		instrument: 'investigation.investigationInstruments.instrument'
    	},
    	dataset: {
    		facility: 'dataset.investigation.facility',
    		investigation: 'dataset.investigation',
    		datafile: 'datasets.datafiles',
    		investigationParameter: 'dataset.investigation.parameters',
    		investigationParameterType: 'dataset.investigation.parameters.type',
    		datasetParameter: 'dataset.parameters',
    		datasetParameterType: 'dataset.parameters.type',
    		datafileParameter: 'dataset.datafiles.parameters',
    		datafileParameterType: 'dataset.datafiles.parameters.type',
    		facilityCycle: 'dataset.investigation.facility.facilityCycles',
            investigationInstrument: 'dataset.investigation.investigationInstruments',
    		instrument: 'dataset.investigation.investigationInstruments.instrument'
    	},
    	datafile: {
    		facility: 'datafile.dataset.investigation.facility',
    		investigation: 'datafile.dataset.investigation',
    		dataset: 'datafile.dataset',
    		investigationParameter: 'datafile.dataset.investigation.parameters',
    		investigationParameterType: 'datafile.dataset.investigation.parameters.type',
    		datasetParameter: 'datafile.dataset.parameters',
    		datasetParameterType: 'datafile.dataset.parameters.type',
    		datafileParameter: 'datafile.parameters',
    		datafileParameterType: 'datafile.parameters.type',
    		facilityCycle: 'datafile.dataset.investigation.facility.facilityCycles',
            investigationInstrument: 'datafile.dataset.investigation.investigationInstruments',
    		instrument: 'datafile.dataset.investigation.investigationInstruments.instrument'
    	}
    };

    var steps = {};
    _.each(paths, function(entityPaths, entityType){
        _.each(entityPaths, function(path, name){
            if(path.match(/^[^\.]+\.[^\.]+$/)){
                steps[path] = name;
            }
        });
    });

    app.service('tcIcatQueryBuilder', function(helpers){

    	this.create = function(icat, entityType){
    		return new IcatQueryBuilder(icat, entityType);
    	};

    	function IcatQueryBuilder(icat, entityType){
    		var facility = icat.facility();
    		var facilityName = facility.config().facilityName;
    		var tc = facility.tc();
    		var user = tc.user(facilityName);
    		var cart = user.cart();
    		var whereList = [];
            var orderByList = [];
    		var entityPaths = paths[entityType];
            var limitOffset;
            var limitCount;

    		this.where = function(where){
    			whereList.push(where);
    			return this;
    		};

            this.orderBy = function(orderBy){
                orderByList.push(orderBy);
                return this;
            };

            this.limit = function(arg1, arg2){
                if(arg2){
                    limitOffset = arg1;
                    limitCount = arg2;
                } else {
                    limitOffset = 0;
                    limitCount = arg1;
                }
                return this;
            };


    		this.build = function(){
    			var out = [];

    			out.push([
    				"select ?", entityType.safe(),
    				"from ? ?", helpers.capitalize(entityType).safe(), entityType.safe()
    			]);

    			var whereQuery = _.clone(whereList);
    			var whereQueryFragment = helpers.buildQuery(whereQuery);
    			var impliedPaths = {};
    			_.each(entityPaths, function(path, name){
    				if(whereQueryFragment.indexOf(name) >= 0){
    					impliedPaths[name] = path;
    				}
    			});

                _.each(orderByList, function(orderBy){
                    var name = orderBy.replace(/\.[^\.]+$/, '');
                    console.log(name);
                    if(name !=  entityType){
                        impliedPaths[name] = entityPaths[name];
                    }
                });


    			if(impliedPaths.facilityCycle || entityType == 'facilityCycle'){
    				whereQuery.push('and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate')
    				if(entityType != 'investigation'){
    					impliedPaths['investigation'] = entityPaths['investigation'];
    				}
    			}

                var impliedVars = {};
                _.each(impliedPaths, function(path, name){
                    var segments = path.split(/\./);
                    var currentEntity = entityType;
                    for(var i = 0; i < segments.length - 1; i++){
                        var pair = segments[i] + '.' + segments[i + 1];
                        impliedVars[currentEntity + '.' + segments[i + 1]] = steps[pair];
                        currentEntity = steps[pair];
                    }
                });

    			var joins = [];
    			_.each(impliedVars, function(name, pair){
    				joins.push([", ? as ?", pair.safe(), name.safe()]);
    			});

    			if(joins.length > 0){
    				out.push(joins);
    			}

    			if(whereQuery.length > 0){
    				out.push(['where', whereQuery]);
    			}

                if(orderByList.length > 0){
                    out.push(['order by', orderByList.join(', ')]);
                }

                if(limitCount){
                    out.push(['limit ?, ?', limitOffset, limitCount]);
                }

    			return helpers.buildQuery(out);
    		}

    		this.run = helpers.overload({
    			'object': function(options){
    				return icat.query([this.build()], options);
    			},
    			'promise': function(timeout){
    				return this.run({timeout: timeout});
    			},
    			'': function(){
    				return this.run({});
    			}
    		});
    	}

	});

})();

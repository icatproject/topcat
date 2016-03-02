

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcIcatQueryBuilder', function($q, helpers, icatEntityPaths){

        var steps = {};
        _.each(icatEntityPaths, function(entityPaths, entityType){
            _.each(entityPaths, function(path, name){
                var matches;
                if(matches = path.match(/^[^\.]+\.([^\.]+)$/)){
                    steps[entityType + '.' + matches[1]] = name;
                }
            });
        });

    	this.create = function(icat, entityType){
    		return new IcatQueryBuilder(icat, entityType);
    	};

    	function IcatQueryBuilder(icat, entityType){
            var that = this;
    		var facility = icat.facility();
    		var facilityName = facility.config().facilityName;
    		var tc = facility.tc();
    		var user = tc.user(facilityName);
    		var cart = user.cart();
    		var whereList = [];
            var orderByList = [];
            var includeList = [];
    		var entityPaths = icatEntityPaths[entityType];
            var limitOffset;
            var limitCount;

    		this.where = function(where){
    			whereList.push(where);
    			return this;
    		};

            this.orderBy = function(orderBy, direction){
                if(!direction) direction = 'asc';
                orderByList.push(orderBy + ' ' + direction);
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

            this.include = function(include){
                includeList.push(entityPaths[include] || include);
                return this;
            };

    		this.build = function(isCount, investigationName){
    			var out = [];

                if(entityType == 'proposal'){
                    if(investigationName){
                        if(isCount){
                            out.push("select count(investigation)");
                        } else {
                            out.push(["select investigation"]);
                        }
                    } else {
                        if(isCount){
                            out.push("select count(distinct investigation)");
                        } else {
                            out.push(["select distinct investigation"]);
                        }
                    }

                    out.push([
                        "from Investigation investigation"
                    ]);
                } else {
                    if(isCount){
                        out.push(["select count(?)", entityType.safe()]);
                    } else {
                        out.push(["select ?", entityType.safe()]);
                    }

        			out.push([
        				"from ? ?", helpers.capitalize(entityType).safe(), entityType.safe()
        			]);
                }

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
                    if(name !=  entityType){
                        impliedPaths[name] = entityPaths[name];
                    }
                });


    			if(impliedPaths.facilityCycle || entityType == 'facilityCycle'){
    				whereQuery.push('and investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate')
    				if(entityType != 'investigation' && entityType != 'proposal'){
    					impliedPaths['investigation'] = entityPaths['investigation'];
    				}
    			}

                if(investigationName){
                    whereQuery.push(["investigation.name = ?", investigationName]);
                }

                if(entityType == 'proposal'){
                    var alteredImpliedPaths = {}
                    _.each(impliedPaths, function(value, name){
                        alteredImpliedPaths[name] = value.replace(/^proposal\.investigations/, 'investigation');
                    });
                    impliedPaths = alteredImpliedPaths;
                }

                var impliedVars = {};
                _.each(impliedPaths, function(path, name){
                    var segments = path.split(/\./);
                    var currentEntity = entityType;
                    if(currentEntity == 'proposal') currentEntity = 'investigation';
                    for(var i = 0; i < segments.length - 1; i++){
                        var pair = currentEntity + '.' + segments[i + 1];
                        if(!steps[pair]){
                            throw "could not work out step " + pair + " for " + currentEntity + '.' + segments[i + 1];
                        }
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

                if(limitCount && !investigationName){
                    out.push(['limit ?, ?', limitOffset, limitCount]);
                }

                if(!isCount && includeList.length > 0 && !investigationName){
                    out.push(['include ', includeList.join(', ')]);
                }

    			return helpers.buildQuery(out);
    		}

    		this.run = helpers.overload({
    			'object': function(options){
                    var out = icat.query([this.build()], options);
                    if(entityType == 'proposal'){
                        var defered = $q.defer();
                        out.then(function(names){
                            var promises = [];
                            var proposals = [];

                            _.each(names, function(name){
                                name = name[0];
                                promises.push(icat.query([that.build(false, name)], options).then(function(investigations){
                                    var proposal = {};
                                    proposal.entityType = "Proposal";
                                    proposal.id = investigations[0].name;
                                    proposal.name = investigations[0].name;
                                    proposal.investigations = investigations;
                                    proposal.find = investigations[0].find;
                                    proposals.push(proposal);
                                }));
                            });
                            
                            $q.all(promises).then(function(){
                                defered.resolve(proposals);
                            });
                            
                        });
                        return defered.promise;
                    } else {
    				    return out;
                    }
    			},
    			'promise': function(timeout){
    				return this.run({timeout: timeout});
    			},
    			'': function(){
    				return this.run({});
    			}
    		});

            this.count = helpers.overload({
                'object': function(options){
                    return icat.query([this.build(true)], options).then(function(results){
                        return results[0];
                    });
                },
                'promise': function(timeout){
                    return this.count({timeout: timeout});
                },
                '': function(){
                    return this.count({});
                }
            });

    	}

	});

})();

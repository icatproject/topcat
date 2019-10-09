

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcIcatQueryBuilder', function($q, helpers, icatSchema, tcIcatEntity){

        var steps = {};
        _.each(icatSchema.entityTypes, function(entitySchema, entityType){
            _.each(entitySchema.variablePaths, function(path, variableName){
                var matches;
                if(path.length == 1){
                    steps[entityType + '.' + path[0]] = variableName;
                }
            });
        });

    	this.create = function(icat, entityType){
    		return new IcatQueryBuilder(icat, entityType);
    	};

        /**
         * @interface IcatQueryBuilder
         *
         * Allows incrementally builds a JPQL query and magically works out the joins.
         */
    	function IcatQueryBuilder(icat, entityType){
            var that = this;
    		var facility = icat.facility();
    		var facilityName = facility.config().name;
    		var tc = facility.tc();
    		var user = tc.user(facilityName);
    		var cart = user.cart();
            var select = entityType;
    		var whereList = [];
            var orderByList = [];
            var includeList = [];
    		var variablePaths = icatSchema.entityTypes[entityType].variablePaths;
            var limitOffset;
            var limitCount;

            /**
             * Adds in a where clause.
             *
             * @method
             * @name  IcatQueryBuilder#where
             * @param  {array} expression
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             * 
             * @example
             *
             * var investigationId = 3;
             * 
             * tc.icat('LILS').queryBuilder('datafile').where(["investigation.id = ?", investigationId]).run().log()
             */
            /**
             * Adds in a where clause.
             *
             * @method
             * @name  IcatQueryBuilder#where
             * @param  {string} expression
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             * 
             * @example
             * 
             * tc.icat('LILS').queryBuilder('datafile').where("investigation.id = 3").run().log()
             */
    		this.where = function(expression){
                if(whereList.length > 0) whereList.push('and');
    			whereList.push(expression);
    			return this;
    		};

            /**
             * Orders the results to be returned by a field name.
             * 
             * @method
             * @name  IcatQueryBuilder#orderBy
             * @param  {string} fieldName the field name which you wish to order by 
             * @param  {string} direction can be either 'asc' (ascending) or 'desc' (descending)
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             */
            /**
             * Orders the results to be returned by a field name in ascending order.
             * 
             * @method
             * @name  IcatQueryBuilder#orderBy
             * @param  {string} fieldName the field name which you wish to order by 
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             */
            this.orderBy = function(fieldName, direction){
                if(!direction) direction = 'asc';
                orderByList.push(fieldName + ' ' + direction);
                return this;
            };
            
            /**
             * Adds in a limit clause to the JPQL expression
             *
             * @method
             * @name  IcatQueryBuilder#limit
             * @param  {string} offset the number of entities you want to skip
             * @param  {string} count the max number entities you want returned
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             */
            /**
             * Adds in a limit clause to the JPQL expression
             *
             * @method
             * @name  IcatQueryBuilder#limit
             * @param  {string} count the max number entities you want returned
             * @return {IcatQueryBuilder} the current IcatQueryBuilder object to allow for chaining
             */
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

            this.include = function(variableName){
                var variablePath = variablePaths[variableName] || [];
                var path = _.flatten([[entityType], variablePath]).join('.');
                if(!_.contains(includeList, path) && entityType != path && !(entityType == 'proposal' && variableName == 'investigation')){
                    includeList.push(path);
                }
                return this;
            };

    		this.build = function(functionName, fieldName, investigationName){
    			var out = [];

                var distinct = functionName == 'count' ? 'distinct ' : '';

                if(entityType == 'proposal'){
                    if(investigationName){
                        if(functionName){
                            if(fieldName){
                                out.push(["select ?(?investigation.?)", functionName.safe(), distinct.safe(), fieldName.safe()]);
                            } else {
                                out.push(["select ?(?investigation)", functionName.safe(), distinct.safe()]);
                            }
                        } else {
                            out.push(["select investigation"]);
                        }
                    } else {
                        if(functionName){
                            if(fieldName){
                                out.push(["select ?(distinct investigation.?)", functionName.safe(), fieldName.safe()]);
                            } else {
                                out.push(["select ?(distinct investigation.name)", functionName.safe()]);
                            }
                        } else {
                            out.push(["select distinct investigation.name"]);
                        }
                    }

                    out.push([
                        "from Investigation investigation"
                    ]);
                } else {
                    if(functionName){
                        if(fieldName){
                            out.push(["select ?(??.?)", functionName.safe(), distinct.safe(), entityType.safe(), fieldName.safe()]);
                        } else {
                            out.push(["select ?(??)", functionName.safe(), distinct.safe(), entityType.safe()]);
                        }
                    } else {
                        out.push(["select distinct ?", entityType.safe()]);
                    }

        			out.push([
        				"from ? ?", helpers.capitalize(entityType).safe(), entityType.safe()
        			]);
                }

    			var whereQuery = _.clone(whereList);
    			var whereQueryFragment = helpers.buildQuery(whereQuery);
    			var impliedPaths = {};

    			_.each(variablePaths, function(path, name){
    				if(name != entityType && whereQueryFragment.indexOf(name) >= 0){
    					impliedPaths[name] = path;
    				}
    			});

                _.each(orderByList, function(orderBy){
                    var name = orderBy.replace(/\.[^\.]+$/, '');
                    if(name !=  entityType){
                        impliedPaths[name] = variablePaths[name];
                    }
                });

    			if(impliedPaths.facilityCycle || entityType == 'facilityCycle'){
                    if(whereQuery.length > 0) whereQuery.push('and');
    				whereQuery.push('investigation.startDate BETWEEN facilityCycle.startDate AND facilityCycle.endDate')
    				if(entityType != 'investigation' && entityType != 'proposal'){
    					impliedPaths['investigation'] = variablePaths['investigation'];
    				}
    			}

                if(investigationName){
                    if(whereQuery.length > 0) whereQuery.push('and');
                    whereQuery.push(["investigation.name = ?", investigationName]);
                }

                if(entityType == 'proposal'){
                    var alteredImpliedPaths = {}
                    _.each(impliedPaths, function(path, name){
                        path = path.slice(1);
                        alteredImpliedPaths[name] = path;
                    });
                    impliedPaths = alteredImpliedPaths;
                }

                var impliedVars = this.impliedPathsToImpliedSteps(impliedPaths);

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

                // Always order by ID to force an order on rows that are otherwise sort-identical;
                // This should avoid pagination duplication problems - see issue #453
                // TODO: should we do this even if no other sorting has been specified?
                
    			var idField = entityType + '.id';
    			if(entityType == 'proposal'){
    			    idField = 'investigation.id';
    			}

    			if(orderByList.length > 0){
                    out.push(['ORDER BY', orderByList.join(', '), ',', idField, 'asc']);
                }

                if(!functionName && limitCount && !investigationName){
                    out.push(['limit ?, ?', limitOffset, limitCount]);
                }

                if(!functionName && includeList.length > 0 && !investigationName){
                    out.push(['include', includeList.join(', ')]);
                }

    			return helpers.buildQuery(out);
    		}

            this.impliedPathsToImpliedSteps = function(impliedPaths){
                var out = {};
                _.each(impliedPaths, function(path, variableName){
                    var currentVariableName = entityType;
                    var currentEntityType = entityType;
                    _.each(path, function(relationship){
                        if(currentEntityType == 'proposal') currentEntityType = 'investigation';
                        if(currentVariableName == 'proposal') currentVariableName = 'investigation';
                        var stepPair = currentVariableName + "." + relationship;
                        var entityPair = currentEntityType + "." + relationship;
                        currentVariableName = steps[entityPair];
                        out[stepPair] = currentVariableName;
                        currentEntityType = icatSchema['variableEntityTypes'][currentVariableName];
                    });
                });
                return out;
            };

    		this.run = helpers.overload({
                /**
                 * Runs the query on the Icat server.
                 * 
                 * @method
                 * @name IcatQueryBuilder#run
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<IcatEntity[]>} a deferred array of icat entities
                 */
    			'object': function(options){
                    var out = icat.query([this.build()], options);
                    if(entityType == 'proposal'){
                        var defered = $q.defer();
                        out.then(function(names){
                            var promises = [];
                            var proposals = [];

                            _.each(names, function(name){
                                promises.push(icat.query([that.build(null, null, name)], options).then(function(investigations){
                                    var proposal = {};
                                    proposal.entityType = "proposal";
                                    proposal.id = investigations[0].name;
                                    proposal.name = investigations[0].name;
                                    proposal.investigations = investigations;
                                    proposal = tcIcatEntity.create(proposal, icat.facility());
                                    proposals.push(proposal);
                                }));
                            });
                            
                            $q.all(promises).then(function(){
                                var proposalIndex = {};
                                _.each(proposals, function(proposal){
                                    proposalIndex[proposal.name] = proposal; 
                                });
                                proposals = _.map(names, function(name){
                                    return proposalIndex[name]
                                });
                                defered.resolve(proposals);
                            });
                            
                        });
                        return defered.promise;
                    } else {
    				    return out;
                    }
    			},

                /**
                 * Runs the query on the Icat server.
                 * 
                 * @method
                 * @name IcatQueryBuilder#run
                 * @param  {Promise} timeout if resolved the request will be cancelled
                 * @return {Promise<IcatEntity[]>} a deferred array of icat entities
                 */
    			'promise': function(timeout){
    				return this.run({timeout: timeout});
    			},

                /**
                 * Runs the query on the Icat server.
                 * 
                 * @method
                 * @name IcatQueryBuilder#run
                 * @return {Promise<IcatEntity[]>} a deferred array of icat entities
                 */
    			'': function(){
    				return this.run({});
    			}
    		});

            this.count = helpers.overload({
                /**
                 * Counts the number of results that will be returned.
                 * 
                 * @method
                 * @name IcatQueryBuilder#count
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<number>} the number results to be returned (deferred)
                 */
                'object': function(options){
                    return icat.query([this.build('count')], options).then(function(results){
                        return results[0];
                    });
                },

                /**
                 * Counts the number of results that will be returned.
                 * 
                 * @method
                 * @name IcatQueryBuilder#count
                 * @param  {Promise} timeout if resolved the request will be cancelled
                 * @return {Promise<number>} the number results to be returned (deferred)
                 */
                'promise': function(timeout){
                    return this.count({timeout: timeout});
                },

                /**
                 * Counts the number of results that will be returned.
                 * 
                 * @method
                 * @name IcatQueryBuilder#count
                 * @return {Promise<number>} the number results to be returned (deferred)
                 */
                '': function(){
                    return this.count({});
                }
            });

            this.min = helpers.overload({
                'string, object': function(fieldName, options){
                    return icat.query([this.build('min', fieldName)], options).then(function(results){
                        return results[0];
                    });
                },
                'string, promise': function(fieldName, timeout){
                    return this.min(fieldName, {timeout: timeout});
                },
                'string': function(fieldName){
                    return this.min(fieldName, {});
                }
            });

            this.max = helpers.overload({
                'string, object': function(fieldName, options){
                    return icat.query([this.build('max', fieldName)], options).then(function(results){
                        return results[0];
                    });
                },
                'string, promise': function(fieldName, timeout){
                    return this.max(fieldName, {timeout: timeout});
                },
                'string': function(fieldName){
                    return this.max(fieldName, {});
                }
            });


    	}

	});

})();



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
            var leftJoinList = [];
            var variablePaths = icatSchema.entityTypes[entityType].variablePaths;
            var limitOffset;
            var limitCount;

            this.where = function(where){
                if(whereList.length > 0) whereList.push('and');
                whereList.push(where);
                return this;
            };

            this.leftJoin = function(leftJoin){
                leftJoinList.push(leftJoin);
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
                    if(leftJoinList.includes(name)) {
                        joins.push(["LEFT OUTER JOIN ? ?", pair.safe(), name.safe()])
                    } else {
                        joins.push([", ? as ?", pair.safe(), name.safe()]);
                    }
                });

                if(joins.length > 0){
                    out.push(joins);
                }

                if(whereQuery.length > 0){
                    out.push(['where', whereQuery]);
                }

                if(orderByList.length > 0){
                    out.push(['ORDER BY', orderByList.join(', ')]);
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
                'promise': function(timeout){
                    return this.run({timeout: timeout});
                },
                '': function(){
                    return this.run({});
                }
            });

            this.count = helpers.overload({
                'object': function(options){
                    return icat.query([this.build('count')], options).then(function(results){
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

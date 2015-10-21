(function() {
    'use strict';

    angular.
        module('angularApp').service('ICATQueryBuilder', ICATQueryBuilder);

    ICATQueryBuilder.$inject = ['ICATAlias'];

    function ICATQueryBuilder(ICATAlias) {
        //private methods
        //
        /**
         * squel expects false for DESC order
         * @param  {[type]} order [description]
         * @return {[type]}       [description]
         */
        function sortOrder(order) {
            if (typeof order !== 'undefined' || order !== null) {
                if (order.toUpperCase() === 'DESC') {
                    return false;
                }
            }

            return true;
        }

        /**
         * [validateRequiredArguments description]
         * @param  {[type]} mySessionId [description]
         * @param  {[type]} facility    [description]
         * @param  {[type]} queryParams [description]
         * @return {[type]}             [description]
         */
        function validateRequiredArguments(mySessionId, facility, queryParams) {
            //session argument is required and must be a string
            if (!mySessionId && ! angular.isString(mySessionId)) {
                throw new Error('Invalid arguments. Session string is expected');
            }

            //facility argument is required and must be an object
            if (! facility && ! angular.isObject(facility)) {
                throw new Error('Invalid arguments. facility object is expected');
            }

            //facility key facilityId and icatUrl are required
            if (! angular.isDefined(facility.facilityId) || ! angular.isDefined(facility.icatUrl)) {
                throw new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl');
            }

            //queryParams is optional
            if (angular.isDefined(queryParams)) {
                //queryParams must be an object
                if (! angular.isObject(queryParams)) {
                    throw new Error('Invalid arguments. queryParams must be an object');
                }
            }
        }


        /*function urlEncodeParameters(params) {
            var p = _.object(_.map(params, function(v, k) {
                return [k, encodeURIComponent(v)];
            }));

            return params;
        }*/

        function aliasIncludeString(include) {
            var index = include.indexOf('.');
            var entity = include.substr(0, index);
            var remain = include.substr(index);

            return ICATAlias.getAlias(entity) + remain;
        }

        function buildInclude(query, includes) {
            _.each(includes, function(include) {
                include = aliasIncludeString(include);

                query.include(include);
            });

        }

        function buildParams(query, countQuery, searchExpr, queryParams, entityName) {
            var params = {};

            if (angular.isDefined(queryParams)) {
                if (!_.isEmpty(queryParams.search) && _.isArray(queryParams.search)) {
                    //TODO needs refactoring
                    //temp array to avoid duplicates
                    var temp = [];

                    _.each(searchExpr.current.nodes, function(node) {
                        //check if filter has more than 2 levels as icat can only deal with 2 in JPQL
                        if ((node.expr.match(/\./g) || []).length > 2) {
                            //get string within brackets  UPPER(....) LIKE
                            var matches = node.expr.match(/\(([^)]+)\)/);
                            var q = null;
                            if (matches !== null) {
                                q = matches[1];
                            }

                            //keep a copy of the original string
                            var o = angular.copy(q);

                            //replace any array square brackets [] from the string
                            q = q.replace(/\[\d+\]/g, '');

                            //we need to split the strig to 1 level chunks (i.e. contain one .)
                            var parts = q.split('.');
                            var pairs = _.chunk(parts, 2);

                            //variable to hold alias
                            var alias = '';
                            //variable to hold the number of chunks
                            var length = pairs.length;

                            _.each(pairs, function(pair, index) {
                                if (length > index + 1) {
                                    //if not last chunk
                                    alias = pair.join('');
                                    var p = pair.join('.') + ' ' + alias;

                                    //check unique
                                    if (temp.indexOf(p) === -1) {
                                        temp.push(p);
                                        query.from(pair.join('.') + ' ' + alias);
                                        countQuery.from(pair.join('.') + ' ' + alias);
                                    }
                                } else {
                                    //replace the existing WHERE search
                                    node.expr = node.expr.replace(o, alias + '.' + pair.join('.'));
                                }
                            });
                        }
                    });

                    query.where(
                        searchExpr
                    );

                    countQuery.where(
                        searchExpr
                    );
                }

                if (angular.isDefined(queryParams.includes) && queryParams.includes.length > 0) {
                    buildInclude(query, queryParams.includes);
                }

                //set limit
                if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                    query.limit(queryParams.start, queryParams.numRows);
                }

                //set sort
                if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                    //works but not sure for all scenarios!! Need rafactoring!!
                    //temp array to avoid duplicates
                    var t = [];

                    var sortQuery = ICATAlias.getAlias(entityName) + '.' + queryParams.sortField;

                    //check if filter has more than 2 levels as icat can only deal with 2 in JPQL
                    if ((sortQuery.match(/\./g) || []).length >= 2) {

                        //replace any array square brackets [] from the string
                        sortQuery = sortQuery.replace(/\[\d+\]/g, '');

                        //we need to split the strig to 1 level chunks (i.e. contain one .)
                        var parts = sortQuery.split('.');
                        var pairs = _.chunk(parts, 2);

                        //variable to hold alias
                        var alias = '';
                        //variable to hold the number of chunks
                        var length = pairs.length;

                        _.each(pairs, function(pair, index) {
                            if (length > index + 1) {
                                //if not last chunk
                                alias = pair.join('');
                                var p = pair.join('.') + ' ' + alias;

                                //check unique
                                if (t.indexOf(p) === -1) {
                                    t.push(p);
                                    query.from(pair.join('.') + ' ' + alias);
                                }
                            } else {
                                sortQuery = alias + '.' + pair.join('.');
                            }
                        });
                    }

                    query.order(sortQuery, sortOrder(queryParams.order));
                }
            }

            return params;
        }

        function getSearchExpr(queryParams, entityName) {
            var searchExpr = squel.expr();
            var entityAlias = ICATAlias.getAlias(entityName);

            if (! angular.isDefined(queryParams)) {
                return searchExpr;
            }

            if (! angular.isDefined(queryParams.search)) {
                return searchExpr;
            }

            if (! _.isEmpty(queryParams.search) && _.isArray(queryParams.search)) {
                _.each(queryParams.search, function(value) {
                    var filterCount = value.search.length;
                    if (filterCount === 1) {
                        if (typeof value.search[0] !== 'undefined' && value.search[0] !== null && value.search[0].trim() !== '') {
                            if (value.type === 'string') {
                                searchExpr.and('UPPER(' + entityAlias + '.' + value.field + ') LIKE ?', '%' + value.search[0].toUpperCase().replace('*', '%').replace('?', '_').replace('?', '_') + '%');
                            }

                            if (value.type === 'date') {
                                searchExpr.and(entityAlias + '.' + value.field + ' BETWEEN {ts ' + value.search[0] + ' 00:00:00} AND {ts ' + value.search[0] + ' 23:59:59}');
                            }
                        }
                    }

                    if(filterCount > 1) {
                        if (typeof value.search[0] !== 'undefined' && value.search[0] !== null && value.search[0].trim() !== '') {
                            if (typeof value.search[1] !== 'undefined' && value.search[1] !== null && value.search[1].trim() !== '') {
                                if (value.type === 'date') {
                                    searchExpr.and(entityAlias + '.' + value.field + ' BETWEEN {ts ' + value.search[0] + ' 00:00:00} AND {ts ' + value.search[1] + ' 23:59:59}');
                                }
                            }
                        }
                    }
                });
            }

            return searchExpr;
        }


        this.getFacilityCycles = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(fc)')
                .from('FacilityCycle', 'fc')
                .from('fc.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('fc')
                .from('FacilityCycle', 'fc')
                .from('fc.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var searchExpr = getSearchExpr(queryParams, 'facilityCycle');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'facilityCycle');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                //entity: 'FacilityCycle',
                server: facility.icatUrl
            });

            return params;
        };

        this.getFacilityCyclesByInstrumentId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(fc)')
                .from('FacilityCycle', 'fc')
                .from('fc.facility', 'f')
                .from('f.investigations', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('fc')
                .from('FacilityCycle', 'fc')
                .from('fc.facility', 'f')
                .from('f.investigations', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var searchExpr = getSearchExpr(queryParams, 'facilityCycle');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'facilityCycle');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                //entity: 'FacilityCycle',
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatasetsByFacilityCycleId = function() {

        };

        this.getDatafilesByFacilityCycleId = function() {

        };

        this.getInvestigations = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(inv)')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('inv')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            if (angular.isDefined(queryParams.user) && queryParams.user === true) {
                query.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );

                countQuery.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );
            }

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                //entity: 'Investigation',
                server: facility.icatUrl
            });

            return params;
        };

        this.getMyInvestigations = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(inv)')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('inv')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            if (angular.isDefined(queryParams.user) && queryParams.user === true) {
                query.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );

                countQuery.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );
            }


            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                //entity: 'Investigation',
                server: facility.icatUrl
            });

            return params;
        };


        this.getProposals = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(DISTINCT inv.name)')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .distinct()
                .field('inv.name')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        /** get instruments **/
        this.getInstruments = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ins)')
                .from('Instrument', 'ins')
                .from('ins.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ins')
                .from('Instrument', 'ins')
                .from('ins.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var searchExpr = getSearchExpr(queryParams, 'instrument');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'instrument');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getInstrumentsByProposalId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ins)')
                .from('Instrument', 'ins')
                .from('ins.facility', 'f')
                .from('ins.investigationInstruments', 'invins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('invins.investigation.name = ?', queryParams.proposalId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ins')
                .from('Instrument', 'ins')
                .from('ins.facility', 'f')
                .from('ins.investigationInstruments', 'invins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('invins.investigation.name = ?', queryParams.proposalId)
                );

            var searchExpr = getSearchExpr(queryParams, 'instrument');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'instrument');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getInvestigationsByFacilityCycleId = function(mySessionId, facility, queryParams){
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('inv')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .from('f.facilityCycles', 'fc')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('inv')
                .from('Investigation', 'inv')
                .from('inv.facility', 'f')
                .from('f.facilityCycles', 'fc')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                //entity: 'FacilityCycle',
                server: facility.icatUrl
            });

            return params;
        };

        this.getProposalsByInstrumentId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(DISTINCT inv.name)')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .distinct()
                .field('inv.name')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };


        this.getProposalsByFacilityCycleId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(DISTINCT inv.name)')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .from('f.facilityCycles', 'fc')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and('fc.id = ?', queryParams.facilityCycleId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .distinct()
                .field('inv.name')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .from('f.facilityCycles', 'fc')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                        .and('fc.id = ?', queryParams.facilityCycleId)
                        .and_begin() //jshint ignore:line
                            .and('inv.startDate BETWEEN fc.startDate AND fc.endDate')
                        .end()
                );

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getInvestigationsByProposalId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(DISTINCT inv)')
                .from('Investigation', 'inv')
                //.from('inv.investigationInstruments', 'invins')
                //.from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.name = ?', decodeURIComponent(queryParams.proposalId))   //TODO added decodeURIComponent until ui-router bug https://github.com/angular-ui/ui-router/pull/2071
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .distinct()
                .field('inv')
                .from('Investigation', 'inv')
                //.from('inv.investigationInstruments', 'invins')
                //.from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.name = ?', decodeURIComponent(queryParams.proposalId))
                );


            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };


        this.getInvestigationsByInstrumentId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(inv)')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('inv')
                .from('Investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );


            //inclusive filter TODO
            if (typeof queryParams.proposalId !== 'undefined') {
                countQuery.where(
                    squel.expr()
                        .and('inv.name = ?', queryParams.proposalId)
                );

                query.where(
                    squel.expr()
                        .and('inv.name = ?', queryParams.proposalId)
                );
            }

            var searchExpr = getSearchExpr(queryParams, 'investigation');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getInvestigationsByInstrumentIdByCycleId = function() {

        };

        this.getDatasets = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ds)')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ds')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            if (angular.isDefined(queryParams.user) && queryParams.user === true) {
                query.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );

                countQuery.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );
            }

            var searchExpr = getSearchExpr(queryParams, 'dataset');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'dataset');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };


        this.getMyDatasets = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ds)')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ds')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            if (angular.isDefined(queryParams.user) && queryParams.user === true) {
                query.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );

                countQuery.from('inv.investigationUsers', 'invu')
                .where(
                    squel.expr()
                        .and('invu.user.name = :user')
                );
            }

            var searchExpr = getSearchExpr(queryParams, 'dataset');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'dataset');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatasetsByInstrumentId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ds)')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ds')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var searchExpr = getSearchExpr(queryParams, 'dataset');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'dataset');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatasetsByInvestigationId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(ds)')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.id = ?', queryParams.investigationId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ds')
                .from('Dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.id = ?', queryParams.investigationId)
                );

            var searchExpr = getSearchExpr(queryParams, 'dataset');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'dataset');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatafiles = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(df)')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('df')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                );

            var searchExpr = getSearchExpr(queryParams, 'datafile');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'datafile');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatafilesByInstrumentId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(df)')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('df')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.investigationInstruments', 'invins')
                .from('invins.instrument', 'ins')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ins.id = ?', queryParams.instrumentId)
                );

            var searchExpr = getSearchExpr(queryParams, 'datafile');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'datafile');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatafilesByInvestigationId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(df)')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.id = ?', queryParams.investigationId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('df')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('inv.id = ?', queryParams.investigationId)
                );

            var searchExpr = getSearchExpr(queryParams, 'datafile');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'datafile');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };

        this.getDatafilesByDatasetId = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('COUNT(df)')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ds.id = ?', queryParams.datasetId)
                );

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('df')
                .from('Datafile', 'df')
                .from('df.dataset', 'ds')
                .from('ds.investigation', 'inv')
                .from('inv.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', facility.facilityId)
                        .and('ds.id = ?', queryParams.datasetId)
                );

            var searchExpr = getSearchExpr(queryParams, 'datafile');

            var params = buildParams(query, countQuery, searchExpr, queryParams, 'datafile');

            _.extend(params, {
                sessionId: mySessionId,
                query: query.toString(),
                countQuery: countQuery.toString(),
                server: facility.icatUrl
            });

            return params;
        };


        this.getEntityById = function(mySessionId, facility, queryParams) {
            validateRequiredArguments(mySessionId, facility, queryParams);

            var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('e')
                .from(queryParams.entityType, 'e')
                .where(
                    squel.expr()
                        .and('e.id = ?', queryParams.entityId)
                );

            /*if (typeof queryParams.include !== 'undefined') {
                _.each(queryParams.include, function(value) {
                    query.include(value);
                });
            }*/

            var params = {
                sessionId: mySessionId,
                query: query.toString(),
                //entity: queryParams.entityType,
                server: facility.icatUrl
            };

            return params;
        };
    }
})();
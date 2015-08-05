(function() {
    'use strict';

    angular.
        module('angularApp').factory('ICATQueryBuilder', ICATQueryBuilder);

    ICATQueryBuilder.$inject = ['ICATAlias', '$log'];

    function ICATQueryBuilder(ICATAlias, $log) {
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
            //var filterCountQuery = countQuery.clone();

            if (angular.isDefined(queryParams)) {
                if (!_.isEmpty(queryParams.search) && _.isArray(queryParams.search)) {
                    query.where(
                        searchExpr
                    );

                    /*filterCountQuery.where(
                        searchExpr
                    );

                    params.filterCountQuery = filterCountQuery;*/
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
                    query.order(ICATAlias.getAlias(entityName) + '.' + queryParams.sortField, sortOrder(queryParams.order));
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
                    if (typeof value.search !== 'undefined') {
                        searchExpr.and('UPPER(' + entityAlias + '.' + value.field + ') LIKE ?', '%' + value.search.toUpperCase() + '%');
                    }
                });
            }

            return searchExpr;
        }


        // Public API here
        return {
            /*buildUrl: function(url, params) {
                if (!params) {
                    return url;
                }
                var parts = [];
                forEachSorted(params, function(value, key) {
                    if (value === null || value === undefined) {
                        return;
                    }
                    if (angular.isObject(value)) {
                        value = angular.toJson(value);
                    }
                    parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
                });
                return url + ((url.indexOf('?') === -1) ? '?' : '&') + parts.join('&');
            },*/

            getFacilityCycles: function(mySessionId, facility, queryParams) {
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
            },

            getFacilityCyclesByInstrumentId: function(mySessionId, facility, queryParams) {
                validateRequiredArguments(mySessionId, facility, queryParams);
                //SELECT fc FROM FacilityCycle fc, fc.facility f, f.investigations inv, inv.investigationInstruments invins, invins.instrument ins
                //WHERE (f.id = 1 AND ins.id = 11 AND (inv.startDate BETWEEN fc.startDate AND fc.endDate)) ORDER BY fc.name ASC LIMIT 0, 50
                $log.debug('queryParams', queryParams);
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
            },

            getDatasetsByFacilityCycleId: function() {

            },

            getDatafilesByFacilityCycleId: function() {

            },


            getInvestigations: function(mySessionId, facility, queryParams) {
                $log.debug('getInvestigations fired');

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
            },


            getProposals: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Proposal',
                    server: facility.icatUrl
                });

                return params;
            },

            /** get instruments **/
            getInstruments: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Instrument',
                    server: facility.icatUrl
                });

                return params;
            },

            getInvestigationsByFacilityCycleId: function(mySessionId, facility, queryParams){
                validateRequiredArguments(mySessionId, facility, queryParams);
                //SELECT fc FROM FacilityCycle fc, fc.facility f, f.investigations inv, inv.investigationInstruments invins, invins.instrument ins
                //WHERE (f.id = 1 AND ins.id = 11 AND (inv.startDate BETWEEN fc.startDate AND fc.endDate)) ORDER BY fc.name ASC LIMIT 0, 50
                $log.debug('queryParams', queryParams);
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

            },

            getProposalsByInstrumentId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Proposal',
                    server: facility.icatUrl
                });

                return params;
            },


            getProposalsByFacilityCycleId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Proposal',
                    server: facility.icatUrl
                });

                return params;
            },

            getInvestigationsByProposalId: function(mySessionId, facility, queryParams) {
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
                            .and('inv.name = ?', queryParams.proposalId)
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
                            .and('inv.name = ?', queryParams.proposalId)
                    );

                var searchExpr = getSearchExpr(queryParams, 'investigation');

                var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

                _.extend(params, {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Investigation',
                    server: facility.icatUrl
                });

                $log.debug('queryParams', queryParams);

                return params;
            },


            getInvestigationsByInstrumentId: function(mySessionId, facility, queryParams) {
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

                var searchExpr = getSearchExpr(queryParams, 'investigation');

                var params = buildParams(query, countQuery, searchExpr, queryParams, 'investigation');

                _.extend(params, {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Investigation',
                    server: facility.icatUrl
                });

                return params;
            },

            getInvestigationsByInstrumentIdByCycleId: function() {

            },

            getDatasets: function(mySessionId, facility, queryParams) {
                validateRequiredArguments(mySessionId, facility, queryParams);

                $log.debug(queryParams);

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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Dataset',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatasetsByInstrumentId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Dataset',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatasetsByInvestigationId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Dataset',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatafiles: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Datafile',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatafilesByInstrumentId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Datafile',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatafilesByInvestigationId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Datafile',
                    server: facility.icatUrl
                });

                return params;
            },

            getDatafilesByDatasetId: function(mySessionId, facility, queryParams) {
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
                    //filterCountQuery: filterCountQuery.toString(),
                    //entity: 'Datafile',
                    server: facility.icatUrl
                });

                return params;
            },


            getEntityById: function(mySessionId, facility, queryParams) {
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
            }
        };
    }
})();

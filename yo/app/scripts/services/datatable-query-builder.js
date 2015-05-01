'use strict';

/**
 * @ngdoc service
 * @name angularApp.DataTableQueryBuilder
 * @description
 * # DataTableQueryBuilder
 * Factory in the angularApp.
 */
angular.module('angularApp')
    .factory('DataTableQueryBuilder', function() {
        // Service logic

        var ICATDATAPROXYURL = 'https://localhost:3001';

        function forEachSorted(obj, iterator, context) {
            var keys = sortedKeys(obj);
            for (var i = 0; i < keys.length; i++) {
                iterator.call(context, obj[keys[i]], keys[i]);
            }
            return keys;
        }

        function sortedKeys(obj) {
            var keys = [];
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    keys.push(key);
                }
            }
            return keys.sort();
        }

        /**
         * squel expects false for DESC order
         * @param  {[type]} order [description]
         * @return {[type]}       [description]
         */
        function sortOrder(order) {
            if (order.toUpperCase === 'DESC') {
                return false;
            }

            return true;
        }

        function validateRequiredArguments(mySessionId, facility, queryParams, absUrl) {
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

            //url is optional
            if (angular.isDefined(absUrl)) {
                //url must be an object
                if (typeof absUrl !== 'boolean') {
                    throw new Error('Invalid arguments. url must be a string');
                }
            }
        }


        // Public API here
        return {
            buildUrl: function(url, params) {
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
            },

            /** get instruments **/
            getInstruments: function(mySessionId, facility, queryParams, absUrl) {
                /*console.log('getInstruments session: ', mySessionId);
                console.log('getInstruments queryParams: ', queryParams);
                console.log('getInstruments facility: ', facility);*/

                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var url = ICATDATAPROXYURL + '/icat/entityManager';

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

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search)) {
                        query.where(
                            squel.expr().and('UPPER(ins.name) LIKE ?', '%' + queryParams.search + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Instrument',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(url, params);
                } else {
                    return this.buildUrl('', params);
                }
            }
        };
    });
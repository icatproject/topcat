'use strict';

/**
 * @ngdoc service
 * @name angularApp.SquelCustomQuery
 * @description
 * # SquelCustomQuery
 * Factory in the angularApp.
 */
angular.module('angularApp')
    .factory('SquelCustomQuery', function() {

        return {
            init: function() {
                /* OOP Inheritance mechanism (substitute your own favourite library for this!) */
                Function.prototype.inheritsFrom = function(ParentClassOrObject) {
                    this.prototype = new ParentClassOrObject();
                    this.prototype.constructor = this;
                    this.prototype.parent = ParentClassOrObject.prototype;
                };


                /* Create the 'include' clause */
                var IncludeBlock = function() {};
                IncludeBlock.inheritsFrom(squel.cls.Block);

                IncludeBlock.prototype.include = function(i) {
                    var include = this._i || [];
                    include.push(i);
                    this._i = include;
                };

                IncludeBlock.prototype.buildStr = function() {
                    if (angular.isDefined(this._i) && this._i.length > 0) {
                        return 'INCLUDE ' + this._i.join(', ');
                    }

                    return '';
                };

                var LimitBlock = function() {};
                LimitBlock.inheritsFrom(squel.cls.Block);

                LimitBlock.prototype.limit = function(start, numRows) {
                    start = this._sanitizeLimitOffset(start);
                    numRows = this._sanitizeLimitOffset(numRows);
                    this._limitStart = start;
                    this._limitNumRows = numRows;
                };

                LimitBlock.prototype.buildStr = function() {
                    if (angular.isDefined(this._limitStart) && angular.isDefined(this._limitNumRows)) {
                        return 'LIMIT ' + this._limitStart + ', ' + this._limitNumRows;
                    }

                    return '';
                };


                squel.ICATSelect = function(options) {
                    return squel.select(options, [
                        new squel.cls.StringBlock(options, 'SELECT'),
                        new squel.cls.DistinctBlock(options),
                        new squel.cls.GetFieldBlock(options),
                        new squel.cls.FromTableBlock(options),
                        new squel.cls.JoinBlock(options),
                        new squel.cls.WhereBlock(options),
                        new squel.cls.GroupByBlock(options),
                        new squel.cls.OrderByBlock(options),
                        new IncludeBlock(options),
                        new LimitBlock(options),
                        new squel.cls.OffsetBlock(options),
                        new squel.cls.UnionBlock(options)
                    ]);
                };
            }
        };
    });

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('helpers', function($http, $q, $timeout, uiGridConstants, icatSchema, topcatSchema){
    	var helpers = this;

    	this.setupMetatabs = function(metaTabs, entityType){
    		_.each(metaTabs, function(metaTab){
                _.each(metaTab.items, function(item){
                    var field = item.field;
                    if(!field) return;
                    var matches;
                    if(matches = field.replace(/\|.+$/, '').match(/^([^\[\]]+).*?\.([^\.\[\]]+)$/)){
                        var variableName = matches[1];
                        entityType = icatSchema.variableEntityTypes[variableName];
                        if(!entityType){
                            console.error("Unknown variableName: " + variableName, item)
                        }
                        entityType = entityType;
                        field = matches[2];
                    }

                    if(!item.title){
                        var entityTypeNamespace = helpers.constantify(entityType);
                        var fieldNamespace = helpers.constantify(field);
                        item.title = "METATABS." + entityTypeNamespace + "." + fieldNamespace;
                    }
                });
            });
    	};

    	this.setupColumnDef = function(columnDef, entityType, translateNameSpace){
            var type = columnDef.type;
            var field = columnDef.field.replace(/^.*\./, '').replace(/\|.*$/, '');


            if(!columnDef.filter){
                if(type == 'string'){
                    columnDef.filter = {
                        "condition": uiGridConstants.filter.CONTAINS,
                        "placeholder": "Containing...",
                        "type": "input",
                    }
                }
            }
            if(!columnDef.filters){
                if(type == 'date'){
                    columnDef.filters = [
                        {
                        	"condition": 'GREATER_THAN_OR_EQUAL',
                            "placeholder": "From...",
                            "type": "input"
                        },
                        {
                        	"condition": 'LESS_THAN_OR_EQUAL',
                            "placeholder": "To...",
                            "type": "input"
                        }
                    ];
                }
            }
            if(!columnDef.cellFilter){
                if(field.match(/Date$/)){
                    columnDef.cellFilter = "date: 'yyyy-MM-dd'"
                } else {
                    columnDef.cellFilter = "date: 'yyyy-MM-dd HH:mm:ss'"
                }
            }

            if(!columnDef.title){
                var fieldNamespace = helpers.constantify(field);
                columnDef.title = translateNameSpace + '.' + fieldNamespace;
            }

            var entityTypeNamespace = helpers.constantify(entityType);

            if(field === 'size' || field === 'fileSize') {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
            	columnDef.enableSorting = false;
                columnDef.enableFiltering = false;
            }

            if(field === 'status') {
               columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.status === undefined" class="grid-cell-spinner"></span><span ng-if="row.entity.status">{{"' + entityTypeNamespace + '.STATUS." + row.entity.status | translate}}</span></div>';
            }


            if(columnDef.title){
                columnDef.displayName = columnDef.title;
                columnDef.headerCellFilter = 'translate';
            }

            if(columnDef.sort){
                if(columnDef.sort.direction.toLowerCase() == 'desc'){
                    columnDef.sort.direction = uiGridConstants.DESC;
                } else {
                    columnDef.sort.direction = uiGridConstants.ASC;
                }
            }

            if(columnDef.type == 'date'){
                if(columnDef.field && columnDef.field.match(/Date$/)){
                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[1].term" placeholder="To..."></div>';
                } else {
                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[1].term" placeholder="To..."></div>';
                }
            }


            if(columnDef.filter && typeof columnDef.filter.condition == 'string'){
            	columnDef.filter.condition = uiGridConstants.filter[columnDef.filter.condition.toUpperCase()];
            }
    	};

    	this.setupTopcatGridOptions = function(gridOptions, entityType){
    		var pagingConfig = tc.config().paging;
	        var isScroll = pagingConfig.pagingType == 'scroll';
	        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;

	        gridOptions.enableHorizontalScrollbar = uiGridConstants.scrollbars.NEVER;
            gridOptions.enableRowSelection =  false;
            gridOptions.enableRowHeaderSelection =  false;
            gridOptions.gridMenuShowHideColumns =  false;
            gridOptions.pageSize =  !this.isScroll ? pagingConfig.paginationNumberOfRows : null;
            gridOptions.paginationPageSizes =  pagingConfig.paginationPageSizes;
            gridOptions.paginationNumberOfRows =  pagingConfig.paginationNumberOfRows;
            gridOptions.useExternalPagination =  true;
            gridOptions.useExternalSorting =  true;
            gridOptions.useExternalFiltering =  true;
            gridOptions.enableFiltering = true;
            gridOptions.enableSelection = false;

            var entitySchema = topcatSchema.entityTypes[entityType];

            _.each(gridOptions.columnDefs, function(columnDef){
            	var field = columnDef.field;
            	var type = entitySchema.fields[field];
            	if(!columnDef.type) columnDef.type = type;
            	helpers.setupColumnDef(columnDef, entityType, helpers.constantify(entityType) + '.COLUMN');
	        });
    	};

    	this.setupIcatGridOptions = function(gridOptions, entityType){
    		if(entityType != 'facility'){
    			gridOptions.useExternalPagination = true;
	        	gridOptions.useExternalSorting = true;
	        	gridOptions.useExternalFiltering = true;
	    	}
	        var enableSelection = gridOptions.enableSelection === true && entityType.match(/^investigation|dataset|datafile$/) !== null;
	        gridOptions.enableSelectAll = false;
	        gridOptions.enableRowSelection = enableSelection;
	        gridOptions.enableRowHeaderSelection = enableSelection;
	        gridOptions.enableFiltering = true;
	        gridOptions.rowTemplate = '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>';

    		_.each(gridOptions.columnDefs, function(columnDef){
    			var matches;
                var field = columnDef.field;
                var variableEntityType = entityType;
                if(matches = field.replace(/\|.+$/, '').match(/^([^\[\]]+).*?\.([^\.\[\]]+)$/)){
                    var variableName = matches[1];
                    variableEntityType = icatSchema.variableEntityTypes[variableName];
                    if(!entityType){
                        console.error("Unknown variableName: " + variableName, columnDef)
                    }
                    entityType = entityType;
                    field = matches[2];
                }

                if(!variableEntityType) return;
                
                var entitySchema = icatSchema.entityTypes[variableEntityType];
                var type = entitySchema.fields[field];
                if(!columnDef.type) columnDef.type = type;
                var entityTypeNamespace = helpers.constantify(variableEntityType);
                helpers.setupColumnDef(columnDef, entityType, 'BROWSE.COLUMN.' + entityTypeNamespace);

	            var filters = "";
	            var matches;
	            if(matches = columnDef.field.match(/^(.*?)(\|[^\.\[\]]*)$/)){
	                columnDef.field = matches[1];
	                filters = matches[2];
	            }

	            if(columnDef.type == 'date'){
	                if(columnDef.field && columnDef.field.match(/Date$/)){
	                    filters = filters + "|date:'yyyy-MM-dd'"
	                } else {
	                    filters = filters + "|date:'yyyy-MM-dd HH:mm:ss'"
	                }
	            }

	            if(columnDef.excludeFuture){
	                var date = new Date();
	                var day = date.getDate();
	                var month = "" + (date.getMonth() + 1);
	                if(month.length == 1) month = '0' + month;
	                var year = date.getFullYear();
	                var filter = year + '-' + month + '-' + day;
	                $timeout(function(){
	                    columnDef.filters[1].term = filter;
	                });
	            }

	            if(!columnDef.jpqlExpression){
	            	var _entityType = entityType;
	            	if(_entityType == 'proposal') _entityType = 'investigation';
	                if(!columnDef.field.match(/\./)){
	                    columnDef.jpqlExpression =  _entityType + '.' + columnDef.field;
	                } else {
	                    columnDef.jpqlExpression = columnDef.field;
	                }
	            }

	            var titleTemplate;
	            var showCondition;
	           
	            if(columnDef.type == 'number' && columnDef.filters){
	            	var pair = columnDef.jpqlExpression.split(/\./);
                    var _entityType = pair[0];
                    var entityField = pair[1];
	            	var fieldNameSuffix = helpers.capitalize(_entityType) + entityField;
	            	var minFieldName = "min" + fieldNameSuffix;
	            	var maxFieldName = "max" + fieldNameSuffix;
	            	titleTemplate = '{{row.entity.find(&quot;' + minFieldName + '&quot;)[0]' + filters + '}} - {{row.entity.find(&quot;' + maxFieldName + '&quot;)[0]' + filters + '}}';
	            	showCondition = 'row.entity.find(&quot;' + minFieldName + '&quot;).length > 0 && row.entity.find(&quot;' + maxFieldName + '&quot;).length > 0';
	            } else {
					titleTemplate = '{{row.entity.find(&quot;' + columnDef.field + '&quot;)[0]' + filters + '}}';
	            	showCondition = 'row.entity.find(&quot;' + columnDef.field + '&quot;).length > 0';
	            }

	            if(columnDef.link) {
	                if(typeof columnDef.link == "string"){
	                    titleTemplate = '<a ng-click="grid.appScope.browse(row.entity.' + columnDef.link + ')">' + titleTemplate + '</a></div>';
	                } else {
	                    titleTemplate = '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.browse(row.entity)">' + titleTemplate + '</a>';
	                }
	            }

	            columnDef.cellTemplate = columnDef.cellTemplate || [
	                '<div class="ui-grid-cell-contents">',
	                    '<span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="!(' + showCondition + ')" class="grid-cell-spinner"></span>',
	                    '<span ng-if="row.entity.find(&quot;' + columnDef.field + '&quot;).length > 1" class="glyphicon glyphicon-th-list" uib-tooltip="{{row.entity.find(&quot;' + columnDef.field + '&quot;).join(&quot;\n&quot;)}}" tooltip-placement="top" tooltip-append-to-body="true"></span> ',
	                    '<span ng-if="' + showCondition + '">',
	                    	titleTemplate,
						'</span>',                    
	                '</div>'
	            ].join('');
	        });


	        if(gridOptions.enableDownload){
	            gridOptions.columnDefs.push({
	                name : 'actions',
	                visible: true,
	                translateDisplayName: 'BROWSE.COLUMN.ACTIONS.NAME',
	                enableFiltering: false,
	                enable: false,
	                enableColumnMenu: false,
	                enableSorting: false,
	                enableHiding: false,
	                cellTemplate : '<div class="ui-grid-cell-contents"><a type="button" class="btn btn-primary btn-xs" translate="BROWSE.COLUMN.ACTIONS.LINK.DOWNLOAD.TEXT" uib-tooltip="{{\'BROWSE.COLUMN.ACTIONS.LINK.DOWNLOAD.TOOLTIP.TEXT\' | translate}}" tooltip-placement="right" tooltip-append-to-body="true" href="{{grid.appScope.downloadUrl(row.entity)}}" target="_blank"></a></div>'
	            });
	        }
    	};

    	this.completePartialFromDate = function(date){
            var segments = date.split(/[-:\s\/]+/);
            var year = segments[0];
            var month = segments[1] || "01";
            var day = segments[2] || "01";
            var hours = segments[3] || "00";
            var minutes = segments[4] || "00";
            var seconds = segments[5] || "00";

            year = year + '0000'.slice(year.length, 4);
            month = month + '00'.slice(month.length, 2);
            day = day + '00'.slice(day.length, 2);
            hours = hours + '00'.slice(hours.length, 2);
            minutes = minutes + '00'.slice(minutes.length, 2);
            seconds = seconds + '00'.slice(seconds.length, 2);

            if(parseInt(month) == 0) month = '01';
            if(parseInt(day) == 0) day = '01';

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        };

        this.completePartialToDate = function(date){
            var segments = date.split(/[-:\s\/]+/);
            var year = segments[0] || "";
            var month = segments[1] || "";
            var day = segments[2] || "";
            var hours = segments[3] || "23";
            var minutes = segments[4] || "59";
            var seconds = segments[5] || "59";
            year = year + '9999'.slice(year.length, 4);
            month = month + '99'.slice(month.length, 2);
            day = day + '99'.slice(day.length, 2);
            hours = hours + '33'.slice(hours.length, 2);
            minutes = minutes + '99'.slice(minutes.length, 2);
            seconds = seconds + '99'.slice(seconds.length, 2);

            if(parseInt(month) > 12) month = '12';
            var daysInMonth = new Date(year, day, 0).getDate();
            if(parseInt(day) > daysInMonth) day = daysInMonth;

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        };

        this.typeOf = function(data){
			var out = typeof data;
			if(out == 'object'){
				if(data instanceof Array) return 'array';
				if(data.then instanceof Function) return 'promise';
			}
			return out;
		}

		this.overload = function(variations){

			return function(){
				var that = this;
				var args = arguments;
				var argTypeOfs = _.map(args,  function(arg){ return helpers.typeOf(arg); });
				var found = false;
				var out;
				if(!variations.default){
					variations.default = function(){
						throw "Could not satisfy overloaded function '" + argTypeOfs.join(', ') + "'.";
					};
				}

				_.each(variations, function(fn, pattern){
					if(pattern == 'default') return false;
					pattern = pattern.trim().split(/\s*,\s*/);
					found = _.isEqual(argTypeOfs, pattern);
					if(found){
						out = fn.apply(that, args);
						return false;
					}
				});

				if(argTypeOfs.length == 0 && variations['']){
					out = variations[''].apply(that, args);
				} else if(!found){
					out = variations.default.apply(that, args);
				}

				return out;
			};
		}

		this.jpqlSanitize = function(data){
			if(typeof data == 'string' && !data.isSafe){
				return "'" + data.replace(/'/g, "''") + "'";
			}
			return data;
		};

		this.buildQuery = function(query){
			while(true){
	        	query = _.map(query, function(i){
	        		if(helpers.typeOf(i) == 'function') i = i.call(this);
	        		return i;
	        	});
	        	query = _.flatten(query);
	        	var isFunction = _.select(query, function(i){ return helpers.typeOf(i) == 'function'; }).length > 0;
	        	var isArray = _.select(query, function(i){ return helpers.typeOf(i) == 'array'; }).length > 0;
	        	if(!isFunction && !isArray) break;
	        }

	        query = _.select(query, function(i){ return i !== undefined; });

	        try {
	        	var _query = [];
	        	for(var i = 0; i < query.length; i++){
	        		var expression = [];
	        		var fragments = query[i].split(/\?/);
	        		for(var j in fragments){
	        			expression.push(fragments[j]);
	        			if(j < fragments.length - 1){
	        				i++;
	        				expression.push(helpers.jpqlSanitize(query[i]));
	        			}
	        		}
	        		_query.push(expression.join(''));
	        	}
	        } catch(e) {
	        	console.error("can't build query", query, e)
	        }
	        return _query.join(' ');
		};

		this.urlEncode = function(o){
			var out = [];
			_.each(o, function(value, key){
				out.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
			});
			return out.join('&');
		};

		this.uncapitalize = function(text){
			return ('' + text).replace(/^(.)/, function(s){ return s.toLowerCase(); });
		}

		this.capitalize = function(text){
			return ('' + text).replace(/^(.)/, function(s){ return s.toUpperCase(); });
		}

		this.constantify = function(text){
			return ('' + text).replace(/([A-Z])/g, '_$1').replace(/^_/, '').toUpperCase();
		};

		this.generateRestMethods = function(that, prefix){
			
			defineMethod.call(that, 'get');
			defineMethod.call(that, 'delete');
			defineMethod.call(that, 'post');
			defineMethod.call(that, 'put');

			function defineMethod(methodName){
				this[methodName] = helpers.overload({
					'string, string, object': function(offset, params, options){
						options = _.clone(options);
						if(methodName.match(/post|put/)){
							if(!options.headers) options.headers = {};
							if(!options.headers['Content-Type']) options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
						}
						if(_.isUndefined(options.byPassIntercepter)) options.byPassIntercepter = true;
						var url = prefix + offset;
						if(methodName.match(/get|delete/) && params !== '') url += '?' + params;
						var out = $q.defer();
						var args = [url];
						if(methodName.match(/post|put/)) args.push(params);
						args.push(options);
						$http[methodName].apply($http, args).then(function(response){
							out.resolve(response.data);
						}, function(response){
							out.reject(response.data);
						});
						return out.promise;
		    		},
					'string, object, object': function(offset, params, options){
						return this[methodName].call(this, offset, helpers.urlEncode(params), options)
		    		},
		    		'string, promise, object': function(offset, timeout, params){
		    			return this[methodName].call(this, offset, params, {timeout: timeout});
		    		},
		    		'string, object': function(offset, params){
		    			return this[methodName].call(this, offset, params, {});
		    		},
		    		'string, promise': function(offset, timeout){
		    			return this[methodName].call(this, offset, {}, {timeout: timeout});
		    		},
		    		'string': function(offset){
		    			return this[methodName].call(this, offset, {}, {});
		    		}
				});
			}

		};

		this.resolvedPromise = function(value){
			var defered = $q.defer();
			defered.resolve(value);
			return defered.promise;
		};

		(function(){
			var methods = {
	            get: $http.get,
	            delete: $http.delete,
	            post: $http.post,
	            put: $http.put
	        };

	        _.each(methods, function(method, name){
	            $http[name] = function(){
	                return extendPromise(method.apply(this, arguments));
	            };
	        });

	        var deferMethod = $q.defer;
	        $q.defer = function(){
	        	var out = deferMethod.apply(this, arguments);
	        	extendPromise(out.promise);
	        	return out;
	        };

	        function extendPromise(promise){
				promise.log = function(){
		            return this.then(function(data){
		                console.log('(success)', data); 
		            }, function(data){
		                console.log('(error)', data);   
		            }, function(data){
		                console.log('(notify)', data);  
		            });
		        };

		        var then = promise.then;
		        promise.then = function(){
		        	return extendPromise(then.apply(this, arguments));
		        };

		        return promise;
			}

	    })();

    });

	String.prototype.safe = function(){
		return new SafeString(this);
	};

	function SafeString(value){
		this.isSafe = true;
		this.value = value;
	}

	SafeString.prototype.toString = function(){
		return this.value;
	};

})();
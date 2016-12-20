
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('helpers', function($http, $q, $timeout, $interval, $rootScope, $injector, $compile, uiGridConstants, icatSchema, topcatSchema, plugins){
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

                    if(!item.label && item.label !== ''){
                        var entityTypeNamespace = helpers.constantify(entityType);
                        var fieldNamespace = helpers.constantify(field);
                        item.label = "METATABS." + entityTypeNamespace + "." + fieldNamespace;
                    }
                });
            });
    	};

    	this.setupColumnDef = function(columnDef, entityType, translateTitleNameSpace, translateStatusNameSpace){
            var type = columnDef.type;
            var field = columnDef.field.replace(/^.*\./, '').replace(/\|.*$/, '');

            if(!columnDef.filter){
                if(type == 'string' || type === undefined){
                    columnDef.filter = {
                        "condition": uiGridConstants.filter.CONTAINS,
                        "placeholder": "Containing...",
                        "type": "input"
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
                //this is a hack to satify Isis
                //todo: refactor to make more generic
                if(type == 'number' && columnDef.field == "datafileParameter.numericValue"){
                    columnDef.filters = [
                        {
                            "placeholder": "From...",
                            "type": "input"
                        },
                        {
                            "placeholder": "To...",
                            "type": "input"
                        }
                    ];
                }
            }
            if(!columnDef.cellFilter){
                if(field.match(/Date$/)){
                    columnDef.cellFilter = "date: 'yyyy-MM-dd'"
                } else if(field.match(/Time$/)) {
                    columnDef.cellFilter = "date: 'yyyy-MM-dd HH:mm:ss'"
                }
            }

            if(!columnDef.title){
                var fieldNamespace = helpers.constantify(field);
                columnDef.title = translateTitleNameSpace + '.' + fieldNamespace;
            }

            if(field === 'size') {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span ng-if="row.entity.size === undefined && $root.requestCounter != 0" class="loading">&nbsp;</span><span>{{row.entity.size|bytes}}</span></div>';
            	columnDef.enableSorting = false;
                columnDef.enableFiltering = false;
            }

            if(field === 'fileSize') {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents">{{row.entity.fileSize|bytes}}</div>';
            }

            if(field === 'status') {
               columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span ng-if="row.entity.status === undefined  && $root.requestCounter != 0" class="loading"></span><span ng-if="row.entity.status">{{"' + translateStatusNameSpace + '." + row.entity.status | translate}}</span></div>';
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
                columnDef.enableHiding = false;
            	var field = columnDef.field;
            	var type = entitySchema.fields[field];
            	if(!columnDef.type) columnDef.type = type;
                var translateColumnNameSpace = helpers.constantify(entityType.replace(/Item$/, '')) + '.COLUMN';
            	var translateStatusNameSpace = helpers.constantify(entityType.replace(/Item$/, '')) + '.STATUS';
                helpers.setupColumnDef(columnDef, entityType, translateColumnNameSpace, translateStatusNameSpace);
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

    		_.each(gridOptions.columnDefs, function(columnDef, i){
                columnDef.enableHiding = false;

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
	                var day = "" + date.getDate();
                    if(day.length == 1) day = '0' + day;
	                var month = "" + (date.getMonth() + 1);
	                if(month.length == 1) month = '0' + month;
	                var year = date.getFullYear();
	                var filter = year + '-' + month + '-' + day;
	                $timeout(function(){
	                    columnDef.filters[1].term = filter;
	                });
	            }

                var jpqlExpression = columnDef.field;
                if(!columnDef.field.match(/\./)){
                    if(entityType == 'proposal'){
                        jpqlExpression = 'investigation.' + jpqlExpression;
                    } else {
                        jpqlExpression = entityType + '.' + jpqlExpression;
                    }
                    
                }
                if(!columnDef.jpqlFilter) columnDef.jpqlFilter = jpqlExpression;
                if(!columnDef.jpqlSort) columnDef.jpqlSort = jpqlExpression;

	            var titleTemplate;
	            var showCondition;
	            if(columnDef.type == 'number' && columnDef.filters){
	            	var pair = jpqlExpression.split(/\./);
                    var _entityType = pair[0];
                    var entityField = pair[1];
	            	var fieldNameSuffix = helpers.capitalize(_entityType) + helpers.capitalize(entityField);
	            	var minFieldName = "min" + fieldNameSuffix;
	            	var maxFieldName = "max" + fieldNameSuffix;
	            	titleTemplate = '{{row.entity.find(&quot;' + minFieldName + '&quot;)[0]' + filters + '}} - {{row.entity.find(&quot;' + maxFieldName + '&quot;)[0]' + filters + '}}';
	            	showCondition = 'row.entity.find(&quot;' + minFieldName + '&quot;).length > 0 && row.entity.find(&quot;' + maxFieldName + '&quot;).length > 0';
                    columnDef.enableSorting = false;
                } else {
					titleTemplate = '{{row.entity.find(&quot;' + columnDef.field + '&quot;)[0]' + filters + '}}';
	            	showCondition = 'row.entity.find(&quot;' + columnDef.field + '&quot;).length > 0';
	            }

	            if(columnDef.link) {
	                if(typeof columnDef.link == "string"){
	                    titleTemplate = '<a ng-click="grid.appScope.browse(row.entity.' + columnDef.link + ')">' + titleTemplate + '</a>';
	                } else {
	                    titleTemplate = '<a ng-click="grid.appScope.browse(row.entity)">' + titleTemplate + '</a>';
	                }
	            }

                var tooltipPlacement = i == 0 ? 'right' : 'top';

	            columnDef.cellTemplate = columnDef.cellTemplate || [
	                '<div class="ui-grid-cell-contents">',
	                    '<span ng-if="!(' + showCondition + ') && $root.requestCounter != 0" class="loading">&nbsp;</span>',
                        '<span ',
                            'ng-if="row.entity.find(&quot;' + columnDef.field + '&quot;).length > 1" ',
                            'uib-dropdown dropdown-append-to-body ',
                            'tooltip-append-to-body="true">',
                            '<button ng-click="$event.stopPropagation();" type="button" class="btn btn-default btn-xs" uib-dropdown-toggle>',
                                '<span class="caret"></span> Multiple values (e.g. &quot;{{row.entity.find(&quot;' + columnDef.field + '&quot;)[0]}}&quot;)',
                            '</button>',
                            '<ul class="dropdown-menu" uib-dropdown-menu>',
                                '<li ng-repeat="item in row.entity.find(&quot;' + columnDef.field + '&quot;)"><a>{{item}}</a></li>',
                            '</ul>',
                        '</span> ',
                        '<span ng-if="row.entity.find(&quot;' + columnDef.field + '&quot;).length == 1 && ' + showCondition + '">',
	                    	titleTemplate,
						'</span>',          
	                '</div>'
	            ].join('');
	        });

            var actionButtons = [];

	        if(gridOptions.enableDownload){
                actionButtons.push({
                    name: "download",
                    click: function(entity){
                        var sessionId = entity.facility.icat().session().sessionId;
                        var id = entity.id;
                        var name = entity.location.replace(/^.*\//, '');
                        var idsUrl = entity.facility.config().idsUrl + [
                            '/ids/getData?sessionId=' + encodeURIComponent(sessionId),
                            'datafileIds=' + id,
                            'compress=false',
                            'zip=false',
                            'outfile=' + encodeURIComponent(name)
                        ].join('&');

                        $(document.body).append($('<iframe>').attr({
                            src: idsUrl
                        }).css({
                            position: 'relative',
                            left: '-1000000px',
                            height: '1px',
                            width: '1px'
                        }));
                    },
                    class: "btn btn-primary",
                    translate: "DOWNLOAD_ENTITY_ACTION_BUTTON.TEXT",
                    translateTooltip: "DOWNLOAD_ENTITY_ACTION_BUTTON.TOOLTIP.TEXT"
                });
	        }

            _.each($injector.get('tc').ui().entityActionButtons(), function(button){
                if(_.includes(button.options.entityTypes, entityType)){
                    actionButtons.push({
                        name: button.name,
                        click: button.click,
                        class: button.options.class || "btn btn-primary",
                        translate: button.name.toUpperCase().replace(/-/g, '_') + "_ENTITY_ACTION_BUTTON.TEXT",
                        translateValues: button.options.translateValues,
                        translateTooltip: button.name.toUpperCase().replace(/-/g, '_') + "_ENTITY_ACTION_BUTTON.TOOLTIP.TEXT",
                        insertBefore: button.options.insertBefore,
                        insertAfter: button.options.insertAfter,
                        show: button.options.show
                    });
                }
            });

            gridOptions.actionButtons = this.mergeNamedObjectArrays([], actionButtons);

            if(gridOptions.actionButtons.length > 0){
                gridOptions.columnDefs.push({
                    name : 'actions',
                    visible: true,
                    title: 'BROWSE.COLUMN.ACTIONS.NAME',
                    enableFiltering: false,
                    enable: false,
                    enableColumnMenu: false,
                    enableSorting: false,
                    enableHiding: false,
                    cellTemplate : [
                        '<div class="ui-grid-cell-contents">',
                            '<a ',
                                'ng-repeat="actionButton in grid.options.actionButtons" ',
                                'type="button" ',
                                'class="{{actionButton.class}} btn-xs btn-entity-action" ',
                                'translate="{{actionButton.translate}}" ',
                                'translateValues="{{actionButton.translateValues}}" ',
                                'uib-tooltip="{{actionButton.translateTooltip | translate}}" ',
                                'tooltip-placement="left" ',
                                'tooltip-append-to-body="true" ',
                                'ng-show="actionButton.show() === undefined ? true : actionButton.show()"',
                                'ng-click="actionButton.click(row.entity); $event.stopPropagation();">',
                            '</a>',
                        '</div>'
                    ].join('')
                });
            }
    	};


        this.generateEntitySorter = function(sortColumns){
            var sorters = [];

            _.each(sortColumns, function(sortColumn){
                if(sortColumn.colDef){
                    sorters.push(function(entityA, entityB){
                        var field = sortColumn.colDef.field;
                        var valueA = (((entityA.find ? entityA.find(field)[0] :  entityA[field]) || '') + '').toLowerCase();
                        var valueB = (((entityB.find ? entityB.find(field)[0] :  entityB[field]) || '') + '').toLowerCase();

                        var out = 0;
                        if(valueA < valueB){
                            out = -1
                        } else if(valueA > valueB){
                            out = 1
                        }

                        if(sortColumn.sort.direction == 'desc') out = out * -1;

                        return out;
                    });
                }
            });

            return function(entityA, entityB){
                var out = 0;
                _.each(sorters, function(sorter){
                    var current = sorter(entityA, entityB);
                    if(current != 0){
                        out = current;
                        return false;
                    }
                });
                return out;
            };
        };

        this.generateEntityFilter = function(gridOptions){
            var conditions = [];

            _.each(gridOptions.columnDefs, function(columnDef){
                if(!columnDef.field) return;
                if(columnDef.type == 'date' && columnDef.filters){
                    conditions.push(function(entity){
                        var fromDate = helpers.completePartialFromDate(columnDef.filters[0].term);
                        var toDate = helpers.completePartialToDate(columnDef.filters[1].term);
                        var field = columnDef.field;
                        var value = (entity.find ? entity.find(field)[0] : entity[field]) || '';
                        return value >= fromDate && value <= toDate;
                    });
                } else if(columnDef.type == 'string' && columnDef.filter){
                    conditions.push(function(entity){
                        var field = columnDef.field;
                        var value = (((entity.find ? entity.find(field)[0] : entity[field]) || '') + '').toLowerCase();
                        return columnDef.filter.term === undefined || columnDef.filter.term === null || value.indexOf(columnDef.filter.term.toLowerCase()) >= 0;
                    });
                }
            });

            return function(row){
                var out = true;
                _.each(conditions, function(condition){
                    if(!condition(row)){
                        out = false;
                        return false;
                    }
                });
                return out;
            };
        };

        this.mergeNamedObjectArrays = function(existingObjects, toBeMergedObjects){
            var out = _.clone(existingObjects);
            var changed;
            
            while(true){
                changed = false;

                _.each(_.clone(toBeMergedObjects), function(toBeMergedObject){
                    if(toBeMergedObject.insertBefore){
                        var index = _.findIndex(out, function(tab){
                            return tab.name == toBeMergedObject.insertBefore
                        });

                        if(index !== -1){
                            out.splice(index, 0, toBeMergedObject);
                            _.remove(toBeMergedObjects, {name: toBeMergedObject.name});
                            changed = true;
                        }

                    } else if(toBeMergedObject.insertAfter){
                        var index = _.findIndex(out, function(tab){
                            return tab.name == toBeMergedObject.insertAfter;
                        });

                        if(index !== -1){
                            out.splice(index + 1, 0, toBeMergedObject);
                            _.remove(toBeMergedObjects, {name: toBeMergedObject.name});
                            changed = true;
                        }
                    } else {
                        out.push(toBeMergedObject);
                        _.remove(toBeMergedObjects, {name: toBeMergedObject.name});
                        changed = true;
                    }   
                });

                if(!changed) break;
            }

            return out;
        };

    	this.completePartialFromDate = function(date){
            var segments = (date || '').split(/[-:\s\/]+/);
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
            var segments = (date || '').split(/[-:\s\/]+/);
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
            if(data === null) return 'null';
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
			return ('' + text).replace(/([A-Z])/g, '_$1').replace(/-/g, '_').replace(/^_/, '').toUpperCase();
		};


        var lowPriorityCounter = 0;
        var lowPriorityQueue = [];

        $interval(function(){
            if(lowPriorityCounter < 2 && lowPriorityQueue.length > 0){
                lowPriorityQueue.pop().call();
            }
        }, 10);

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
						var url = prefix + offset;
						if(methodName.match(/get|delete/)){
                            if(params !== '') url += '?' + params;
                        } else if(options.queryParams) {
                            url += '?' + helpers.urlEncode(options.queryParams);
                        }
                    
						var out = $q.defer();

                        function call(){
                            if(options.lowPriority) lowPriorityCounter++;

                            if(options.bypassInterceptors){
                                var xhr = $.ajax(url, {
                                    method: methodName.toUpperCase(),
                                    headers: options.headers,
                                    data: methodName.match(/post|put/) ? params : undefined
                                });

                                xhr.then(function(data){
                                    success({data: data})
                                }, function(qXHR, textStatus, errorThrown){
                                    failure({data: errorThrown})
                                });

                                if(options.timeout){
                                    options.timeout.then(function(){
                                        xhr.abort();
                                    });
                                }
                            } else {
                                var args = [url];
                                if(methodName.match(/post|put/)) args.push(params);
                                args.push(options);

        						$http[methodName].apply($http, args).then(success, failure);
                            }
                        }

                        function success(response){
                            out.resolve(response.data);
                            if(options.lowPriority) lowPriorityCounter--;
                        }

                        function failure(response){
                            out.reject(response.data);
                            if(options.lowPriority) lowPriorityCounter--;
                        }

                        if(options.lowPriority){
                            lowPriorityQueue.push(call);
                        } else {
                            call();
                        }

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

            var allMethod = $q.all;
            $q.all = function(){
                var out = allMethod.apply(this, arguments);
                extendPromise(out);
                return out;
            };

	        function extendPromise(promise){
				promise.log = function(){
                    var start = (new Date()).getTime();
		            return this.then(function(data){
                        var end = (new Date()).getTime();
                        var diff = end - start;
		                console.log('(success - ' + diff + ' milliseconds)', data); 
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

        this.mixinPluginMethods = function(name, that){
            _.each(plugins, function(plugin){
              if(plugin.extend && plugin.extend[name]){
                $injector.invoke(plugin.extend[name], that);
              }
            });
        };

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



(function() {
    'use strict';

    var app = angular.module('angularApp');

    var paths = {
    	facilityCycle: instrument: {
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
    		instrument: 'datafile.dataset.investigation.investigationInstruments.instrument'
    	}
    };

    app.service('tcIcatGridBuilder', function(uiGridConstants){

    	this.create = function(icat, entityType){
    		return new IcatQueryBuilder(icat, entityType);
    	};

    	function IcatGridBuilder(icat, entityType){
    		var facility = icat.facility();
    		var facilityName = facility.config().facilityName;
    		var tc = facility.tc();
    		var user = tc.user(facilityName);
    		var cart = user.cart();
    		var pagingConfig = tc.config().paging;
	        var isScroll = pagingConfig.pagingType == 'scroll';
	        var pageSize = isScroll ? pagingConfig.scrollPageSize : pagingConfig.paginationNumberOfRows;
	        var page = 1;
	        var baseConditions = {};

    		var gridOptions;
    		this.setGridOptions = function(_gridOptions){
    			gridOptions = _gridOptions;
    			gridOptions.data = [];
    			gridOptions.appScopeProvider = this;

    			//normalize grid options
    			gridOptions.rowTemplate = '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>';
    			gridOptions.paginationPageSizes = pagingConfig.paginationPageSizes;
		        gridOptions.paginationNumberOfRows = pagingConfig.paginationNumberOfRows;
		        gridOptions.useExternalPagination = true;
		        gridOptions.useExternalSorting = true;
		        gridOptions.useExternalFiltering = true;
		        var enableSelection = gridOptions.enableSelection === true && entityType.match(/^investigation|dataset|datafile$/) !== null;
		        gridOptions.enableSelectAll = false;
		        gridOptions.enableRowSelection = enableSelection;
		        gridOptions.enableRowHeaderSelection = enableSelection;

    			_.each(gridOptions.columnDefs, function(columnDef){
		            if(columnDef.link) {
		                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents" title="TOOLTIP"><a ng-click="$event.stopPropagation();" href="{{grid.appScope.getNextRouteUrl(row.entity)}}">{{row.entity.' + columnDef.field + '}}</a></div>';
		            }

		            if(columnDef.type == 'date'){
		                if(columnDef.field && columnDef.field.match(/Date$/)){
		                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker only-date ng-model="col.filters[1].term" placeholder="To..."></div>';
		                } else {
		                    columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[1].term" placeholder="To..."></div>';
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
		                    saveState();
		                });
		            }

		            if(columnDef.field == 'size'){
		                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
		                columnDef.enableSorting = false;
		                columnDef.enableFiltering = false;
		            }

		            if(columnDef.translateDisplayName){
		                columnDef.displayName = columnDef.translateDisplayName;
		                columnDef.headerCellFilter = 'translate';
		            }

		            if(columnDef.field == 'instrumentNames'){
		                columnDef.cellTemplate = '<div class="ui-grid-cell-contents" ng-if="row.entity.investigationInstruments.length > 1"><span class="glyphicon glyphicon-th-list" uib-tooltip="{{row.entity.instrumentNames}}" tooltip-placement="top" tooltip-append-to-body="true"></span> {{row.entity.firstInstrumentName}}</div><div class="ui-grid-cell-contents" ng-if="row.entity.investigationInstruments.length <= 1">{{row.entity.firstInstrumentName}}</div>';
		            }

		            if(columnDef.sort){
		                if(columnDef.sort.direction.toLowerCase() == 'desc'){
		                    columnDef.sort.direction = uiGridConstants.DESC;
		                } else {
		                    columnDef.sort.direction = uiGridConstants.ASC;
		                }
		            }

		            columnDef.jpqlExpression = columnDef.jpqlExpression || realEntityInstanceName + '.' + columnDef.field;
		            if(columnDef.sort) sortColumns.push(columnDef);
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

    		this.showTabs = function(row) {
	            $rootScope.$broadcast('rowclick', {
	                'type': row.entity.entityType.toLowerCase(),
	                'id' : row.entity.id,
	                facilityName: facility.config()
	            });
	        };

	        this.where = function(field, value){

	        };

    	}

	});

})();

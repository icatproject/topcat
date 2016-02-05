
(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('BrowseFacilitiesModel', BrowseFacilitiesModel);

    BrowseFacilitiesModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage', 'inform'];

    function BrowseFacilitiesModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage, inform){
        var self = this;

        this.configToUIGridOptions = function() {
            var gridOptions = Config.getSiteFacilitiesGridOptions(APP_CONFIG);

            //do the work of transposing
            _.mapValues(gridOptions.columnDefs, function(value) {
                //replace filter condition to one expected by ui-grid
                if (angular.isDefined(value.filter)) {
                    if (angular.isDefined(value.filter.condition) && angular.isString(value.filter.condition)) {
                        value.filter.condition = uiGridConstants.filter[value.filter.condition.toUpperCase()];
                    }
                }

                //replace translate text
                if (angular.isDefined(value.translateDisplayName) && angular.isString(value.translateDisplayName)) {
                    value.displayName = value.translateDisplayName;
                    delete value.translateDisplayName;

                    value.headerCellFilter = 'translate';
                }

                //replace links
                if (angular.isDefined(value.link) && value.link === true) {
                    delete value.link;

                    value.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="$event.stopPropagation();" ui-sref="home.browse.facility.{{grid.appScope.getNextRouteSegment(row)}}({facilityName : \'{{row.entity.name}}\'})">{{row.entity.' + value.field + '}}</a></div>';
                }

                return value;
            });

            return gridOptions;
        };


        this.init = function(facilityObjs, scope, gridOptions) {
            self.facilityObjs = facilityObjs;
            self.scope = scope;
            self.gridOptions = gridOptions;
            self.options = self.configToUIGridOptions();
            self.pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
            self.pageSize = Config.getSitePageSize(APP_CONFIG, self.pagingType); //the number of rows for grid
            self.scrollRowFromEnd = Config.getSiteScrollRowFromEnd(APP_CONFIG, self.pagingType);
            self.paginationPageSizes = Config.getPaginationPageSizes(APP_CONFIG, self.pagingType); //the number of rows for grid
            self.setGridOptions(self.scope.gridOptions);
        };

        this.setGridOptions = function(gridOptions) {
            self.gridOptions = _.extend(gridOptions, {
                data: [],
                enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                columnDefs: self.options.columnDefs,
                enableFiltering: self.options.enableFiltering,
                appScopeProvider: self.scope,
                enableRowSelection: false,
                enableRowHeaderSelection: false,
                paginationPageSizes: self.paginationPageSizes,
                rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
            });
        };

        this.getPage = function() {
            ConfigUtils.getLoggedInFacilitiesFromConfig(self.facilityObjs, $sessionStorage.sessions).then(function (data){
                    self.gridOptions.data = data;
                }, function(error){
                    inform.add(error, {
                        'ttl': 4000,
                        'type': 'danger'
                    });
                }
            );
        };


        this.getNextRouteSegment = function(row, currentEntityType) {
            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, row.entity.name);
            var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);
            this.nextRouteSegment = nextRouteSegment;

            return nextRouteSegment;
        };

    }
})();

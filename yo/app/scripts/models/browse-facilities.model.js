(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('BrowseFacilitiesModel', BrowseFacilitiesModel);

    BrowseFacilitiesModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage', '$log'];

    function BrowseFacilitiesModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage, $log){
        var self = this;

        this.configToUIGridOptions = function() {
            //$log.debug('configToUIGridOptions called');
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
            $log.debug('init self.gridOptions', self.gridOptions);

            self.facilityObjs = facilityObjs;
            self.scope = scope;
            self.gridOptions = gridOptions;

            self.options = self.configToUIGridOptions();
            self.paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid

            self.setGridOptions(self.scope.gridOptions);

            /*ConfigUtils.getLoggedInFacilitiesFromConfig(facilityObjs, $sessionStorage.sessions).then(function (data){
                    $log.debug('promise data', data);
                    gridOptions.data = data;
                }, function(){
                    throw new Error('Unable to retrieve logged in facilitites');
                }
            );

            this.gridOptions = gridOptions;*/
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

            $log.debug('setGridOptions self.gridOptions', self.gridOptions);
        };

        this.getPage = function() {
            $log.debug('getPage self.gridOptions', self.gridOptions);

            ConfigUtils.getLoggedInFacilitiesFromConfig(self.facilityObjs, $sessionStorage.sessions).then(function (data){
                    $log.debug('promise data', data);
                    self.gridOptions.data = data;
                }, function(error){
                    $log.debug('Unable to retrieve data:' + error);
                    throw new Error('Unable to retrieve logged in facilitites');
                }
            );
        };


        this.getNextRouteSegment = function(row, currentEntityType) {
            //$log.debug('getNextRouteSegment called');
            //$log.debug('row', row);
            //count++;
            //$log.debug('count', count);

            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, row.entity.name);
            var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);
            this.nextRouteSegment = nextRouteSegment;

            return nextRouteSegment;
        };

    }
})();

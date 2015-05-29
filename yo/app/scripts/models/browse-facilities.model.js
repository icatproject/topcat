'use strict';

angular
    .module('angularApp')
    .factory('BrowseFacilitiesModel', BrowseFacilitiesModel);

BrowseFacilitiesModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage'];

function BrowseFacilitiesModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage){
    return {
        gridOptions : {},

        /**
         * This function transpose the site config file to settings used by ui-grid
         *
         * @return {[type]} [description]
         */
        configToUIGridOptions : function() {
            //console.log('configToUIGridOptions called');
            var gridOptions = Config.getSiteFacilitiesGridOptions(APP_CONFIG);

            //do the work of transposing
            _.mapValues(gridOptions.columnDefs, function(value) {
                //replace filter condition to one expected by ui-grid
                if (angular.isDefined(value.filter.condition) && angular.isString(value.filter.condition)) {
                    value.filter.condition = uiGridConstants.filter[value.filter.condition.toUpperCase()];
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

                    value.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="$event.stopPropagation();" ui-sref="home.browse.facilities.{{grid.appScope.getNextRouteSegment(row)}}({facilityName : \'{{row.entity.name}}\'})">{{row.entity.' + value.field + '}}</a></div>';
                }

                return value;
            });

            return gridOptions;
        },


        init : function(facilityObjs, scope) {
            var options = this.configToUIGridOptions();
            var paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid

            var gridOptions = {
                data: [],
                enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                columnDefs: options.columnDefs,
                enableFiltering: options.enableFiltering,
                appScopeProvider: scope,
                enableRowSelection: false,
                enableRowHeaderSelection: false,
                paginationPageSizes: paginationPageSizes,
                rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
            };

            gridOptions.onRegisterApi = function(gridApi) {
                scope.gridApi = gridApi;
            };

            ConfigUtils.getLoggedInFacilitiesFromConfig(facilityObjs, $sessionStorage.sessions).then(function (data){
                    console.log('promise data', data);
                    gridOptions.data = data;
                }, function(){
                    throw new Error('Unable to retrieve logged in facilitites');
                }
            );

            this.gridOptions = gridOptions;
        },


        getNextRouteSegment: function(row, currentEntityType) {
            //console.log('getNextRouteSegment called');
            //console.log('row', row);
            //count++;
            //console.log('count', count);

            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, row.entity.name);
            var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);
            this.nextRouteSegment = nextRouteSegment;

            return nextRouteSegment;
        }



    };
}


(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('CartModel', CartModel);

    CartModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage', 'Cart', '$log'];

    function CartModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage, Cart, $log){ //jshint ignore: line
        return {
            gridOptions : {},

            /**
             * This function transpose the site config file to settings used by ui-grid
             *
             * @return {[type]} [description]
             */
            configToUIGridOptions : function() {
                //$log.debug('configToUIGridOptions called');
                var gridOptions = Config.getSiteCartGridOptions(APP_CONFIG);

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

                    if(angular.isDefined(value.field) && value.field === 'size') {
                        value.cellTemplate = '<div class="ui-grid-cell-contents"><span load-size ng-model="row">{{ row.entity.size | bytes }}</span></div>';
                    }

                    if(angular.isDefined(value.field) && value.field === 'availability') {
                        value.cellTemplate = '<div class="ui-grid-cell-contents"><span load-availability ng-model="row">{{ row.entity.availability }}</span></div>';
                    }

                    return value;
                });

                //add a delete column
                gridOptions.columnDefs.push({
                    name : 'action',
                    displayName : 'Action',
                    translateDisplayName: 'CART.COLUMN.ACTION',
                    enableFiltering: false,
                    enable: false,
                    enableColumnMenu: false,
                    enableSorting: false,
                    enableHiding: false,
                    cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.removeItem(row)">Remove</a></div>'
                });

                return gridOptions;
            },


            init : function(scope) {
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
                    //rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
                };

                gridOptions.onRegisterApi = function(gridApi) {
                    scope.gridApi = gridApi;
                };

                /*ConfigUtils.getLoggedInFacilitiesFromConfig(facilityObjs, $sessionStorage.sessions).then(function (data){
                        $log.debug('promise data', data);
                        gridOptions.data = data;
                    }, function(){
                        throw new Error('Unable to retrieve logged in facilitites');
                    }
                );*/

                gridOptions.data = Cart.getLoggedInItems($sessionStorage);

                /*if (gridOptions.data.length === 0) {
                    scope.isEmpty = true;
                } else {
                    scope.isEmpty = false;
                }*/

                //$log.debug('gridOptions.data', gridOptions.data);

                this.gridOptions = gridOptions;



            },

            refreshData : function() {
                this.gridOptions.data = Cart.getLoggedInItems($sessionStorage);

                /*if (this.gridOptions.data.length === 0) {
                    scope.isEmpty = true;
                } else {
                    scope.isEmpty = false;
                }*/
            },

            removeItem : function(row) {
                Cart.removeItem(row.entity.facilityName, row.entity.entityType, row.entity.id);

                var index = this.gridOptions.data.indexOf(row.entity);
                this.gridOptions.data.splice(index, 1);
            },

            removeAllItems : function() {
                Cart.removeAllItems();
                this.gridOptions.data = [];
            }

            /*getNextRouteSegment: function(row, currentEntityType) {
                //$log.debug('getNextRouteSegment called');
                //$log.debug('row', row);
                //count++;
                //$log.debug('count', count);

                var structure = Config.getHierarchyByFacilityName(APP_CONFIG, row.entity.name);
                var nextRouteSegment = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);
                this.nextRouteSegment = nextRouteSegment;

                return nextRouteSegment;
            }*/



        };
    }
})();

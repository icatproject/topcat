(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('CartModel', CartModel);

    CartModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage', 'Cart', '$log'];

    function CartModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage, Cart, $log){ //jshint ignore: line
        var self = this;

        /**
         * This function transpose the site config file to settings used by ui-grid
         *
         * @return {[type]} [description]
         */
        function configToUIGridOptions() {
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
                    value.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}" spinner-key="spinner-size-{{row.uid}}" class="grid-cell-spinner"></span><span load-size ng-model="row">{{ row.entity.size | bytes }}</span></div>';
                }

                if(angular.isDefined(value.field) && value.field === 'availability') {
                    value.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}" spinner-key="spinner-status-{{row.uid}}" class="grid-cell-spinner"></span><span load-availability ng-model="row">{{ row.entity.availability }}</span></div>';
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
        }

        function setGridOptions(gridOptions) {
            self.gridOptions = _.extend(gridOptions, {
                data: [],
                enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                columnDefs: self.options.columnDefs,
                enableFiltering: self.options.enableFiltering,
                enableRowSelection: false,
                enableRowHeaderSelection: false,
                paginationPageSizes: self.paginationPageSizes,
                //rowTemplate: '<div ng-click="grid.appScope.showTabs(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" ui-grid-cell></div>'
            });
        }

        this.init = function(scope) {
            self.scope = scope;
            self.options = configToUIGridOptions();
            self.paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid

            setGridOptions(scope.gridOptions);
        };

        this.setCart = function() {
            self.gridOptions.data = Cart.getItems();
        };

        this.refreshData = function() {
            self.gridOptions.data = Cart.getItems();
        };

        this.removeItem = function(row) {
            Cart.removeItem(row.entity.facilityName, row.entity.entityType, row.entity.entityId);
        };

        this.removeAllItems = function() {
            $log.debug('remove all called');
            Cart.removeAllItems();
            self.gridOptions.data = [];
        };
    }
})();

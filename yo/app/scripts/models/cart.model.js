(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('CartModel', CartModel);

    CartModel.$inject = ['APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'uiGridConstants', '$sessionStorage', 'Cart', '$translate'];

    function CartModel(APP_CONFIG, Config, ConfigUtils, RouteUtils, uiGridConstants, $sessionStorage, Cart, $translate){
        var self = this;

        /**
         * This function converts the grid options in the config file to options used by ui-grid
         *
         * @return {[type]} [description]
         */
        function configToUIGridOptions() {
            var gridOptions = Config.getSiteCartGridOptions(APP_CONFIG);

            //add a delete column
            gridOptions.columnDefs.push({
                name : 'actions',
                translateDisplayName: 'CART.COLUMN.ACTIONS',
                enableFiltering: false,
                enable: false,
                enableSorting: false,
                cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.removeItem(row)" translate="CART.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" tooltip="' + $translate.instant('CART.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></div>'
            });

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
                paginationPageSizes: self.paginationPageSizes
            });
        }

        this.init = function(scope) {
            self.scope = scope;
            self.options = configToUIGridOptions();
            self.pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
            self.pageSize = Config.getSitePageSize(APP_CONFIG, self.pagingType); //the number of rows for grid
            self.scrollRowFromEnd = Config.getSiteScrollRowFromEnd(APP_CONFIG, self.pagingType);
            self.paginationPageSizes = Config.getPaginationPageSizes(APP_CONFIG, self.pagingType); //the number of rows for grid
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
            Cart.removeAllItems();
            self.gridOptions.data = [];
        };
    }
})();

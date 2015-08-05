'use strict';

angular
    .module('angularApp')
    .factory('DownloadModel', DownloadModel);

DownloadModel.$inject = ['$rootScope', 'APP_CONFIG', 'Config', 'uiGridConstants', 'TopcatService', '$sessionStorage', '$log'];

function DownloadModel($rootScope, APP_CONFIG, Config, uiGridConstants, TopcatService, $sessionStorage, $log){  //jshint ignore: line


    return {
        gridOptions : {},

        configToUIGridOptions : function() {
            //$log.debug('configToUIGridOptions called');
            var gridOptions = Config.getSiteMyDownloadGridOptions(APP_CONFIG);

            //do the work of transposing
            _.mapValues(gridOptions.columnDefs, function(value) {
                //replace filter condition to one expected by ui-grid

                return value;
            });

            //add a Download column
            gridOptions.columnDefs.push({
                name : 'download',
                displayName : 'Download',
                translateDisplayName: 'CART.COLUMN.DOWNLOAD',
                enableFiltering: false,
                enable: false,
                enableColumnMenu: false,
                enableSorting: false,
                enableHiding: false,
                cellTemplate : '<div class="ui-grid-cell-contents"><span ng-bind-html="grid.appScope.getDownloadUrl(row)"></span></div>'
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


            _.each($sessionStorage.sessions, function(session, key) {
                var facility = Config.getFacilityByName(APP_CONFIG, key);

                TopcatService.getMyDownloads(facility, session.userName).then(function(data) {
                    $log.debug('results', data.data);
                    gridOptions.data = gridOptions.data.concat(data.data);
                }) ;
            });

            this.gridOptions = gridOptions;
        }

    };
}


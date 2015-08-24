'use strict';

angular
    .module('angularApp')
    .factory('DownloadModel', DownloadModel);

DownloadModel.$inject = ['$rootScope', '$state', 'APP_CONFIG', 'Config', 'uiGridConstants', 'TopcatService', '$sessionStorage', '$compile', '$log'];

function DownloadModel($rootScope, $state, APP_CONFIG, Config, uiGridConstants, TopcatService, $sessionStorage, $compile, $log){  //jshint ignore: line
    /**
     * build download url html
     * @param  {[type]} data [description]
     * @return {[type]}      [description]
     */
    function getDownloadUrl(data) {
        var html = '';

        if (data.transport === 'https') {
            if (data.status === 'COMPLETE') {
                html = '<a href="' + data.transportUrl + '/ids/getData?preparedId=' + data.preparedId + '&outname=' + data.fileName + '">Download</a>';
            } else {
                html = '<span class="not-active">Download</span>';
            }
        } else if (data.transport === 'globus') {
            var route = $state.href('globus-faq');
            html ='<a href="' + route + '">Download Via Globus</a>';
        }

        return html;
    }

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
                displayName : 'Actions',
                translateDisplayName: 'CART.COLUMN.DOWNLOAD',
                enableFiltering: false,
                enable: false,
                enableColumnMenu: false,
                enableSorting: false,
                enableHiding: false,
                cellTemplate : '<div class="ui-grid-cell-contents"><span ng-bind-html="row.entity.downloadLink"></span> <span class="remove-download"><a ng-click="grid.appScope.remove(row.entity, grid.renderContainers.body.visibleRowCache.indexOf(row))">Remove</a></span></div>'
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

                    //
                    _.each(data.data, function(entity) {
                        entity.downloadLink = getDownloadUrl(entity);
                    });

                    gridOptions.data = gridOptions.data.concat(data.data);
                }) ;
            });

            this.gridOptions = gridOptions;
        }

    };
}


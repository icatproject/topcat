'use strict';

angular
    .module('angularApp')
    .service('DownloadModel', DownloadModel);

DownloadModel.$inject = ['$rootScope', '$state', 'APP_CONFIG', 'Config', 'uiGridConstants', 'TopcatService', '$sessionStorage', '$compile', '$translate'];

function DownloadModel($rootScope, $state, APP_CONFIG, Config, uiGridConstants, TopcatService, $sessionStorage, $compile, $translate){
    var self = this;

    /**
     * build download url html
     * @param  {[type]} data [description]
     * @return {[type]}      [description]
     */
    function getDownloadUrl(data) {
        var html = '';

        if (data.transport === 'https') {
            if (data.status === 'COMPLETE') {
                html = '<a href="' + data.transportUrl + '/ids/getData?preparedId=' + data.preparedId + '&outname=' + data.fileName + '" translate="DOWNLOAD.ACTIONS.LINK.HTTP_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.HTTP_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
            } else {
                html = '<span class="inline-block" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.NON_ACTIVE_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"><button translate="DOWNLOAD.ACTIONS.LINK.NON_ACTIVE_DOWNLOAD.TEXT" class="btn btn-primary btn-xs disabled"></button></span>';
            }
        } else if (data.transport === 'globus') {
            html ='<a href="' + $state.href('globus-help') + '" translate="DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
        } else if (data.transport === 'smartclient') {
            if (data.status === 'COMPLETE') {
                html ='<a ng-click="grid.appScope.smartClientModal()" translate="DOWNLOAD.ACTIONS.LINK.SMARTCLIENT_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.SMARTCLIENT_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
            }
        }

        return html;
    }


    function configToUIGridOptions() {
        var gridOptions = Config.getSiteMyDownloadGridOptions(APP_CONFIG);

        //add a Download column
        gridOptions.columnDefs.push({
            name : 'actions',
            translateDisplayName: 'DOWNLOAD.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableColumnMenu: false,
            enableSorting: false,
            enableHiding: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><span bind-html-compile="row.entity.downloadLink"></span> <span class="remove-download"><a ng-click="grid.appScope.remove(row.entity, grid.renderContainers.body.visibleRowCache.indexOf(row))" translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></span></div>'
        });

        //do the work of transposing
        _.mapValues(gridOptions.columnDefs, function(value) {
            //replace filter condition to one expected by ui-grid
            if (angular.isDefined(value.filter)) {
                if (angular.isDefined(value.filter.condition) && angular.isString(value.filter.condition)) {
                    value.filter.condition = uiGridConstants.filter[value.filter.condition.toUpperCase()];
                }
            }

            //default type to string if not defined
            if (! angular.isDefined(value.type)) {
                value.type = 'string';
            }

            //replace translate text
            if (angular.isDefined(value.translateDisplayName) && angular.isString(value.translateDisplayName)) {
                value.displayName = value.translateDisplayName;
                delete value.translateDisplayName;

                value.headerCellFilter = 'translate';
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
            gridMenuShowHideColumns: false,
            paginationPageSizes: self.paginationPageSizes
        });
    }

    this.init = function(scope) {
        this.scope = scope;

        self.options = configToUIGridOptions();
        self.pagingType = Config.getSitePagingType(APP_CONFIG); //the pagination type. 'scroll' or 'page'
        self.pageSize = Config.getSitePageSize(APP_CONFIG, self.pagingType); //the number of rows for grid
        self.scrollRowFromEnd = Config.getSiteScrollRowFromEnd(APP_CONFIG, self.pagingType);
        self.paginationPageSizes = Config.getPaginationPageSizes(APP_CONFIG, self.pagingType); //the number of rows for grid

        setGridOptions(scope.gridOptions);
    };

    this.getPage = function() {
        _.each($sessionStorage.sessions, function(session, key) {
            var facility = Config.getFacilityByName(APP_CONFIG, key);

            TopcatService.getMyDownloads(facility, session.userName).then(function(data) {
                _.each(data.data, function(entity) {
                    entity.downloadLink = getDownloadUrl(entity);
                });

                self.gridOptions.data = self.gridOptions.data.concat(data.data);
            }) ;
        });
    };

    this.refresh = function() {
        self.gridOptions.data = [];
        self.getPage();
    };
}


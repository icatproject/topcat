'use strict';

angular
    .module('angularApp')
    .service('DownloadModel', DownloadModel);

DownloadModel.$inject = ['$rootScope', '$state', 'APP_CONFIG', 'Config', 'uiGridConstants', 'TopcatService', '$sessionStorage', '$compile', '$log'];

function DownloadModel($rootScope, $state, APP_CONFIG, Config, uiGridConstants, TopcatService, $sessionStorage, $compile, $log){  //jshint ignore: line
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


    function configToUIGridOptions() {
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
        this.scope = scope;

        self.options = configToUIGridOptions();
        self.paginationPageSizes = Config.getSiteConfig(APP_CONFIG).paginationPageSizes; //the number of rows for grid

        setGridOptions(scope.gridOptions);
    };

    this.getPage = function() {
        _.each($sessionStorage.sessions, function(session, key) {
            var facility = Config.getFacilityByName(APP_CONFIG, key);

            TopcatService.getMyDownloads(facility, session.userName).then(function(data) {
                $log.debug('results', data.data);

                //
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


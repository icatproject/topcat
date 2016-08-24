
(function(){
    'use strict';

    var app = angular.module('topcat');

    app.controller('DownloadsController', function($state, $scope, $translate, $uibModalInstance, $q, $interval, tc, uiGridConstants, helpers){
        var that = this;
        var pagingConfig = tc.config().paging;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });
        this.isScroll = pagingConfig.pagingType == 'scroll';
        this.gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().myDownloads.gridOptions);
        helpers.setupTopcatGridOptions(this.gridOptions, 'download');
        this.gridOptions.useExternalPagination =  false;
        this.gridOptions.useExternalSorting =  false;
        this.gridOptions.useExternalFiltering =  false;
        this.gridOptions.columnDefs.push({
            name : 'actions',
            title: 'DOWNLOAD.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableColumnMenu: false,
            enableSorting: false,
            enableHiding: false,
            cellTemplate : [
                '<div class="ui-grid-cell-contents">',
                    '<span bind-html-compile="row.entity.downloadLink"></span>',
                    '<span class="remove-download">', 
                        '<a ng-click="grid.appScope.remove(row.entity)" translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>',
                    '</span>',
                '</div>'
            ].join('')
        });


        var refreshPromise = $interval(refresh, 1000 * 60);
        timeout.promise.then(function(){ $interval.cancel(refreshPromise); });
        refresh();

        function refresh(){
            var promises = [];
            that.gridOptions.data = [];
            _.each(tc.userFacilities(), function(facility){
                var smartclient = facility.smartclient();
                var smartclientPing = smartclient.isEnabled() ? smartclient.ping(timeout.promise) : undefined;

                promises.push(facility.user().downloads("where download.isDeleted = false").then(function(results){
                    _.each(results, function(download){
                        download.downloadLink = getDownloadUrl(download);
                    });
                    that.gridOptions.data = _.flatten([that.gridOptions.data, results]);
                }));
            });

            $q.all(promises).then(function(){
                if(that.gridOptions.data.length == 0){
                    $uibModalInstance.dismiss('cancel');
                }
            });
        };
        
    
        this.remove = function(download){
            var data = [];
            _.each(that.gridOptions.data, function(currentDownload){
                if(currentDownload.id != download.id) data.push(currentDownload);
            });
            that.gridOptions.data = data;
            download.delete().then(function(){
                if(that.gridOptions.data.length == 0){
                    $uibModalInstance.dismiss('cancel');
                }
            });
        };

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        function getDownloadUrl(data) {
            var html = '';

            if (data.transport === 'https') {
                if (data.status === 'COMPLETE') {
                    html = '<a href="' + data.transportUrl + '/ids/getData?preparedId=' + data.preparedId + '&outname=' + data.fileName + '" translate="DOWNLOAD.ACTIONS.LINK.HTTP_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.HTTP_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
                } else {
                    html = '<span class="inline-block" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.NON_ACTIVE_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"><button translate="DOWNLOAD.ACTIONS.LINK.NON_ACTIVE_DOWNLOAD.TEXT" class="btn btn-primary btn-xs disabled"></button></span>';
                }
            } else if (data.transport === 'globus') {
                html ='<a href="' + $state.href('globus-help') + '" target="_blank" translate="DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
            } else if (data.transport === 'smartclient') {
                if (data.status === 'COMPLETE') {
                    html ='<a href="' + $state.href('smartclient-help') + '" target="_blank" translate="DOWNLOAD.ACTIONS.LINK.START_SMARTCLIENT_SERVER.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.START_SMARTCLIENT_SERVER.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
                }
            }

            return html;
        }

    });

})();

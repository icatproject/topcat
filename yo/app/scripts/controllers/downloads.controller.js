
(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('DownloadsController', function($translate, $uibModalInstance, $q, tc, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
        this.isScroll = pagingConfig.pagingType == 'scroll';
        this.gridOptions = _.merge({
            data: [],
            appScopeProvider: this,
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            enableRowSelection: false,
            enableRowHeaderSelection: false,
            gridMenuShowHideColumns: false,
            pageSize: !this.isScroll ? pagingConfig.paginationNumberOfRows : null,
            paginationPageSizes: pagingConfig.paginationPageSizes
        }, tc.config().myDownloadGridOptions);
        _.each(this.gridOptions.columnDefs, function(columnDef){
            if (columnDef.filter.condition) {
                columnDef.filter.condition = uiGridConstants.filter[columnDef.filter.condition.toUpperCase()];
            }
        });
        this.gridOptions.columnDefs.push({
            name : 'actions',
            translateDisplayName: 'DOWNLOAD.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableColumnMenu: false,
            enableSorting: false,
            enableHiding: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><span bind-html-compile="row.entity.downloadLink"></span> <span class="remove-download"><a ng-click="grid.appScope.remove(row.entity)" translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></span></div>'
        });


        this.refresh = function(){
            var promises = [];
            that.gridOptions.data = [];
            _.each(tc.userFacilities(), function(facility){
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
        this.refresh();

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

        this.smartClientModal = function() {
            $uibModal.open({
                templateUrl: 'views/smartclient-download-modal.html',
                controller: 'SmartClientDownloadModalController as sc',
            });
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
                html ='<a href="' + $state.href('globus-help') + '" translate="DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.GLOBUS_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
            } else if (data.transport === 'smartclient') {
                if (data.status === 'COMPLETE') {
                    html ='<a ng-click="grid.appScope.smartClientModal()" translate="DOWNLOAD.ACTIONS.LINK.SMARTCLIENT_DOWNLOAD.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('DOWNLOAD.ACTIONS.LINK.SMARTCLIENT_DOWNLOAD.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a>';
                }
            }

            return html;
        }

    });


    app.controller('SmartClientDownloadModalController', function($modalInstance) {
        var sc = this;

        sc.ok = function() {
            $modalInstance.dismiss('cancel');
        };
    });

})();


(function(){
    'use strict';

    var app = angular.module('angularApp');

    app.controller('CartController', function($translate, $uibModalInstance, $uibModal, $q, $scope, $rootScope, tc, uiGridConstants){
        var that = this;
        var pagingConfig = tc.config().paging;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });
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
        }, tc.config().cartGridOptions);
        _.each(this.gridOptions.columnDefs, function(columnDef){
            if (columnDef.filter.condition) {
                columnDef.filter.condition = uiGridConstants.filter[columnDef.filter.condition.toUpperCase()];
            }
            if(columnDef.translateDisplayName){
                columnDef.displayName = columnDef.translateDisplayName;
                columnDef.headerCellFilter = 'translate';
            }

            if(columnDef.field === 'size') {
                columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
            }

            if(columnDef.field === 'status') {
               columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.status === undefined" class="grid-cell-spinner"></span><span>{{"CART.STATUS." + row.entity.status | translate}}</span></div>';
            }

        });
        this.gridOptions.columnDefs.push({
            name : 'actions',
            translateDisplayName: 'CART.COLUMN.ACTIONS',
            enableFiltering: false,
            enable: false,
            enableSorting: false,
            cellTemplate : '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.remove(row.entity)" translate="CART.ACTIONS.LINK.REMOVE.TEXT" class="btn btn-primary btn-xs" uib-tooltip="' + $translate.instant('CART.ACTIONS.LINK.REMOVE.TOOLTIP.TEXT') + '" tooltip-placement="left" tooltip-append-to-body="true"></a></div>'
        });
        this.totalSize = 0;

        _.each(tc.userFacilities(), function(facility){
            facility.user().cart(timeout.promise).then(function(cart){
                that.gridOptions.data = _.flatten([that.gridOptions.data, cart.cartItems]);
                _.each(cart.cartItems, function(cartItem){
                    cartItem.getSize(timeout.promise).then(function(size){
                        that.totalSize = that.totalSize + size;
                    });
                    cartItem.getStatus(timeout.promise);
                });
            });
        });

        this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        this.remove = function(cartItem){
            var data = [];
            _.each(that.gridOptions.data, function(currentCartItem){
                if(currentCartItem.id != cartItem.id) data.push(currentCartItem);
            });
            that.gridOptions.data = data;
            cartItem.delete().then(function(){
                if(that.gridOptions.data.length == 0){
                    $uibModalInstance.dismiss('cancel');
                }
            });
        };

        this.removeAll = function(){
            var promises = [];
            _.each(tc.userFacilities(), function(facility){
                promises.push(facility.user().deleteAllCartItems(timeout.promise));
            });
            $q.all(promises).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

        this.download = function(){
            $uibModal.open({
                templateUrl : 'views/download-cart.html',
                controller: "DownloadCartController as downloadCartController",
                size : 'lg'
            })
        };

    });

})();

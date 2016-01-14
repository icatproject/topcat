
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, $scope, $state, $timeout, tc){
    	var that = this;
        var page = 1;
        var gridApi;
        var pageSize = 10;
        var filters = {};
    	this.username = "";
    	this.password = "";
    	this.loggedIn = false;
    	this.facilities = tc.adminFacilities();

        this.gridOptions = _.merge({
            data: [],
            appScopeProvider: this,
            enableFiltering: true,
            enableSelectAll: false,
            enableRowSelection: false,
            enableRowHeaderSelection: false,
            infiniteScrollDown: true,
            useExternalPagination: true,
            useExternalFiltering: true
        }, tc.config().adminGridOptions);

        _.each(this.gridOptions.columnDefs, function(columnDef){
            columnDef.enableSorting = false;
            columnDef.enableHiding = false;
            columnDef.enableColumnMenu = false;
        });

        if($state.params.facilityName == ''){
            $state.go('admin', {facilityName: this.facilities[0].config().facilityName});
            return;
        }

        var admin = tc.admin($state.params.facilityName);

        function updateScroll(resultCount){
            $timeout(function(){
                var isMore = resultCount == pageSize;
                if(page == 1) gridApi.infiniteScroll.resetScroll(false, isMore);
                gridApi.infiniteScroll.dataLoaded(false, isMore);
            });
        }

        function getPage(){
            return admin.downloads(_.merge({page: page, pageSize: pageSize}, filters));
        }

    	this.gridOptions.onRegisterApi = function(_gridApi) {
            gridApi = _gridApi;

            getPage().then(function(downloads){
                that.gridOptions.data = downloads;
                updateScroll(downloads.length);
            });

            gridApi.core.on.filterChanged($scope, function() {
                page = 1;

                filters = {};

                _.each(that.gridOptions.columnDefs, function(columnDef){
                    if(columnDef.type == 'date' && columnDef.filters){
                        var from = columnDef.filters[0].term || '';
                        var to = columnDef.filters[1].term || '';
                        if(from.match(/^\d\d\d\d-\d\d-\d\d$/) && to.match(/^\d\d\d\d-\d\d-\d\d$/)){
                            filters[columnDef.field + "From"] = from;
                            filters[columnDef.field + "To"] = to;
                        }
                    } else if(columnDef.type == 'string' && columnDef.filter && columnDef.filter.term) {
                        filters[columnDef.field] = columnDef.filter.term
                    }
                });

                getPage().then(function(downloads){
                    that.gridOptions.data = downloads;
                    updateScroll(downloads.length);
                });
            });

            gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                page++;
                getPage().then(function(downloads){
                    _.each(downloads, function(result){ that.gridOptions.data.push(download); });
                    updateScroll(downloads.length);
                });
            });

        };

    });

})();

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, $scope, $state, tc){
    	var that = this;
    	this.username = "";
    	this.password = "";
    	this.loggedIn = false;
    	this.facilities = tc.adminFacilities();

        if($state.params.facilityName == ''){
            $state.go('admin', {facilityName: this.facilities[0].config().facilityName});
        }

        this.gridOptions = _.merge({
    		data: [],
    		appScopeProvider: this,
    		enableSelectAll: false,
    		enableRowSelection: false,
    		enableRowHeaderSelection: false
    	}, tc.config().adminGridOptions);

    	_.each(this.gridOptions.columnDefs, function(columnDef){
    		columnDef.enableSorting = false;
    		columnDef.enableHiding = false;
    		columnDef.enableColumnMenu = false;
    	});

    	this.gridOptions.onRegisterApi = function(gridApi) {
            
            gridApi.core.on.filterChanged($scope, function() {

            });

            gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {

            });

            gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {

            });

        };

    });

})();
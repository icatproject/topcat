
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, $scope, inform, tc){
    	var that = this;
    	this.username = "";
    	this.password = "";
    	this.loggedIn = false;
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


    	this.login = function(){
    		tc.admin(this.username, this.password).downloads().then(function(downloads){
    			that.gridOptions.data = downloads;
    			that.loggedIn = true;
    		}, function(){
    			inform.add($translate.instant('ADMIN.LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE'), {
                    'ttl': 4000,
                    'type': 'danger'
                });
                that.password = '';
    		});
    	};

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

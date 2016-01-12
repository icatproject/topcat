
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, inform, tc){
    	var that = this;
    	this.username = "";
    	this.password = "";
    	this.loggedIn = false;
    	this.gridOptions = _.merge({data: [], appScopeProvider: this}, tc.config().adminGridOptions);

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

    });

})();

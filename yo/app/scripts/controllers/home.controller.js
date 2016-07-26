(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('HomeController', function ($rootScope, $scope, tc) {

    	var that = this;
    	this.tabs = tc.registeredTabs();
    	var stopListeningForTabChanges =  $rootScope.$on('tab:change', function(){
            that.tabs = tc.registeredTabs();
            $scope.$apply();
        });
    	$scope.$on('$destroy', stopListeningForTabChanges);

        if (!$scope.random) {
            $scope.random = Math.round(Math.random()*10000);
        }

        
    });
    
})();

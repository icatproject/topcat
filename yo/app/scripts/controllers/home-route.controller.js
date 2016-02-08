(function() {
	'use strict';

    var app = angular.module('angularApp');

    app.controller('HomeRouteController', function ($state, tc){
    	var state = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
        $state.go(state);
    });
})();

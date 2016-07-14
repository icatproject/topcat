
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($state, tc){

    	this.facilities = tc.adminFacilities();

      if($state.params.facilityName == ''){
          $state.go('admin.downloads', {facilityName: this.facilities[0].config().name});
          return;
      }
      
    });

})();
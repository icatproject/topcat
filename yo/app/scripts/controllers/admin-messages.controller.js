
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminMessagesController', function($scope, $state, $timeout, $q, tc, inform){
    	var that = this;
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){
          timeout.resolve();
      });

      this.serviceStatus = {};
      this.maintenanceMode = {};
      
      tc.getConfVar('serviceStatus').then(function(serviceStatus){
        that.serviceStatus = serviceStatus;
      });

      tc.getConfVar('maintenanceMode').then(function(maintenanceMode){
        that.maintenanceMode = maintenanceMode;
      });

      var admin = tc.adminFacilities()[0].admin();
      this.save = function(){
        var promises = [];

        promises.push(admin.setConfVar('serviceStatus', this.serviceStatus));
        promises.push(admin.setConfVar('maintenanceMode', this.maintenanceMode));

        $q.all(promises).then(function(){
          inform.add("Saved", {
              'ttl': 1500,
              'type': 'success'
          });
        });

      };

    });

})();
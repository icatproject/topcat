
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminMessagesController', function($scope, $state, $timeout, $q, tc, inform){
    	var that = this;
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){
          timeout.resolve();
      });

      var admin = tc.admin($state.params.facilityName);
      admin.getConfVar('serviceStatus').then(function(serviceStatus){
        that.serviceStatus = serviceStatus;
      });


      this.save = function(){
        admin.setConfVar('serviceStatus', this.serviceStatus).then(function(){
          inform.add("Saved", {
              'ttl': 1500,
              'type': 'success'
          });
        });
      };

    });

})();
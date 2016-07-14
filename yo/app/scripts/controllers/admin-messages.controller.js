
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminMessagesController', function($scope, $state, $timeout, $q, tc){
    	var that = this;
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){
          timeout.resolve();
      });


      var admin = tc.admin($state.params.facilityName);

    });

})();
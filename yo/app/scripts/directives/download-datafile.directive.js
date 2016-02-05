(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.directive('downloadDatafile', function(){
        return {
            restrict: 'E',
            templateUrl: 'views/download-datafile.directive.html',
            controller: 'DownloadDatafileController',
            controllerAs: 'downloadDatafileController'
        };
    });

    app.controller('DownloadDatafileController', ['$stateParams', 'APP_CONFIG', '$sessionStorage', '$scope', function($stateParams, APP_CONFIG, $sessionStorage, $scope){
        var facilityName = $stateParams.facilityName;
        var facility = APP_CONFIG.facilities[facilityName];
        var idsUrl = facility.idsUrl;
        var sessionId = $sessionStorage.sessions[facilityName].sessionId;
        var datafile = $scope.row.entity;
        var id = datafile.id;
        var name = datafile.location.replace(/^.*\//, '');
        this.url = idsUrl + 
            '/ids/getData?sessionId=' + encodeURIComponent(sessionId) +
            '&datafileIds=' + id +
            '&compress=false' +
            '&zip=false' +
            '&outfile=' + encodeURIComponent(name);
    }]);

})();
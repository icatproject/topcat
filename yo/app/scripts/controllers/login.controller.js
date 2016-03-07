(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('LoginController', function($translate, $state, $sessionStorage, inform, deviceDetector, tc){
        var that = this;
        this.isFirefox = deviceDetector.browser == 'firefox'
        this.isIE9 = deviceDetector.browser == 'ie' && deviceDetector.browser_version <= 9; 
        this.facilities = tc.facilities();
        this.userFacilities = tc.userFacilities();
        this.nonUserFacilities = tc.nonUserFacilities();
        if(this.nonUserFacilities[0]) this.facilityName = this.nonUserFacilities[0].config().facilityName;
        this.authenticationTypes = [];
        var facility;

        this.facilityChanged = function(){
            facility = tc.facility(this.facilityName);
            this.plugin = facility.config().authenticationType[0].plugin;
            this.authenticationTypes = facility.config().authenticationType;
        };
        if(this.nonUserFacilities.length > 0) this.facilityChanged();

        this.login = function(){
            facility.icat().login(this.plugin, this.userName, this.password).then(function(){
                var name;
                var params = {};
                if($sessionStorage.lastState){
                    name = $sessionStorage.lastState.name;
                    params = $sessionStorage.lastState.params;
                } else {
                    name = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
                }
                $state.go(name, params);

            }, function(response){
                inform.add(response.message != null ? response.message : $translate.instant('LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE'), {
                    'ttl': 4000,
                    'type': 'danger'
                });
            });
        };

    });

})();


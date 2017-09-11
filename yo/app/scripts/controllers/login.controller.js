(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('LoginController', function($translate, $state, $rootScope, $scope, $sessionStorage, inform, deviceDetector, tc){
        var that = this;
        this.isFirefox = deviceDetector.browser == 'firefox'
        this.isIE9 = deviceDetector.browser == 'ie' && deviceDetector.browser_version <= 9; 
        this.facilities = tc.facilities();
        this.userFacilities = tc.userFacilities();
        this.nonUserFacilities = tc.nonUserFacilities();
        if(this.nonUserFacilities[0]) this.facilityName = this.nonUserFacilities[0].config().name;
        this.authenticationTypes = [];
        this.userName = "";
        this.password = "";

        var facility;

        $rootScope.$broadcast('login:enter');

        $scope.$on('$destroy', function(){
            $rootScope.$broadcast('login:leave');
        });

        this.login = function(){
            if(this.authenticationType.external){
                $rootScope.$broadcast('login:external:' + this.authenticationType.plugin, facility, this.authenticationType);
            } else {
                facility.icat().login(this.authenticationType.plugin, this.userName, this.password).then(function(){
                    if($state.current.name == 'login-admin'){
                        var cookies = {};
                        _.each(document.cookie.split(/;\s*/), function(pair){
                            pair = pair.split(/=/);
                            cookies[pair[0]] = pair[1];
                        });
                        if(cookies['isAdmin'] == 'true'){
                            window.location.href = '/';
                        } else {
                            inform.add("You're not an admin user", {
                                'ttl': 2000,
                                'type': 'danger'
                            });
                        }
                    } else {
                        var name;
                        var params = {};
                        if($sessionStorage.lastState){
                            name = $sessionStorage.lastState.name;
                            params = $sessionStorage.lastState.params;
                        } else {
                            name = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
                        }
                        $state.go(name, params);
                    }
                }, function(response){
                    inform.add(response.message != null ? response.message : $translate.instant('LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE'), {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });
            }
        };

        this.facilityChanged = function(){
            facility = tc.facility(this.facilityName);
            this.authenticationType = facility.config().authenticationTypes[0];
            this.authenticationTypes = facility.config().authenticationTypes;

            if(this.facilities.length <= 1 && this.authenticationTypes.length == 1 && this.authenticationType.external){
                this.login();
            }
        };
        if(this.nonUserFacilities.length > 0) this.facilityChanged();

    });

})();


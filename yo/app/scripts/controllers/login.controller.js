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
        var facility;

        this.facilityChanged = function(){
            facility = tc.facility(this.facilityName);
            this.plugin = facility.config().authenticationTypes[0].plugin;
            this.authenticationTypes = facility.config().authenticationTypes;
            this.casService = window.location.href.replace(/#.*$/, '').replace(/[^\/]*$/, '') + 'cas?facilityName=' + this.facilityName;
        
            this.authenticationTypesIndex = {};
            _.each(facility.config().authenticationTypes, function(authenticationType){
                that.authenticationTypesIndex[authenticationType.plugin] = authenticationType;
            });
        };
        if(this.nonUserFacilities.length > 0) this.facilityChanged();

        var casIframes = [];
        _.each(tc.nonUserFacilities(), function(facility){
            _.each(facility.config().authenticationTypes, function(authenticationType){
                if(authenticationType.plugin == 'cas'){

                    var service = window.location.href.replace(/#.*$/, '').replace(/[^\/]*$/, '') + 'cas?facilityName=' + facility.config().name;

                    var casIframe = $('<iframe>').attr({
                        src: authenticationType.casUrl + '/login?service=' + encodeURIComponent(service)
                    }).css({
                        position: 'relative',
                        left: '-1000000px',
                        height: '1px',
                        width: '1px'
                    });

                    $(document.body).append(casIframe);
                    casIframes.push(casIframe);
                }
            });
        });
        $scope.$on('$destroy', function(){
            _.each(casIframes, function(casIframe){
                $(casIframe).remove();
            });
        });

        this.login = function(){
            facility.icat().login(this.plugin, this.userName, this.password).then(function(){
                
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
        };

    });

})();


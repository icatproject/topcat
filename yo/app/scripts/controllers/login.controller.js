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
        this.buttonAuthTypes = [];
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
                        // Does the lastUserName match this one? If not, clear lastState
                        if($sessionStorage.lastUserName && $sessionStorage.lastUserName != that.userName){
                            console.log("New user '" + that.userName + "' different from previous '" + $sessionStorage.lastUserName + "'; clearing lastState");
                            delete $sessionStorage.lastState;
                        }
                        $sessionStorage.lastUserName = that.userName;
                        if($sessionStorage.lastState){
                            name = $sessionStorage.lastState.name;
                            params = $sessionStorage.lastState.params;
                        } else {
                            name = tc.config().home == 'browse' ? 'home.browse.facility' : 'home.' + tc.config().home;
                        }
                        $state.go(name, params);
                    }
                }, function(response){
                    // Response can be null
                    var msg = response?response.message:null;
                    inform.add(msg != null ? msg : $translate.instant('LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE'), {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });
            }
        };
        
        this.buttonLogin = function(authenticationType){
        	this.authenticationType = authenticationType;
        	this.login();
        	// At this point, the authenticationType won't be in the non-button authenticationTypes;
        	// so if login fails, the chosen authenticator in the choice list will be blank;
        	// and if the choice list contains just one authenticator, the user won't be able to change it!
        	// So at this point, always reset the authenticationType to a non-button authenticator, if there is one
        	if( this.authenticationTypes.length >= 1 ){
        		this.authenticationType = this.authenticationTypes[0];
        	}
        };
        
        this.showCredInputs = function(){
        	// true if the currently-selected auth type requires credentials
        	// OR if any of the button-based authenticators do
        	if( this.authenticationType.plugin != 'anon' && !this.authenticationType.external ){
        		return true;
        	} else {
        		return _.some(this.buttonAuthTypes,function(authenticationType){return authenticationType.plugin != 'anon' && !authenticationType.external;});
        	};
        };
        
        this.requiresCreds = function(authenticationType){
        	return authenticationType.plugin != 'anon' && ! authenticationType.external;
        }

        this.facilityChanged = function(){
            facility = tc.facility(this.facilityName);
            this.authenticationTypes = facility.config().authenticationTypes;
            
            // Split authenticationTypes into those that have / don't have buttons
            this.buttonAuthTypes = _.select(this.authenticationTypes, function(authenticationType){return authenticationType.showAsButton;});
            this.authenticationTypes = _.select(this.authenticationTypes, function(authenticationType){return !authenticationType.showAsButton;});
            
            // If there are still non-button auth types, choose the first initially
            if( this.authenticationTypes.length > 0 ){
            	this.authenticationType = this.authenticationTypes[0];
            } else {
            	// Unlikely to be used: only when there's only one external authType, and yet showAsButton is true
            	this.authenticationType = this.buttonAuthTypes[0];
            }

            // If there's only one authenticator and it's external, then login directly
            if(this.facilities.length <= 1 && this.authenticationTypes.length + this.buttonAuthTypes.length == 1 && this.authenticationType.external){
                this.login();
            }
        };
        if(this.nonUserFacilities.length > 0) this.facilityChanged();

    });

})();


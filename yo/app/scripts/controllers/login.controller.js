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

/*
(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$rootScope', '$state', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', '$translate', '$q', 'DataManager', '$sessionStorage', '$localStorage', 'inform', 'Cart', 'RemoteStorageManager', 'deviceDetector', 'SMARTCLIENTPING', 'SmartClientManager', 'tc'];

    function LoginController($rootScope, $state, APP_CONFIG, Config, ConfigUtils, RouteUtils, $translate, $q, DataManager, $sessionStorage, $localStorage, inform, Cart, RemoteStorageManager, deviceDetector, SMARTCLIENTPING, SmartClientManager, tc) {
        var vm = this;
        vm.user = {};

        $sessionStorage.$default({
            sessions : {}
        });

        //@TODO this could do with a review/clean up. Functions are called multiple times from the template

        //get available facilities from config
        var allFacilityNames = ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG));
        var loggedInFacilities = _.keys($sessionStorage.sessions);
        var notLoggedInFacilities = getNotLoggedInFacilitiesObjects(allFacilityNames, loggedInFacilities, APP_CONFIG, Config);

        if (notLoggedInFacilities.length !== 0) {
            vm.facilities = notLoggedInFacilities;
            vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, allFacilityNames[0]).authenticationType;
            vm.user.facilityName = vm.facilities[0].facilityName;
            vm.user.plugin = getAuthenticationTypes(vm.user.facilityName)[0].plugin;
        }

        vm.updateAuthenticationTypes = updateAuthenticationTypes;
        vm.getAuthenticationTypes = getAuthenticationTypes;
        vm.isLoggedInAll = isLoggedInAll;
        vm.isSingleFacility = isSingleFacility;
        vm.isSingleAuthenticationType = isSingleAuthenticationType;
        vm.isAnonymous = isAnonymous;
        vm.login = login;

        vm.isFirefox = (deviceDetector.browser === 'firefox') ? true : false;
        vm.isIE9 = (deviceDetector.browser === 'ie' && deviceDetector.browser_version <= 9) ? true : false; //jshint ignore: line

        //load previous remembered login
        loadRememberMe();

        function loadRememberMe() {
            if (typeof $localStorage.login !== 'undefined') {
                var rememberedFacility = _.find(vm.facilities, function(facility) {
                    return (facility.facilityName === $localStorage.login.facilityName);
                });

                if (typeof rememberedFacility !== 'undefined') {
                    vm.user.facilityName = rememberedFacility.facilityName;
                }

                //TODO need to add a check here that the facility exists rather than a try block
                try {
                    vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, $localStorage.login.facilityName).authenticationType;
                } catch(error) {

                }

                var rememberedPlugin = _.find(vm.authenticationTypes, function(plugin) {
                    return (plugin.plugin === $localStorage.login.plugin);
                });

                if (typeof rememberedFacility !== 'undefined') {
                    if (typeof rememberedPlugin !== 'undefined') {
                        vm.user.plugin = rememberedPlugin.plugin;
                    }
                }
            }
        }

        function isLoggedInAll() {
            if (notLoggedInFacilities.length === 0) {
                return true;
            }

            return false;
        }

        function isSingleFacility() {
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                return true;
            }

            return false;
        }

        function isSingleAuthenticationType() {
            if (Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length === 1) {
                return true;
            }

            return false;
        }

        function isAnonymous() {
            if (angular.isDefined(vm.user.plugin)) {
                if (vm.user.plugin === 'anon') {
                    return true;
                }
            }

            return false;
        }

        function updateAuthenticationTypes(facilityName) {
            vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, facilityName).authenticationType;
            vm.user.plugin = vm.authenticationTypes[0].plugin;
        }


        function getAuthenticationTypes(facilityName) {
            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
            return facility.authenticationType;
        }

        function login(form) {
            //remember selected facility and authentication type for multiple facilities
            $localStorage.login = {
                facilityName: form.facilityName.$modelValue,
                plugin: form.plugin.$modelValue
            };

            var facility = Config.getFacilityByName(APP_CONFIG, form.facilityName.$modelValue);
            var credential = {};

            if (vm.isAnonymous()) {
                credential = {
                    plugin : form.plugin.$modelValue,
                    credentials : {
                    }
                };
            } else {
                credential = {
                    plugin : form.plugin.$modelValue,
                    credentials : {
                        username : form.username.$modelValue,
                        password : form.password.$modelValue
                    }
                };
            }

            DataManager.login(facility, credential).then(function(data){
                var defered = $q.defer();
                tc.admin(facility.facilityName).isValidSession(data.sessionId).then(function(isAdmin){
                    data.isAdmin = isAdmin;
                    defered.resolve(data);
                });
                return defered.promise;
            }).then(function(data){
                vm.session = data;

                if (data !== null && angular.isDefined(data.sessionId)) {

                    //reset the form
                    $sessionStorage.sessions[form.facilityName.$modelValue]  = {
                        sessionId : data.sessionId,
                        userName: data.userName,
                        isAdmin: data.isAdmin,
                    };

                    //Do login stuff here
                    Cart.restore();

                    //login to smartclient is installed/online
                    if (SMARTCLIENTPING.ping === 'online') {
                        SmartClientManager.connect(data.sessionId, facility);
                    }

                    $rootScope.$broadcast('Login:success', {facility: facility, userName: data.userName});

                    //clear the password field
                    delete vm.user.password;

                    //sets the form to pristine state
                    form.$setPristine();
                    
                    var lastState = $sessionStorage.lastState;
                    var sessions = $sessionStorage.sessions;
                    var previouslyBrowsingFacility = lastState && lastState.name.match(/^home.browse.facility.facility-/);
                    var isFacilitySession = lastState && sessions[lastState.params.facilityName] && sessions[lastState.params.facilityName].sessionId;

                    //if previously browsing a facility make sure there is a session for that facility.
                    if(lastState && ((previouslyBrowsingFacility && isFacilitySession) || !previouslyBrowsingFacility)){
                        $state.go(lastState.name, lastState.params);
                    } else {
                        //$state.go('home.browse.facility');
                        $state.go(RouteUtils.getHomeRouteName());
                    }
                } else {
                    //@TODO is this code still necessary??

                    //set the default error message
                    var message = null;

                    //@TODO check why message is returned as a string!!
                    //get and set error message from server
                    if (typeof data === 'string') {
                        var json;
                        try {
                            json = JSON.parse(data);
                        } catch(error) {

                        }

                        if (angular.isDefined(json)) {
                            message = json.message;
                        }

                        inform.add(message !== null ? message : $translate.instant('LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE'), {
                            'ttl': 4000,
                            'type': 'danger'
                        });
                    }
                }
            }, function(error) {
                var errorMessage = (error !== null) ? error : $translate.instant('LOGIN.DEFAULT_LOGIN_ERROR_MESSAGE');

                inform.add(errorMessage, {
                    'ttl': 4000,
                    'type': 'danger'
                });
            });
        }

        //get not logged in facilities
        function getNotLoggedInFacilitiesObjects(allFacilityNames, loggedInFacilities, APP_CONFIG, Config) {
            var notLoggedInFacilitiesObject = [];

            var notLoggedInFacilitiesNames = _.difference(allFacilityNames, loggedInFacilities);

            _.each(notLoggedInFacilitiesNames, function(facilityName) {
                notLoggedInFacilitiesObject.push(Config.getFacilityByName(APP_CONFIG, facilityName));
            });

            return notLoggedInFacilitiesObject;
        }
    }
})();

*/

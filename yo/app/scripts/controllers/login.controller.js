(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$rootScope', '$state', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', '$translate', 'DataManager', '$sessionStorage', '$localStorage', 'inform', 'Cart', 'RemoteStorageManager', 'deviceDetector', 'SMARTCLIENTPING', 'SmartClientManager', '$log'];

    function LoginController($rootScope, $state, APP_CONFIG, Config, ConfigUtils, RouteUtils, $translate, DataManager, $sessionStorage, $localStorage, inform, Cart, RemoteStorageManager, deviceDetector, SMARTCLIENTPING, SmartClientManager, $log) { //jshint ignore: line
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

        vm.facilities = notLoggedInFacilities;
        vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, allFacilityNames[0]).authenticationType;
        vm.user.facilityName = vm.facilities[0].facilityName;
        vm.user.plugin = getAuthenticationTypes(vm.user.facilityName)[0].plugin;

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

        /**
         * Save the user's last selected facility and authentication type to localstorage
         * @return {[type]} [description]
         */
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
                    $log.debug('facility ' + $localStorage.login.facilityName + ' is not configured');
                }

                var rememberedPlugin = _.find(vm.authenticationTypes, function(plugin) {
                    return (plugin.plugin === $localStorage.login.plugin);
                });

                if (typeof rememberedFacility !== 'undefined') {
                    vm.user.plugin = rememberedPlugin.plugin;
                }
            }
        }

        /**
         * Determine if a user has logged into all the congifured facilities
         * @return {Boolean} [description]
         */
        function isLoggedInAll() {
            if (notLoggedInFacilities.length === 0) {
                return true;
            }

            return false;
        }

        /**
         * Determine if only one facility is configured
         * @return {Boolean} [description]
         */
        function isSingleFacility() {
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                return true;
            }

            return false;
        }

        /**
         * Determine if only one authentication type is configured for the current selected facility
         * @return {Boolean} [description]
         */
        function isSingleAuthenticationType() {
            if (Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length === 1) {
                return true;
            }

            return false;
        }

        /**
         * Check if the authentication type in anonymous
         * @return {Boolean} [description]
         */
        function isAnonymous() {
            if (angular.isDefined(vm.user.plugin)) {
                if (vm.user.plugin === 'anon') {
                    return true;
                }
            }

            return false;
        }

        /**
         * Updates the authenticationTypes when a facility is selected and set the type
         * to the first one on the list
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        function updateAuthenticationTypes(facilityName) {
            vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, facilityName).authenticationType;
            vm.user.plugin = vm.authenticationTypes[0].plugin;
        }


        /**
         * Get the authentication type for login form dropdown
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        function getAuthenticationTypes(facilityName) {
            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
            return facility.authenticationType;
        }


        /**
         * Perform a login. Go to facilities list if success and display a notification
         *
         * @param  {[type]} form [description]
         * @return {[type]}      [description]
         */
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

            var promise = DataManager.login(facility, credential);

            promise.then(function(data){
                vm.session = data;

                if (data !== null && angular.isDefined(data.sessionId)) {

                    //reset the form
                    $sessionStorage.sessions[form.facilityName.$modelValue]  = {
                        sessionId : data.sessionId,
                        userName: data.userName
                    };

                    //Do login stuff here
                    Cart.restore();

                    //login to smartclient is installed/online
                    if (SMARTCLIENTPING.ping === 'online') {
                        SmartClientManager.connect(data.sessionId, facility).then(function(connectData) {
                            $log.debug('SmartClientPollManager login', connectData);
                        }, function() {
                            $log.debug('Unable to login to smartcient');
                        });
                    }

                    $rootScope.$broadcast('Login:success', {facility: facility, userName: data.userName});

                    //clear the password field
                    delete vm.user.password;

                    //sets the form to pristine state
                    form.$setPristine();
                    //$state.go('home.browse.facility');
                    $state.go(RouteUtils.getHomeRouteName());
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
                            //$log.debug('could not parse error string: ' + error);
                            //throw new Error('could not parse error string: ' + error);
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
                $log.debug(errorMessage);
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

            //$log.debug('notLoggedInFacilitiesNames', notLoggedInFacilitiesNames);

            _.each(notLoggedInFacilitiesNames, function(facilityName) {
                //$log.debug('facilityName', facilityName);
                notLoggedInFacilitiesObject.push(Config.getFacilityByName(APP_CONFIG, facilityName));
            });

            return notLoggedInFacilitiesObject;
        }
    }
})();

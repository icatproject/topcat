(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$state', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', '$translate', 'DataManager', '$sessionStorage', 'inform', '$log'];

    function LoginController($state, APP_CONFIG, Config, ConfigUtils, RouteUtils, $translate, DataManager, $sessionStorage, inform, $log) {
        var vm = this;

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


        vm.isLoggedInAll  = function() {
            //$log.debug('LoginController.isLoggedInAll called');
            if (notLoggedInFacilities.length === 0) {
                return true;
            }

            return false;
        };

        vm.isSingleFacility = function() {
            //$log.debug('LoginController.isSingleFacility', ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length);
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                return true;
            }

            return false;
        };


        vm.isSingleAuthenticationType = function() {
            //$log.debug('LoginController.isSingleAuthenticationType', Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length);
            if (Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length === 1) {
                return true;
            }

            return false;
        };

        vm.isAnonymous = function() {
            if (angular.isDefined(vm.user.plugin)) {
                if (vm.user.plugin === 'anon') {
                    return true;
                }
            }

            return false;
        };


        vm.updateAuthenticationTypes = function(facilityName) {
            //$log.debug('LoginController.updateAuthenticationTypes called');
            vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, facilityName).authenticationType;
            vm.user.plugin = vm.authenticationTypes[0].plugin;
        };

        /**
         * Get the authentication type for login form dropdown
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        vm.getAuthenticationTypes = function(facilityName) {
            //$log.debug('LoginController.getAuthenticationTypes called');
            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
            return facility.authenticationType;
        };


        /**
         * Perform a login. Go to facilities list if success and display a notification
         *
         * @param  {[type]} form [description]
         * @return {[type]}      [description]
         */
        vm.login = function(form) {
            //$log.debug('LoginController.login called');
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

                    //clear the password field
                    delete vm.user.password;

                    //sets the form to pristine state
                    form.$setPristine();
                    //$state.go('home.browse.facility');
                    $state.go(RouteUtils.getHomeRouteName());
                } else {
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
                        'ttl': 0,
                        'type': 'danger'
                        });
                    }
                }

            });
        };

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

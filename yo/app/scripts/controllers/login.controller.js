(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$state', 'APP_CONFIG', 'Config', 'ConfigUtils', '$translate', 'DataManager', '$sessionStorage', 'inform'];

    function LoginController($state, APP_CONFIG, Config, ConfigUtils, $translate, DataManager, $sessionStorage, inform) {
        var vm = this;

        $sessionStorage.$default({
            sessions : {}
        });

        //@TODO this could do with a review/clean up. Functions are called multiple times from the template

        //get available facilities from config
        var allFacilityNames = ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG));
        var loggedInFacilities = _.keys($sessionStorage.sessions);
        var notLoggedInFacilities = _.difference(allFacilityNames, loggedInFacilities);

        vm.facilities = notLoggedInFacilities;
        vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, allFacilityNames[0]).authenticationType;

        vm.isLoggedInAll  = function() {
            console.log('LoginController.isLoggedInAll called');
            if (notLoggedInFacilities.length === 0) {
                return true;
            }

            return false;
        };

        vm.isSingleFacility = function() {
            console.log('LoginController.isSingleFacility', ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length);
            if (ConfigUtils.getAllFacilityNames(Config.getFacilities(APP_CONFIG)).length === 1) {
                return true;
            }

            return false;
        };


        vm.isSingleAuthenticationType = function() {
            console.log('LoginController.isSingleAuthenticationType', Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length);
            if (Config.getFacilityByName(APP_CONFIG, vm.user.facilityName).authenticationType.length === 1) {
                return true;
            }

            return false;
        };


        vm.updateAuthenticationTypes = function(facilityName) {
            console.log('LoginController.updateAuthenticationTypes called');
            vm.authenticationTypes = Config.getFacilityByName(APP_CONFIG, facilityName).authenticationType;
            vm.user.plugin = vm.authenticationTypes[0].plugin;
        };

        /**
         * Get the authentication type for login form dropdown
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        vm.getAuthenticationTypes = function(facilityName) {
            console.log('LoginController.getAuthenticationTypes called');
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
            console.log('LoginController.login called');
            var facility = Config.getFacilityByName(APP_CONFIG, form.facilityName.$modelValue);

            var credential = {
                plugin : form.plugin.$modelValue,
                credentials : {
                    username : form.username.$modelValue,
                    password : form.password.$modelValue
                }
            };

            var promise = DataManager.login(facility, credential);

            promise.then(function(data){
                vm.session = data;

                //@TODO remove until login code is finalised
                var useFileForSession = angular.isDefined(APP_CONFIG.site.useFileForSession) ? APP_CONFIG.site.useFileForSession : false;
                if (useFileForSession) {
                    $sessionStorage.sessions = data;
                    //clear the password field
                    delete vm.user.password;

                    //sets the form to pristine state
                    form.$setPristine();

                    /*var previousState =  $previousState.get();
                    if (previousState !== null) {
                        $previousState.go();
                    } else {
                        $state.go('home');
                    }*/
                    $state.go('home.browse.facilities');

                } else {

                    if (angular.isDefined(data.sessionId)) {
                        //reset the form
                        $sessionStorage.sessions[form.facilityName.$modelValue]  = {
                            sessionId : data.sessionId
                        };

                        //clear the password field
                        delete vm.user.password;

                        //sets the form to pristine state
                        form.$setPristine();
                        $state.go('home.browse.facilities');
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
                                console.log('could not parse error string: ' + error);
                            }

                            if (angular.isDefined(json)) {
                                message = json.message;
                            }
                        }

                        inform.add(message !== null ? message : $translate('DEFAULT_LOGIN_ERROR_MESSAGE'), {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }
                }
            });
        };
    }
})();

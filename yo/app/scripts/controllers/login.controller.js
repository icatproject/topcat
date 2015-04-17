(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$state', 'APP_CONFIG', 'Config', 'DataManager', '$sessionStorage', 'inform'];

    function LoginController($state, APP_CONFIG, Config, DataManager, $sessionStorage, inform) {
        var vm = this;

        $sessionStorage.$default({
            sessions : {}
        });

        console.log('$sessionStorage', $sessionStorage.sessions);

        vm.login = function(form) {
            console.log('in login');

            console.log('login controller state', $state);

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
                        var message = 'Login failed';

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

                        inform.add(message, {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }
                }
            });
        };
    }
})();

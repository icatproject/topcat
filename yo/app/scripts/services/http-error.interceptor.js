(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('HttpErrorInterceptor', HttpErrorInterceptor);

    HttpErrorInterceptor.$inject = ['inform', '$translate', '$sessionStorage', '$injector', '$log'];

    function HttpErrorInterceptor(inform, $translate, $sessionStorage, $injector, $log){
        return {
            responseError: function(rejection) {
                    $log.debug('bad response', rejection);

                    var state;

                    if(rejection.status === 403){
                        $log.debug('HttpErrorInterceptor', rejection);
                        $log.debug('HttpErrorInterceptor facilityTitle', rejection.config.headers.facilityTitle);

                        state = $injector.get('$state');

                        inform.add($translate.instant('SESSION.EXPIRED_ERROR', {'facilityTitle' : rejection.config.headers.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });

                        //delete the session
                        $log.debug('delete session key: ' + rejection.config.headers.facilityKeyName);
                        delete $sessionStorage.sessions[rejection.config.headers.facilityKeyName];

                        if (_.size($sessionStorage.sessions) === 0) {
                            state.go('login');
                        }
                    }

                    if(rejection.status === 400){
                        $log.debug('HttpErrorInterceptor', rejection);
                        $log.debug('HttpErrorInterceptor facilityTitle', rejection.config.headers.facilityTitle);

                        state = $injector.get('$state');

                        inform.add($translate.instant('RESTAPI.BAD_REQUEST_ERROR', {'facilityTitle' : rejection.config.headers.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }

                    if(rejection.status === 0){
                        inform.add($translate.instant('RESPONSE.ERROR.NO_CONNECTION', {'facilityTitle' : rejection.config.headers.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }

                    return rejection;
                }
        };
    }
})();

(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('HttpErrorInterceptor', HttpErrorInterceptor);

    HttpErrorInterceptor.$inject = ['$rootScope', 'inform', '$translate', '$sessionStorage', '$injector', '$q', '$log'];

    function HttpErrorInterceptor($rootScope, inform, $translate, $sessionStorage, $injector, $q, $log){
        return {
            responseError: function(rejection) {
                    $log.debug('bad response', rejection);

                    var state;
                    var userName;

                    function idsInvalidUUID(data) {
                        if (data.code === 'BadRequestException') {
                            if (data.message.indexOf('is not a valid UUID') > -1) {
                                return true;
                            }
                        }

                        return false;
                    }

                    function icatInsufficientPrivileges(data) {
                        if (data.code === 'InsufficientPrivilegesException') {
                            return true;
                        }

                        return false;
                    }

                    if(rejection.status === 403){
                        $log.debug('HttpErrorInterceptor', rejection);
                        $log.debug('HttpErrorInterceptor facilityTitle', rejection.config.headers.facilityTitle);

                        state = $injector.get('$state');

                        inform.add($translate.instant('SESSION.EXPIRED_ERROR', {'facilityTitle' : rejection.config.headers.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });

                        //delete the session
                        if (typeof $sessionStorage.sessions[rejection.config.headers.facilityKeyName] !== 'undefined') {
                            userName = $sessionStorage.sessions[rejection.config.headers.facilityKeyName].userName;

                            delete $sessionStorage.sessions[rejection.config.headers.facilityKeyName];
                        }


                        //if (typeof userName !== 'undefined' && typeof rejection.config.headers.facilityKeyName !== 'undefined') {
                        //broadcast session expiry
                        $rootScope.$broadcast('SESSION:EXPIRED', {facilityName: rejection.config.headers.facilityKeyName, userName: userName});
                        //}

                        if (_.size($sessionStorage.sessions) === 0) {
                            state.go('login');
                        }
                    }

                    if(rejection.status === 400){
                        $log.debug('HttpErrorInterceptor', rejection);
                        $log.debug('HttpErrorInterceptor facilityTitle', rejection.config.headers.facilityTitle);

                        state = $injector.get('$state');

                        var data;

                        try {
                            data = JSON.parse(rejection.data);
                        } catch(error){
                            $log.debug('Error parsing server message');
                        }

                        if (typeof data !== 'undefined') {
                            //check it is an InsufficientPrivilegesException error meaning session expired
                            if (idsInvalidUUID(data) || icatInsufficientPrivileges(data)) {
                                $log.debug('session expired deleting session');
                                userName = $sessionStorage.sessions[rejection.config.headers.facilityKeyName].userName;
                                delete $sessionStorage.sessions[rejection.config.headers.facilityKeyName];

                                //broadcast session expiry
                                $rootScope.$broadcast('SESSION:EXPIRED', {facilityName: rejection.config.headers.facilityKeyName, userName: userName});
                            }
                        }

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

                    return $q.reject(rejection);
                }
        };
    }
})();

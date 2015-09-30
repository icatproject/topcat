(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('HttpErrorInterceptor', HttpErrorInterceptor);

    HttpErrorInterceptor.$inject = ['$rootScope', 'inform', '$translate', '$sessionStorage', '$injector', '$q'];

    function HttpErrorInterceptor($rootScope, inform, $translate, $sessionStorage, $injector, $q){
        return {
            responseError: function(rejection) {
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

                    function isSmartClientSessionExpired(data) {
                        if (data.code === 'ICAT reports') {
                            if (data.message.indexOf('Unable to find user by sessionid') > -1) {
                                return true;
                            }
                        }

                        return false;
                    }




                    //by pass interceptor if byPassIntercepter is set to true
                    if (typeof rejection.config.byPassIntercepter !== 'undefined' && rejection.config.byPassIntercepter === true) {
                        return $q.reject(rejection);
                    }

                    if(rejection.status === 403){
                        console.log('HttpErrorInterceptor', rejection);
                        console.log('HttpErrorInterceptor facilityTitle', rejection.config.info.facilityTitle);

                        state = $injector.get('$state');

                        inform.clear();

                        inform.add($translate.instant('SESSION.EXPIRED_ERROR', {'facilityTitle' : rejection.config.info.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });

                        //delete the session
                        if (typeof $sessionStorage.sessions[rejection.config.info.facilityKeyName] !== 'undefined') {
                            userName = $sessionStorage.sessions[rejection.config.info.facilityKeyName].userName;

                            delete $sessionStorage.sessions[rejection.config.info.facilityKeyName];
                        }


                        if (typeof userName !== 'undefined' && typeof rejection.config.info.facilityKeyName !== 'undefined') {
                            //broadcast session expiry
                            $rootScope.$broadcast('SESSION:EXPIRED', {facilityName: rejection.config.info.facilityKeyName, userName: userName});
                        }

                        if (_.size($sessionStorage.sessions) === 0) {
                            state.go('login');
                        }
                    }

                    if(rejection.status === 400){
                        //$log.debug('HttpErrorInterceptor', rejection);
                        //$log.debug('HttpErrorInterceptor facilityTitle', rejection.config.info.facilityTitle);

                        state = $injector.get('$state');

                        if (typeof rejection.data !== 'undefined') {
                            //check it is an InsufficientPrivilegesException error meaning session expired
                            if (idsInvalidUUID(rejection.data) || icatInsufficientPrivileges(rejection.data) || isSmartClientSessionExpired(rejection.data)) {
                                //if (typeof $sessionStorage.sessions[rejection.config.info.facilityKeyName] !== 'undefined')

                                userName = $sessionStorage.sessions[rejection.config.info.facilityKeyName].userName;
                                delete $sessionStorage.sessions[rejection.config.info.facilityKeyName];

                                //broadcast session expiry
                                if (typeof rejection.config.info.facilityKeyName !== 'undefined' && typeof userName !== 'undefined') {
                                    $rootScope.$broadcast('SESSION:EXPIRED', {facilityName: rejection.config.info.facilityKeyName, userName: userName});
                                } else {
                                    $rootScope.$broadcast('SESSION:EXPIRED', {});
                                }
                            }
                        }

                        inform.add($translate.instant('RESTAPI.BAD_REQUEST_ERROR', {'facilityTitle' : rejection.config.info.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }

                    /*if(rejection.status === 0){
                        inform.add($translate.instant('RESPONSE.ERROR.NO_CONNECTION', {'facilityTitle' : rejection.config.info.facilityTitle}), {
                            'ttl': 0,
                            'type': 'danger'
                        });
                    }*/

                    return $q.reject(rejection);
                }
        };
    }
})();

'use strict';

angular
    .module('angularApp')
    .factory('HttpErrorInterceptor', HttpErrorInterceptor);

HttpErrorInterceptor.$inject = ['inform', '$translate'];

function HttpErrorInterceptor(inform, $translate){
    return {
        responseError: function(rejection) {
                console.log('bad response', rejection);
                if(rejection.status === 403){
                    console.log('HttpErrorInterceptor', rejection);
                    console.log('HttpErrorInterceptor facilityTitle', rejection.config.headers.facilityTitle);

                    inform.add($translate.instant('SESSION.EXPIRED_ERROR', {'facilityTitle' : rejection.config.headers.facilityTitle}), {
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


'use strict';

angular
    .module('angularApp')
    .factory('Authenticate', Authenticate );

Authenticate.$inject = ['$q', '$sessionStorage'];

function Authenticate($q, $sessionStorage){
    return {
        authenticate : function(){
            //console.log('authenticate called');

            var isAuthenticated = false;

            //console.log('$sessionStorage.sessions', $sessionStorage.sessions);

            if (_.size($sessionStorage.sessions) > 0) {
                isAuthenticated = true;
            }

            //Authentication logic here
            if(isAuthenticated){
                //If authenticated, return anything you want, probably a user object
                //console.log('returning true');
                return true;
            } else {
                //Else send a rejection
                //console.log('returning reject promise');

                return $q.reject({isAuthenticated : false});
            }
        }
    };

}
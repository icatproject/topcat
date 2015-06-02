'use strict';

angular
    .module('angularApp')
    .factory('Authenticate', Authenticate );

Authenticate.$inject = ['$q', '$sessionStorage'];

function Authenticate($q, $sessionStorage){
    return {
        authenticate : function(){
            var isAuthenticated = false;

            if (_.size($sessionStorage.sessions) > 0) {
                isAuthenticated = true;
            }

            //Authentication logic here
            if(isAuthenticated){
                //If authenticated, return anything you want, probably a user object
                return true;
            } else {
                //Else send a rejection
                return $q.reject({isAuthenticated : false});
            }
        }
    };

}
(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('RemoteStorageManager', RemoteStorageManager);

    RemoteStorageManager.$inject = ['TopcatManager', '$sessionStorage', 'APP_CONFIG', 'Config'];

    function RemoteStorageManager(TopcatManager, $sessionStorage, APP_CONFIG, Config) {
        var manager = {};

        /**
         * Returns a string base of the username and the facility which will be used
         * as an object key
         *
         * @param  {[type]} facilityName [description]
         * @param  {[type]} userName     [description]
         * @return {[type]}              [description]
         */
        function getObjectKey(facilityName, userName) {
            return userName + '@#@' + facilityName;
        }

        /**
         * Split the object key and return an object with facilityName and userName
         * @param  {[type]} key [description]
         * @return {[type]}     [description]
         */
        function splitObjectKey(key) {
            var arr = key.split('@#@');

            return {
                userName: arr[0],
                facilityName: arr[1]
            };
        }

        /**
         * Initialise a localstorage for a user of a facility. This put a storage structure
         * on the browser
         *
         * @param  {[type]} facility [description]
         * @param  {[type]} userName [description]
         * @return {[type]}          [description]
         */
        manager.init = function(facility, userName) {
            if (!facility || ! userName) {
                throw new Error('facility and userName required');
            }
        };

        /**
         * Returns a user store for a facility
         *
         * @param  {[type]} facility [description]
         * @param  {[type]} userName [description]
         * @return {[type]}          [description]
         */
        manager.getUserStore = function(facility, userName) {
            if (!facility || ! userName) {
                throw new Error('facility and userName required');
            }

            return TopcatManager.getCartItems(facility, userName);
        };


        /**
         * Saves the cart to the localstorage.
         *
         * @param {[type]} cart [description]
         */
        manager.setStore = function(cart) {
            var myCarts = {};

            //need a copy of the session which is used to determine if a cart for a logged
            //in facility is empty as there are no indication in the cart otherwise.
            //If a logged in facility has an item in the cart, the facility key will be removed.
            //The remaining facility are those that does not have items in the cart.
            var loggedInSessions = angular.copy($sessionStorage.sessions);

            //set cart items to the localstorage
            _.each(cart.items, function(item) {

                var itemCopy = angular.copy(item);

                //initial array if not already done so
                if (typeof myCarts[getObjectKey(itemCopy.facilityName, itemCopy.userName)] === 'undefined') {
                    myCarts[getObjectKey(itemCopy.facilityName, itemCopy.userName)] = {
                        items: []
                    };
                }

                delete itemCopy.availability;
                delete itemCopy.size;

                myCarts[getObjectKey(itemCopy.facilityName, itemCopy.userName)].items.push(itemCopy);

                //remove the key from loggedInSessions object since the facility has items in the cart
                delete loggedInSessions[item.facilityName];
            });

            //Set items to an empty array for facilities that doesn't have items in the cart
            _.each(loggedInSessions, function(session, key) {
                myCarts[getObjectKey(key, session.userName)] = {
                    items: []
                };
            });

            _.each(myCarts, function(myCart, key) {
                var facility = Config.getFacilityByName(APP_CONFIG, splitObjectKey(key).facilityName);
                var userName = splitObjectKey(key).userName;

                var saveCartOject = {
                    facilityName: facility.facilityName,
                    userName: userName,
                    sessionId: $sessionStorage.sessions[facility.facilityName].sessionId,
                    icatUrl: facility.icatUrl,
                    cartItems: myCart.items
                };

                TopcatManager.saveCart(facility, userName, saveCartOject);
            });
        };

        /**
         * Reset a user store to empty
         *
         * @param  {[type]} facility [description]
         * @param  {[type]} userName [description]
         * @return {[type]}          [description]
         */
        manager.resetUser = function(facility, userName) {
            TopcatManager.removeCart(facility, userName);
        };

        return manager;
    }
})();
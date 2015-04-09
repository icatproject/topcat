'use strict';

angular
    .module('angularApp')
    .factory('ConfigUtils', ConfigUtils);

ConfigUtils.$inject = ['$q'];

function ConfigUtils($q){
    return {

        /**
         * returns the first key name of an object
         *
         * @param  {Object} obj the object
         * @return {String} the key name
         */
        getFirstKey : function(obj) {
            return _.keys(obj)[0];
        },


        /**
         * This is a function to get a list of facilities from the config file
         *
         * The actual function should make a request to all the
         * connected servers in order to get the id number of the facility.
         * Currently we have it hard coded in the config.json file.
         * Somewhere in the config.json file, we need to map a config
         * to a facility in a ICAT server. Suggest using the name key.
         *
         *
         * @param  {[type]} APP_CONFIG [description]
         * @return {[type]}            [description]
         */
        getFacilitiesFromConfig : function(APP_CONFIG){
            var facilities = [];

            _.each(APP_CONFIG.servers, function(servervalue, serverName) {
                _.each(servervalue.facility, function(facility) {
                    var obj = {};

                    _.each(facility, function(value, key){
                        obj.server = serverName;

                        if (key === 'facilityId') {
                            obj.id = value;
                        }

                        if (key === 'name') {
                            obj.name = value;
                        }

                        if (key === 'title') {
                            obj.title = value;
                        }

                    });

                    facilities.push(obj);
                });
            });

            //we need to return a promise
            var deferred = $q.defer();
            deferred.resolve(facilities);

            return deferred.promise;
        }

    };
}


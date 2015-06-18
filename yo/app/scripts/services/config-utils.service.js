(function() {
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


            getAllFacilityNames : function(facilities) {
                return _.keys(facilities);
            },


            /**
             * This is a dummy function to get a list of facilities
             * The actual function should make a request to all the
             * connected servers in order to get the id number of the facility.
             * Currently we have it hard coded in the config.json file.
             * Somewhere in the config.json file, we need to map a config
             * to a facility in a ICAT server. Suggest using the name key.
             *
             *
             * @param  {Object} facilities the list of facility objects
             * @return {Object}            a promise containing the list of facilities as expected by datatables
             */
            getFacilitiesFromConfig : function(facilities){
                var data = [];

                _.each(facilities, function(value){
                    var obj = {};
                    obj.id = value.facilityId;
                    obj.name = value.facilityName;
                    obj.title = value.title;

                    data.push(obj);
                });

                //we need to return a promise
                var deferred = $q.defer();
                deferred.resolve(data);

                return deferred.promise;
            },


            /**
             * Return the list of logged in facilities from the config as a promise
             * @param  {[type]} facilities [description]
             * @param  {[type]} sessions   [description]
             * @return {[type]}            [description]
             */
            getLoggedInFacilitiesFromConfig : function(facilities, sessions){
                var data = [];
                var loggedIn = _.keys(sessions);

                if (_.size(loggedIn) !== 0) {
                    _.each(facilities, function(value){
                        if(_.indexOf(loggedIn, value.facilityName) !== -1) {
                            var obj = {};
                            obj.id = value.facilityId;
                            obj.name = value.facilityName;
                            obj.fullName = value.title;

                            data.push(obj);
                        }
                    });
                }

                //we need to return a promise
                var deferred = $q.defer();
                deferred.resolve(data);

                return deferred.promise;
            },

            getLoggedInFacilities : function(facilities, sessions){
                var data = [];
                var loggedIn = _.keys(sessions);

                if (_.size(loggedIn) !== 0) {
                    _.each(facilities, function(value){
                        if(_.indexOf(loggedIn, value.facilityName) !== -1) {
                            data.push(value);
                        }
                    });
                }

                //we need to return a promise
                var deferred = $q.defer();
                deferred.resolve(data);

                return deferred.promise;
            },


            /**
             * Returns the default sort column and order array expected by datatables options.
             * The array is in the form [index number of the column to sort, the order string 'asc' or 'desc']
             * If none is configured, default to using the first column and sort by ascending order
             * @param  {object} column the browser column config
             * @return {Array}        the column index and sort order array expected by dataTable options
             */
            getDefaultSortArray : function(column) {
                //default to sort by first column and in ascending order
                var defaultSort = [0, 'asc'];

                //find the first column index where there is a key named 'sortByDefault'
                var sortColumnIndex = _.findIndex(column, function(field) {
                    return _.contains(_.keys(field), 'sortByDefault');
                });

                //set default order if one is found
                if (angular.isDefined(sortColumnIndex) && sortColumnIndex !== -1) {
                    defaultSort = [sortColumnIndex, column[sortColumnIndex].sortByDefault];
                }

                return defaultSort;
            },

            getSessionValueForFacility : function(sessions, facility) {
                return sessions[facility.facilityName].sessionId;
            }

        };
    }
})();

'use strict';

angular
    .module('angularApp')
    .factory('Config', Config);

Config.$inject = [];

function Config(){
    return {

        /**
         * Return the list of facilities from the config
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        getFacilities : function(config) {
            return config.facilities;
        },

        /**
         * Returns a facility object by a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getFacilityByName : function(config, facilityName) {
            return config.facilities[facilityName];
        },

        /**
         * Returns the facility id by a given faciity name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getFacilityIdByName : function(config, facilityName) {
            return config.facilities[facilityName].id;
        },


        /**
         * Returns the hierarchy for a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getHierarchyByFacilityName : function(config, facilityName) {
            return config.facilities[facilityName].hierarchy;
        },


        /**
         * Returns the browse column configuration for a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getColumnsByFacilityName : function(config, facilityName) {
            return config.facilities[facilityName].browseColumns;
        },


        /**
         * Returns the browse column config for a site
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        getFacilitiesColumns : function(config) {
            return config.site.facilitiesColumns;
        },

        /**
         * Return the site config
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        getSiteConfig : function(config) {
            return config.site;
        }

    };
}


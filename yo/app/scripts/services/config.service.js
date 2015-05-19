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
            if (typeof config.facilities !== 'undefined') {
                return config.facilities;
            } else {
                throw new Error('facilities not configured');
            }
        },

        /**
         * Returns a facility object by a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getFacilityByName : function(config, facilityName) {
            if (typeof config.facilities[facilityName] !== 'undefined') {
                return config.facilities[facilityName];
            } else {
                throw new Error('facility ' + facilityName  + ' not configured');
            }
        },

        /**
         * Returns the facility id by a given faciity name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getFacilityIdByName : function(config, facilityName) {
            if (typeof config.facilities[facilityName].id !== 'undefined') {
                return config.facilities[facilityName].id;
            } else {
                throw new Error('\'id\' for facility ' + facilityName  + ' not configured');
            }
        },


        /**
         * Returns the hierarchy for a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        getHierarchyByFacilityName : function(config, facilityName) {
            if (typeof config.facilities[facilityName].hierarchy !== 'undefined') {
                return config.facilities[facilityName].hierarchy;
            } else {
                throw new Error('\'hierarchy\' for facility ' + facilityName  + ' not configured');
            }
        },


        /**
         * Returns the browse column configuration for a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        /*getColumnsByFacilityName : function(config, facilityName) {
            if (typeof config.facilities[facilityName].browseColumns !== 'undefined') {
                return config.facilities[facilityName].browseColumns;
            } else {
                throw new Error('\'browseColumns\' for facility ' + facilityName  + ' not configured');
            }
        },*/


        /**
         * Returns the browse column config for a site
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        getFacilitiesColumns : function(config) {
            if (typeof config.site.facilitiesColumns !== 'undefined') {
                return config.site.facilitiesColumns;
            } else {
                throw new Error('\'facilitiesColumn\' not configured');
            }
        },

        /**
         * Return the site config
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        getSiteConfig : function(config) {
            if (typeof config.site !== 'undefined') {
                return config.site;
            } else {
                throw new Error('\'site\' not configured');
            }
        },


        getSitePagingType: function(config) {
            if (typeof config.site.pagingType !== 'undefined') {
                if (config.site.pagingType === 'scroll' || config.site.pagingType === 'page') {
                    return config.site.pagingType;
                } else {
                    throw new Error('\'pagingType\' must be \'page\' or \'scroll\'');
                }
            } else {
                throw new Error('\'pagingType\' not configured for site');
            }
        },

        getSitePageSize: function(config, pagingType) {
            if (pagingType === 'page') {
                return config.site.paginationNumberOfRows;
            }

            if (pagingType === 'scroll') {
                return config.site.scrollPageSize;
            } else {
                throw new Error('\'pagingType\' must be \'page\' or \'scroll\'');
            }
        },

        getSiteScrollRowFromEnd : function(config) {
            if (typeof config.site.scrollRowFromEnd !== 'undefined') {
                return config.site.scrollRowFromEnd;
            } else {
                throw new Error('\'scrollRowFromEnd\' not set for site');
            }
        },

        /**
         * Return the realtions to be shown in meta tabs
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @param  {[type]} entityType   [description]
         * @return {[type]}              [description]
         */
        getMetaTabsByEntityType : function(config, facilityName, entityType) {
            return config.facilities[facilityName].metaTabs[entityType];
        },

        /**
         * [getFacilitiesMetaTabs description]
         * @return {[type]} [description]
         */
        getSiteFacilitiesMetaTabs : function(config) {
            return config.site.facility.metaTabs.facility;
        },


        getSiteFacilitiesGridOptions : function(config) {
            if (typeof config.site.facilitiesGridOptions !== 'undefined') {
                return config.site.facilitiesGridOptions;
            } else {
                throw new Error('\'facilitiesGridOptions\' not configured');
            }
        },


        getBrowseOptionsByFacilityName : function(config, facilityName) {
            if (typeof config.facilities[facilityName].browseOptions !== 'undefined') {
                return config.facilities[facilityName].browseOptions;
            } else {
                throw new Error('\'browseOptions\' for facility \'' + facilityName + '\' not configured');
            }
        },

        getEntityBrowseOptionsByFacilityName : function(config, facilityName, entityType) {
            var browseOptions = this.getBrowseOptionsByFacilityName(config, facilityName);

            if (typeof browseOptions[entityType] !== 'undefined') {
                return browseOptions[entityType];
            } else {
                throw new Error('\'browseOptions\' for \'entityType\' ' + entityType + ' for facility \'' + facilityName + '\' not configured');
            }
        }

    };
}


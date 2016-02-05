(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('Config', Config);

    Config.$inject = ['APP_CONFIG'];

    function Config(APP_CONFIG){


        /**
         * Return the list of facilities from the config
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        this.getFacilities = function(config) {
            if (typeof config.facilities !== 'undefined') {
                return config.facilities;
            } else {
                throw new Error('facilities not configured');
            }
        };

        /**
         * Returns a facility object by a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        this.getFacilityByName = function(config, facilityName) {
            if (typeof config.facilities[facilityName] !== 'undefined') {
                return config.facilities[facilityName];
            } else {
                throw new Error('facility ' + facilityName  + ' not configured');
            }
        };

        /**
         * Returns the facility id by a given faciity name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        this.getFacilityIdByName = function(config, facilityName) {
            if (typeof config.facilities[facilityName].id !== 'undefined') {
                return config.facilities[facilityName].id;
            } else {
                throw new Error('\'id\' for facility ' + facilityName  + ' not configured');
            }
        };

        this.getFacilityTitleByFacilityName = function(config, facilityName) {
            if (typeof config.facilities[facilityName].title !== 'undefined') {
                return config.facilities[facilityName].title;
            } else {
                throw new Error('\'title\' for facility ' + facilityName  + ' not configured');
            }
        };


        /**
         * Returns the hierarchy for a given facility name
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @return {[type]}              [description]
         */
        this.getHierarchyByFacilityName = function(config, facilityName) {
            if (typeof config.facilities[facilityName].hierarchy !== 'undefined') {
                return config.facilities[facilityName].hierarchy;
            } else {
                throw new Error('\'hierarchy\' for facility ' + facilityName  + ' not configured');
            }
        };

        this.getDownloadTransportTypeByFacilityName = function(config, facilityName) {
            if (typeof config.facilities[facilityName].downloadTransportType !== 'undefined') {
                return config.facilities[facilityName].downloadTransportType;
            } else {
                throw new Error('\'downloadTransportType\' for facility ' + facilityName  + ' not configured');
            }
        };


        /**
         * Returns the browse column config for a site
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        this.getFacilitiesColumns = function(config) {
            if (typeof config.site.facilitiesColumns !== 'undefined') {
                return config.site.facilitiesColumns;
            } else {
                throw new Error('\'facilitiesColumn\' not configured');
            }
        };

        /**
         * Return the site config
         * @param  {[type]} config [description]
         * @return {[type]}        [description]
         */
        this.getSiteConfig = function(config) {
            if (typeof config.site !== 'undefined') {
                return config.site;
            } else {
                throw new Error('\'site\' not configured');
            }
        };

        this.getPaging = function(config) {
            if (typeof config.site.paging !== 'undefined') {
                return config.site.paging;
            } else {
                throw new Error('\'paging\' object not configured for site');
            }
        };

        this.getSitePagingType = function(config) {
            var paging = this.getPaging(config);

            if (typeof paging.pagingType !== 'undefined') {
                if (paging.pagingType === 'scroll' || paging.pagingType === 'page') {
                    return paging.pagingType;
                } else {
                    throw new Error('\'paging.pagingType\' must be \'page\' or \'scroll\'');
                }
            } else {
                throw new Error('\'paging.pagingType\' not configured for site');
            }
        };

        this.getSitePageSize = function(config, pagingType) {
            var paging = this.getPaging(config);

            if (pagingType === 'page') {
                return paging.paginationNumberOfRows;
            }

            if (pagingType === 'scroll') {
                if (typeof paging.scrollPageSize !== 'undefined') {
                    return paging.scrollPageSize;
                } else {
                    throw new Error('\'paging.scrollPageSize\' not configure for site');
                }
            } else {
                throw new Error('\'paging.pagingType\' must be \'page\' or \'scroll\'');
            }
        };

        this.getPaginationPageSizes = function(config, pagingType) {
            if (pagingType === 'page') {
                var paging = this.getPaging(config);

                if (typeof paging.paginationPageSizes !== 'undefined') {
                    return paging.paginationPageSizes;
                } else {
                    throw new Error('\'paging.paginationPageSizes\' not set for site');
                }
            }
        };

        this.getSiteScrollRowFromEnd = function(config, pagingType) {
            if (pagingType === 'scroll') {
                var paging = this.getPaging(config);

                if (typeof paging.scrollRowFromEnd !== 'undefined') {
                    return paging.scrollRowFromEnd;
                } else {
                    throw new Error('\'paging.scrollRowFromEnd\' not set for site');
                }
            }
        };

        /**
         * Return the realtions to be shown in meta tabs
         * @param  {[type]} config       [description]
         * @param  {[type]} facilityName [description]
         * @param  {[type]} entityType   [description]
         * @return {[type]}              [description]
         */
        this.getMetaTabsByEntityType = function(config, facilityName, entityType) {
            return config.facilities[facilityName].metaTabs[entityType];
        };

        /**
         * [getFacilitiesMetaTabs description]
         * @return {[type]} [description]
         */
        this.getSiteFacilitiesMetaTabs = function(config) {
            if (typeof config.site.metaTabs !== 'undefined') {
                if (typeof config.site.metaTabs.facility !== 'undefined') {
                    return config.site.metaTabs.facility;
                } else {
                    throw new Error('\'metaTab.facility\' not set for site');
                }
            } else {
                throw new Error('\'metaTab\' not set for site');
            }
        };


        this.getSiteFacilitiesGridOptions = function(config) {
            if (typeof config.site.facilitiesGridOptions !== 'undefined') {
                return config.site.facilitiesGridOptions;
            } else {
                if (this.getFacilities.length > 1) {
                    throw new Error('\'facilitiesGridOptions\' not configured');
                }
            }
        };

        this.getSiteCartGridOptions = function(config) {
            if (typeof config.site.cartGridOptions !== 'undefined') {
                return config.site.cartGridOptions;
            } else {
                throw new Error('\'cartGridOptions\' not configured');
            }
        };

        this.getSiteMyDataGridOptions = function(config) {
            if (typeof config.site.myDataGridOptions !== 'undefined') {
                return config.site.myDataGridOptions;
            } else {
                throw new Error('\'myDataGridOptions\' not configured');
            }
        };

        this.getSiteMyDownloadGridOptions = function(config) {
            if (typeof config.site.myDownloadGridOptions !== 'undefined') {
                return config.site.myDownloadGridOptions;
            } else {
                throw new Error('\'myDataGridOptions\' not configured');
            }
        };

        this.getSiteMyDataGridEntityType = function(config) {
            if (typeof config.site.myDataGridOptions !== 'undefined') {
                if (typeof config.site.myDataGridOptions.entityType !== 'undefined') {
                    if (config.site.myDataGridOptions.entityType === 'investigation' || config.site.myDataGridOptions.entityType === 'dataset') {
                        //check all configured hierarchy has the entity type
                        var facilities = this.getFacilities(APP_CONFIG);
                        _.each(facilities, function(facility) {

                            if (facility.hierarchy.indexOf(config.site.myDataGridOptions.entityType) === -1) {
                                throw new Error('A configured hierarchy does not have the entity \'' + config.site.myDataGridOptions.entityType + '\'. All configured facility hierarchy must have the \'' + config.site.myDataGridOptions.entityType + '\' entity which you have configured for myDataGridOptions');
                            }
                        });

                        return config.site.myDataGridOptions.entityType;
                    } else {
                        throw new Error('Only \'investigation\' and \'dataset\' entityType is supported for \'myDataGridOptions\' configuration');
                    }
                } else {
                    throw new Error('\'myDataGridOptions\' entityType not configured');
                }
            } else {
                throw new Error('\'myDataGridOptions\' not configured');
            }
        };

        this.getSiteHome = function(config) {
            return config.site.home;
        };

        this.getEuCookieLaw = function(config) {
            return config.site.enableEuCookieLaw;
        };

        this.getBrowseGridOptionsByFacilityName = function(config, facilityName) {
            if (typeof config.facilities[facilityName].browseGridOptions !== 'undefined') {
                return config.facilities[facilityName].browseGridOptions;
            } else {
                throw new Error('\'browseGridOptions\' for facility \'' + facilityName + '\' not configured');
            }
        };

        this.getEntityBrowseGridOptionsByFacilityName = function(config, facilityName, entityType) {
            var browseGridOptions = this.getBrowseGridOptionsByFacilityName(config, facilityName);

            if (typeof browseGridOptions[entityType] !== 'undefined') {
                return browseGridOptions[entityType];
            } else {
                throw new Error('\'browseGridOptions\' for \'entityType\' ' + entityType + ' for facility \'' + facilityName + '\' not configured');
            }
        };

        this.getPages = function(config) {
            return config.site.pages;
        };
    }
})();

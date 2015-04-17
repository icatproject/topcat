(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseFacilitiesController', BrowseFacilitiesController);

    BrowseFacilitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', 'Config', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage'];

    function BrowseFacilitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG, Config, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage) {
        var vm = this;
        //var facility = 0;
        var server = 'dls-server';
        var dtOptions = {}; //dtoptions for the datatable
        var dtColumns = []; //dtColumns for the datatable
        var pagingType = Config.getSiteConfig(APP_CONFIG).pagingType; //the pagination type. 'scroll' or 'page'
        var currentEntityType = RouteUtils.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        //var structure = APP_CONFIG.servers[server].facility[facility].structure;
        var column = Config.getFacilitiesColumns(APP_CONFIG); //the facilities column configuration



        //var nextEntityType = getNextEntityType(structure, currentEntityType);
        var browseMaxRows = Config.getSiteConfig(APP_CONFIG).browseMaxRows;

        //vm.structure = structure;
        vm.currentEntityType = currentEntityType;

        if (! angular.isDefined($rootScope.cart)) {
            $rootScope.cart = [];
            $rootScope.ref = [];
        }

        console.log('$state:', $state);
        console.log('currentEntityType: ' + currentEntityType);

        vm.rowClickHandler = rowClickHandler;

        //var login = DataManager.login();
        //console.log(login);

        //var version = DataManager.getVersion();
        //console.log('icat version=', version);

        //determine paging style type. Options are page and scroll where scroll is the default
        switch (pagingType) {
            case 'page':
                dtOptions = DTOptionsBuilder.fromFnPromise(getDataPromise(Config.getFacilities(APP_CONFIG), $sessionStorage.sessions))
                    .withPaginationType('full_numbers')
                    .withDOM('frtip')
                    .withDisplayLength(browseMaxRows)
                    .withOption('fnRowCallback', rowCallback)
                    .withOption('autoWidth', false)
                    .withOption('aaSorting', ConfigUtils.getDefaultSortArray(column));
                break;
            case 'scroll':
                /* falls through */
            default:
                dtOptions = DTOptionsBuilder.fromFnPromise(getDataPromise(Config.getFacilities(APP_CONFIG, $sessionStorage.sessions)))
                    .withDOM('frti')
                    .withScroller()
                    .withOption('deferRender', true)
                    // Do not forget to add the scorllY option!!!
                    .withOption('scrollY', 180)
                    .withOption('fnRowCallback', rowCallback)
                    .withOption('autoWidth', false)
                    .withOption('aaSorting', ConfigUtils.getDefaultSortArray(column));
                break;
        }

        vm.dtOptions = dtOptions;

        /**
         * click handler when a row is clicked on the datatable
         * @param  {object} aData the row data object
         * @return {void}
         */
        function rowClickHandler(aData) {
            inform.add('Type: ' + currentEntityType + ' Id:' + aData.id, {
                'ttl': 3000,
                'type': 'info'
            }); //TODO update meta tab with details
        }

        /**
         * Row Callback
         * @param  {Object} nRow the row object
         * @param  {Object} aData the row data object
         * @return {Object} nRow the row object
         */
        function rowCallback(nRow, aData) {
            // Unbind first in order to avoid any duplicate handler
            angular.element('td', nRow).unbind('click');

            //bind click event to the td element of the row
            angular.element('td', nRow).bind('click', function() {
                $scope.$apply(function() {
                    vm.rowClickHandler(aData);
                });
            });

            //stop the link from firing the row click event
            angular.element('td a', nRow).bind('click', function(event) {
                event.stopPropagation();
            });

            angular.element(':checkbox', nRow).bind('click', function(event) {
                event.stopPropagation();
            });


            //perform a compile on the row
            $compile(nRow)($scope);

            return nRow;
        }


        /**
         * Get data based on the current ui-route
         *
         * @param  {string} currentRouteSegment the last segment of the ui-route name
         * @param  {Object} APP_CONFIG site configuration object
         * @return {Object} Promise object
         */
        function getDataPromise(facilities, $sessions) {
            return ConfigUtils.getLoggedInFacilitiesFromConfig(facilities, $sessions);
            //return ConfigUtils.getFacilitiesFromConfig(facilities);
        }



        //set the columns to display from config
        for (var i in column) {
            var col = DTColumnBuilder.newColumn(column[i].name).withTitle(column[i].title).withOption('defaultContent', '');

            if (angular.isDefined(column[i].checkbox) && column[i].checkbox === true) {
                col.renderWith(function(data, type, full) {
                    //return '<input type="checkbox" ng-model="cart.selected[' + full.id + ']"/>';

                    $rootScope.ref[full.id] = {
                        'id': full.id,
                        'server' : server,
                        'facility' : $stateParams.facility,
                        'entity' : currentEntityType

                    };

                    return '<input type="checkbox" checklist-model="cart" checklist-value="ref[' + full.id + ']" />';
                });
                dtColumns.push(col);
                continue;
            }

            //set the css class of the column
            if (angular.isDefined(column[i].style)) {
                col.withClass(column[i].style);
            }

            //hide the column
            if (angular.isDefined(column[i].visible)) {
                if (column[i].visible === false) {
                    col.notVisible();
                }
            }

            //set the column as not sortable
            if (angular.isDefined(column[i].sortable)) {
                if (column[i].notSortable === false) {
                    col.notSortable();
                }
            }



            //TODO should combine link and filter instead of filter overrriding the renderWith

            //create link if link is set to true
            if (angular.isDefined(column[i].link)) {
                if (column[i].link === true) {
                    col.renderWith(function(data, type, full, meta) {
                        if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true) {
                            //add facility id to $stateParams
                            $stateParams.id = full.id;

                            var structure = Config.getHierarchyByFacilityName(APP_CONFIG, full.name);
                            var nextRouteSegement = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);

                            if (currentEntityType === 'facility') {
                                return '<a ui-sref="home.browse.facilities.' + nextRouteSegement + '({facilityName : \'' + full.name + '\'})">' + full.title + '</a>';
                                //return '<a ui-sref="home.browse.facilities.' + getNextRouteSegmentName(structure, currentEntityType) + '({facility : \'' + full.id + '\', server: \'' +  full.server + '\'})">' + data + '</a>';
                            }
                        } else {
                            return full.title;
                        }
                    });
                }
            }


            //if expressionFilter is set
            if (angular.isDefined(column[i].expressionFilter)) {
                var expressionFilter = column[i].expressionFilter;

                if (angular.isDefined(expressionFilter.type)) {
                    if ('string' === expressionFilter.type) {
                        col.renderWith(function(data, type, full, meta) {
                            if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true) {
                                //add facility id to $stateParams
                                $stateParams.id = full.id;
                                var structure = Config.getHierarchyByFacilityName(APP_CONFIG, full.name);
                                var nextRouteSegement = RouteUtils.getNextRouteSegmentName(structure, currentEntityType);

                                return "<a ui-sref='home.browse.facilities." + nextRouteSegement + '(' + JSON.stringify($stateParams) + ")'>" + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + '</a>'; // jshint ignore:line
                            } else {
                                //no link
                                return $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters);
                            }
                        });
                    }

                    if (expressionFilter.type === 'date') {
                        col.renderWith(function(data, type, full, meta) {
                            return $filter('date')(data, column[meta.col].expressionFilter.format, column[meta.col].expressionFilter.timezone);
                        });
                    }

                    if (expressionFilter.type === 'bytes') {
                        col.renderWith(function(data, type, full, meta) {
                            return $filter('prettyBytes')(parseInt(data), column[meta.col].expressionFilter.format, column[meta.col].expressionFilter.timezone);
                        });
                    }


                    expressionFilter = undefined; //unset the filter
                }
            }

            dtColumns.push(col);

        }

        vm.dtColumns = dtColumns;
    }
})();
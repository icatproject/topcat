(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowseFacilitiesController', BrowseFacilitiesController);

    BrowseFacilitiesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', 'Config', 'DataManager', '$q', 'inform'];

    function BrowseFacilitiesController($rootScope, $scope, $state, $stateParams, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG, Config, DataManager, $q, inform) {
        var vm = this;
        //var facility = 0;
        var server = 'dls-server';
        var dtOptions = {}; //dtoptions for the datatable
        var dtColumns = []; //dtColumns for the datatable
        var pagingType = Config.getSiteConfig(APP_CONFIG).pagingType; //the pagination type. 'scroll' or 'page'
        var currentEntityType = getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        //var structure = APP_CONFIG.servers[server].facility[facility].structure;
        var column = Config.getFacilitiesColumns(APP_CONFIG); //the facilities column configuration

        //var nextEntityType = getNextEntityType(structure, currentEntityType);
        var browseMaxRows = APP_CONFIG.site.browseMaxRows;

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
                dtOptions = DTOptionsBuilder.fromFnPromise(getDataPromise(APP_CONFIG)) //TODO
                    .withPaginationType('full_numbers')
                    .withDOM('frtip')
                    .withDisplayLength(browseMaxRows)
                    .withOption('fnRowCallback', rowCallback)
                    .withOption('autoWidth', false);
                break;
            case 'scroll':
                /* falls through */
            default:
                dtOptions = DTOptionsBuilder.fromFnPromise(getDataPromise(APP_CONFIG)) //TODO
                    .withDOM('frti')
                    .withScroller()
                    .withOption('deferRender', true)
                    // Do not forget to add the scorllY option!!!
                    .withOption('scrollY', 180)
                    .withOption('fnRowCallback', rowCallback)
                    .withOption('autoWidth', false);
                break;
        }

        vm.dtOptions = dtOptions;


        /**
         * [getNextEntityType description]
         * @param  {Array} the array structure of the facility
         * @param  {String} the current entity type
         * @return {String || false} Returns the next item in the array or false if already last item
         */
        function getNextEntityType(structure, currentEntityType) {
            if (structure[structure.length] === currentEntityType) {
                return false;
            }

            var index;
            for (index = 0; index < structure.length; ++index) {
                if (structure[index] === currentEntityType) {
                    break;
                }
            }

            return structure[index + 1];
        }


        /**
         * Get the next ui-route name to go up the facility
         * structure
         *
         * @param  {[type]} structure         [description]
         * @param  {[type]} currentEntityType [description]
         * @return {[type]}                   [description]
         */
        function getNextRouteSegmentName(structure, currentEntityType) {
            var next = getNextEntityType(structure, currentEntityType);
            if (angular.isDefined(next)) {
                return currentEntityType + '-' + next;
            } else {
                return currentEntityType;
            }
        }


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
         * Returns the current entity type base on the current route param entityType.
         * Default to facility if non is specified
         *
         * @param  {Object} the $state object
         * @return {String} the current entity type
         */
        function getCurrentEntityType($state) {
            if (angular.isDefined($state.current.param)) {
                return $state.current.param.entityType || 'facility';
            }

            return 'facility';
        }


        /**
         * This is a dummy function to get a list of facilities
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
        function getFacilitiesFromConfig(APP_CONFIG){
            var facilities = Config.getFacilities(APP_CONFIG);
            var data = [];


            _.each(facilities, function(value){
                var obj = {};
                obj.id = value.facilityId;
                obj.name = value.name;
                obj.title = value.title;

                data.push(obj);
            });

            console.log('data', data);

            //we need to return a promise
            var deferred = $q.defer();
            deferred.resolve(data);

            return deferred.promise;
        }


        /**
         * Get data based on the current ui-route
         *
         * @param  {string} currentRouteSegment the last segment of the ui-route name
         * @param  {Object} APP_CONFIG site configuration object
         * @return {Object} Promise object
         */
        function getDataPromise(APP_CONFIG) {
            return getFacilitiesFromConfig(APP_CONFIG);
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

                            console.log('structure', structure);

                            var nextRouteSegement = getNextRouteSegmentName(structure, currentEntityType);

                            if (currentEntityType === 'facility') {
                                return '<a ui-sref="home.browse.facilities.' + nextRouteSegement + '({facilityName : \'' + full.name + '\'})">' + data + ' fff</a>';
                                //return '<a ui-sref="home.browse.facilities.' + getNextRouteSegmentName(structure, currentEntityType) + '({facility : \'' + full.id + '\', server: \'' +  full.server + '\'})">' + data + '</a>';
                            }
                        } else {
                            return data;
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

                                return "<a ui-sref='home.browse.facilities." + getNextRouteSegmentName(structure, currentEntityType) + '(' + JSON.stringify($stateParams) + ")'>" + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + '</a>'; // jshint ignore:line
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
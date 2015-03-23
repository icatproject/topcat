(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowsePanelContoller', BrowsePanelContoller);

    BrowsePanelContoller.$inject = ['$scope', '$state', '$stateParams', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', 'ICATService', '$q', 'inform'];

    function BrowsePanelContoller($scope, $state, $stateParams, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG, ICATService, $q, inform) {
        //$log.info(APP_CONFIG);

        var vm = this;
        var dtOptions = {}; //dtoptions for the datatable
        var dtColumns = []; //dtColumns for the datatable
        var pagingType = APP_CONFIG.site.pagingType; //the pagination type. 'scroll' or 'page'
        var currentEntityType = getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        var column = APP_CONFIG.servers[0].facility[0].browseColumns[currentEntityType]; //the column configuration
        var nextEntityType = getNextEntityType(APP_CONFIG.servers[0].facility[0].struture, currentEntityType);

        console.log(APP_CONFIG.servers[0].facility[0].struture);

        vm.rowClickHandler = rowClickHandler;


        //currentEntityType = $stateParams.facility;
        console.log($stateParams);
        console.log($state);
        console.log('currentEntityType:' + currentEntityType);

        pagingType = 'page';

        //determine paging style type. Options are page and scroll where scroll is the default
        switch (pagingType) {
            case 'page':
                dtOptions = DTOptionsBuilder.fromFnPromise(getData(currentEntityType, $state)) //TODO
                    .withPaginationType('full_numbers')
                    .withDOM('frtip')
                    .withDisplayLength(5)
                    .withOption('fnRowCallback', rowCallback);
                break;
            case 'scroll':
                /* falls through */
            default:
                dtOptions = DTOptionsBuilder.fromFnPromise(getData(currentEntityType, $state)) //TODO
                    .withDOM('frti')
                    .withScroller()
                    .withOption('deferRender', true)
                    // Do not forget to add the scorllY option!!!
                    .withOption('scrollY', 180)
                    .withOption('fnRowCallback', rowCallback);
                break;
        }


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
                console.log(structure[index]);
                if (structure[index] === currentEntityType) {
                    break;
                }
            }

            console.log('matching index found ' + structure[index] + ' index: ' + index);
            console.log('next index is ' + structure[index + 1] + ' index: ' + (index + 1));

            return structure[index + 1];

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
         * Call the correct function to returns the required data
         * based on the current entity type
         *
         * @param  {Object} the $state object
         * @return {Object} the data object
         */
        function getData(currentEntityType, $state) {
            console.log('entityType: ' + $state.params.entityType);
            console.log('facility: ' + $state.params.facility);
            console.log('id: ' + $state.params.id);

            switch (currentEntityType) {
                case 'facility':
                    console.log('function called: facilities');
                    return getFacilities();
                case 'investigation':
                    console.log('function called: getInvestigations');
                    return getInvestigations($state.params.entityType);
                case 'dataset':
                    console.log('function called: getDatasetByInvestigationId');
                    return getDatasetByInvestigationId($state.params.entityType);
                case 'datafile':
                    console.log('function called: getDatafilesByDatasetId');
                    return getDatafilesByDatasetId($state.params.entityType);
                default:
                    console.log('function called: default');
                    return getFacilities();
            }
        }


        function getFacilities() {
            var def = $q.defer();

            ICATService.getFacilties()
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }


        function getInvestigations(facilityId) {
            var def = $q.defer();

            ICATService.getInvestigations(facilityId)
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }


        function getInvestigationsByInstrumentId(instrumentId) { // jshint ignore:line
            var def = $q.defer();

            ICATService.getInvestigationsByInstrumentId(instrumentId)
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }


        function getDatasetByFacilityId(facilityId) { // jshint ignore:line
            var def = $q.defer();

            ICATService.getDatasetByFacilityId(facilityId)
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }


        function getDatasetByInvestigationId(investigationId) {
            var def = $q.defer();

            ICATService.getDatasetByInvestigationId(investigationId)
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }


        function getDatafilesByDatasetId(facilityId, investigationId) {
            var def = $q.defer();

            ICATService.getDatasetByInvestigationId(facilityId, investigationId)
                .success(function(data) {
                    def.resolve(data);
                })
                .error(function() {
                    def.reject('Failed to retrieve data');
                    inform.add('Failed to retrieve data', {
                        'ttl': 0,
                        'type': 'danger'
                    });
                });

            return def.promise;
        }



        vm.dtOptions = dtOptions;

        //set the columns to display from config
        for (var i in column) {
            var col = DTColumnBuilder.newColumn(column[i].name).withTitle(column[i].title).withOption('defaultContent', '');

            //set the css class of the column
            if (angular.isDefined(column[i].style)) {
                col.withClass(column[i].style);
            }

            //hide the column
            if (angular.isDefined(column[i].notVisible)) {
                if (column[i].notVisible === true) {
                    col.notVisible();
                }
            }

            //set the column as not sortable
            if (angular.isDefined(column[i].notSortable)) {
                if (column[i].notSortable === true) {
                    col.notSortable();
                }
            }

            console.log('nextEntityType: ' + nextEntityType);

            //TODO should combine link and filter instead of filter overrriding the renderWith

            //create link if link is set to true
            if (angular.isDefined(column[i].link)) {
                if (column[i].link === true) {
                    col.renderWith(function(data, type, full, meta) {
                        if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true && nextEntityType !== false) {
                            if (currentEntityType === 'facility') {
                                return '<a ui-sref="home.browse.main.facilities.' + nextEntityType + '({facility : \'' + full.name + '\'})">' + data + '</a>';
                            } else {
                                return '<a ui-sref="home.browse.main.facilities.' + nextEntityType + '({facility : \'' + $state.params.facility + '\'})">' + data + '</a>';
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

                            //console.log(full);

                            if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true && nextEntityType !== false) {
                                return '<a ui-sref="home.browse.main.facilities.' + nextEntityType + '({facility : \'' + $state.params.facility + '\'})">' + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + '</a>';
                            } else {
                                return $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters);
                            }
                        });
                    }

                    if (expressionFilter.type === 'date') {
                        col.renderWith(function(data, type, full, meta) {
                            return $filter('date')(data, column[meta.col].expressionFilter.format, column[meta.col].expressionFilter.timezone);
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
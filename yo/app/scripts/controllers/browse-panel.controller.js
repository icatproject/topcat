(function() {
    'use strict';

    /*jshint -W083 */
    angular
        .module('angularApp')
        .controller('BrowsePanelController', BrowsePanelController);

    BrowsePanelController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', 'Config', '$translate', 'ConfigUtils', 'RouteUtils', 'DataManager', '$q', 'inform', '$sessionStorage', 'DataTableAODataBuilder'];

    function BrowsePanelController($rootScope, $scope, $state, $stateParams, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG, Config, $translate, ConfigUtils, RouteUtils, DataManager, $q, inform, $sessionStorage, DataTableAODataBuilder) {
        $scope.message = null;
        var vm = this;
        //var facility = 0;
        var facilityName = $stateParams.facilityName;

        //var server = 'dls-server';
        var dtOptions = {}; //dtoptions for the datatable
        var dtColumns = []; //dtColumns for the datatable
        var pagingType = Config.getSiteConfig(APP_CONFIG).pagingType; //the pagination type. 'scroll' or 'page'
        var currentEntityType = RouteUtils.getCurrentEntityType($state); //possible options: facility, cycle, instrument, investigation dataset, datafile
        var structure = Config.getHierarchyByFacilityName(APP_CONFIG, facilityName);
        var column = Config.getColumnsByFacilityName(APP_CONFIG, facilityName)[currentEntityType]; //the column configuration
        var currentRouteSegment = RouteUtils.getCurrentRouteSegmentName($state);
        var nextEntityType = RouteUtils.getNextEntityType(structure, currentEntityType);
        var browseMaxRows = Config.getSiteConfig(APP_CONFIG).browseMaxRows;
        var sessions = $sessionStorage.sessions;
        var stateParamClone = angular.copy($stateParams); //we need this because for some reason when using the filter search box, the call changes the stateParam and change the id to something unexpected
        var REST_API_URL = 'https://localhost:3001/icat/entityManager';

        vm.structure = structure;
        vm.currentEntityType = currentEntityType;

        if (! angular.isDefined($rootScope.cart)) {
            $rootScope.cart = [];
            $rootScope.ref = [];
        }

        console.log('$state:', $state);
        console.log('$stateParams:', $stateParams);
        console.log('Current facility name :', facilityName);
        console.log('structure:', structure);
        console.log('currentEntityType: ' + currentEntityType);
        console.log('nextEntityType: ' + nextEntityType);
        console.log('currentRouteSegment: ' + currentRouteSegment);
        console.log('sessions: ', sessions);

        vm.rowClickHandler = rowClickHandler;

        //determine paging style type. Options are page and scroll where scroll is the default
        switch (pagingType) {
            case 'page':
                dtOptions = DTOptionsBuilder.newOptions()
                    .withOption('sAjaxSource', REST_API_URL)
                    .withFnServerData(function serverData(sSource, aoData, fnCallback, oSettings) {
                        var data = {};
                        _.each(aoData, function(obj) {
                            data[obj.name] = obj.value;
                        });

                        var queryParams = {
                            start: data.iDisplayStart,
                            numRows: data.iDisplayLength,
                            search: data.sSearch
                        };

                        var params = getAoDataParams(currentRouteSegment, APP_CONFIG, queryParams, stateParamClone);
                        console.log('params', params);

                        _.each(params, function(value, key){
                            aoData.push({
                                name: key,
                                value: value
                            });
                        });

                        oSettings.jqXHR = $.ajax({
                            'dataType': 'json',
                            'type': 'GET',
                            'url': sSource,
                            'data': aoData,
                            'success': fnCallback
                        });
                    })
                    .withOption('serverSide', true)
                    .withOption('fnServerParams', function(aoData) {
                        console.log('before', aoData);
                        aoData = [];
                        console.log('after', aoData);
                    })
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
                dtOptions = DTOptionsBuilder.newOptions()
                    .withOption('sAjaxSource', REST_API_URL)
                    .withFnServerData(function serverData(sSource, aoData, fnCallback, oSettings) {
                        var data = {};
                        _.each(aoData, function(obj) {
                            data[obj.name] = obj.value;
                        });

                        var queryParams = {
                            start: data.iDisplayStart,
                            numRows: data.iDisplayLength,
                            search: data.sSearch
                        };

                        var params = getAoDataParams(currentRouteSegment, APP_CONFIG, queryParams, stateParamClone);
                        console.log('params', params);

                        _.each(params, function(value, key){
                            aoData.push({
                                name: key,
                                value: value
                            });
                        });

                        oSettings.jqXHR = $.ajax({
                            'dataType': 'json',
                            'type': 'GET',
                            'url': sSource,
                            'data': aoData,
                            'success': fnCallback
                        });
                    })
                    .withOption('serverSide', true)
                    .withOption('fnServerParams', function(aoData) {
                        aoData = [];
                    })
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


        function getfaciityName () {
            return $stateParams.facilityName;
        }


        /**
         * click handler when a row is clicked on the datatable
         * @param  {object} aData the row data object
         * @return {void}
         */
        function rowClickHandler(aData) {
            $scope.message = {'type' : currentEntityType, 'id' : aData.id, facilityName: aData.facilityName};
            $rootScope.$broadcast('rowclick', $scope.message);
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

            aData.facilityName = getfaciityName();

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
        function getAoDataParams(currentRouteSegment, APP_CONFIG, queryParams, stateParamClone) {
            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);
            var mySessionId = ConfigUtils.getSessionValueForFacility(sessions, facility);
            var absUrl = true;

            switch (currentRouteSegment) {
                case 'facility-instrument':
                    console.log('DataTableAODataBuilder.getInstruments() called');

                    return DataTableAODataBuilder.getInstruments(mySessionId, facility, queryParams, absUrl);
                case 'facility-cycle':
                    console.log('DataTableAODataBuilder.getCycles() called');

                    return DataTableAODataBuilder.getCyclesByFacilityId(mySessionId, facility, queryParams, absUrl);
                case 'facility-investigation':
                    console.log('DataTableAODataBuilder.getInvestigations() called');

                    return DataTableAODataBuilder.getInvestigations(mySessionId, facility, queryParams, absUrl);
                case 'facility-dataset':
                    console.log('DataTableAODataBuilder.getDatasets() called');

                    return DataTableAODataBuilder.getDatasets(mySessionId, facility, queryParams, absUrl);
                case 'facility-datafile':
                    console.log('DataTableAODataBuilder.getDatafiles() called');

                    return DataTableAODataBuilder.getDatafiles(mySessionId, facility, queryParams, absUrl);
                case 'instrument-cycle':
                    console.log('DataTableAODataBuilder.getCyclesByInstruments() called');
                    queryParams.instrumentId = stateParamClone.id;

                    return DataTableAODataBuilder.getCyclesByInstruments(mySessionId, facility, queryParams, absUrl);
                case 'instrument-investigation':
                    console.log('DataTableAODataBuilder.getInvestigationsByInstrumentId() called');
                    queryParams.instrumentId = stateParamClone.id;

                    return DataTableAODataBuilder.getInvestigationsByInstrumentId(mySessionId, facility, queryParams, absUrl);
                case 'instrument-dataset':
                    console.log('DataTableAODataBuilder.getDatasetsByInstrumentId() called');

                    queryParams.instrumentId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatasetsByInstrumentId(mySessionId, facility, queryParams, absUrl);
                case 'instrument-datafile':
                    console.log('DataTableAODataBuilder.getDatafilesByInstrumentId() called');

                    queryParams.instrumentId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatafilesByInstrumentId(mySessionId, facility, queryParams, absUrl);
                case 'cycle-instrument':
                    console.log('DataTableAODataBuilder.getInstrumentsByCycleId() called');

                    queryParams.cycleId = stateParamClone.id;

                    return DataTableAODataBuilder.getInstrumentsByCycleId(mySessionId, facility, queryParams, absUrl);
                case 'cycle-investigation':
                    console.log('DataTableAODataBuilder.getCyclesByInvestigationId() called');

                    queryParams.cycleId = stateParamClone.id;

                    return DataTableAODataBuilder.getInvestigationsByCycleId(mySessionId, facility, queryParams, absUrl);
                case 'cycle-dataset':
                    console.log('DataTableAODataBuilder.getDatasetsByCycleId() called');

                    queryParams.cycleId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatasetsByCycleId(mySessionId, facility, queryParams, absUrl);
                case 'cycle-datafile':
                    console.log('DataTableAODataBuilder.getDatafilesByCycleId() called');

                    queryParams.cycleId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatafilesByCycleId(mySessionId, facility, queryParams, absUrl);
                case 'investigation-dataset':
                    console.log('DataTableAODataBuilder.getDatasetsByInvestigationId() called');

                    queryParams.investigationId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatasetsByInvestigationId(mySessionId, facility, queryParams, absUrl);
                case 'investigation-datafile':
                    console.log('DataTableAODataBuilder.getDatafilesByInvestigationId() called');

                    queryParams.investigationId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatafilesByInvestigationId(mySessionId, facility, queryParams, absUrl);
                case 'dataset-datafile':
                    console.log('DataTableAODataBuilder.getDatafilesByDatasetId() called');

                    queryParams.datasetId = stateParamClone.id;

                    return DataTableAODataBuilder.getDatafilesByDatasetId(mySessionId, facility, queryParams, absUrl);
                default:
                    console.log('default called');
                    return;
            }
        }


        /**
         * Get data based on the current ui-route
         *
         * @param  {string} currentRouteSegment the last segment of the ui-route name
         * @param  {Object} APP_CONFIG site configuration object
         * @return {Object} Promise object
         */
        /*function getDataPromise(currentRouteSegment, APP_CONFIG) {
            var facility = Config.getFacilityByName(APP_CONFIG, facilityName);

            switch (currentRouteSegment) {
                case 'facility-instrument':
                    console.log('function called: getInstruments');
                    return DataManager.getInstruments(sessions, facility);
                case 'facility-cycle':
                    console.log('function called: getCycles');
                    return DataManager.getCyclesByFacilityId(sessions, facility);
                case 'facility-investigation':
                    console.log('function called: getInvestigations');
                    return DataManager.getInvestigations(sessions, facility);
                case 'facility-dataset':
                    console.log('function called: getDatasets');
                    return DataManager.getDatasets(sessions, facility);
                case 'facility-datafile':
                    console.log('function called: getDatafiles');
                    return DataManager.getDatafiles(sessions, facility);
                case 'instrument-cycle':
                    console.log('function called: getCyclesByInstruments');
                    return DataManager.getCyclesByInstruments(sessions, facility, $stateParams.id);
                case 'instrument-investigation':
                    console.log('function called: getInvestigationsByInstrumentId');
                    return DataManager.getInvestigationsByInstrumentId(sessions, facility, $stateParams.id);
                case 'instrument-dataset':
                    console.log('function called: getDatasetsByInstrumentId');
                    return DataManager.getDatasetsByInstrumentId(sessions, facility, $stateParams.id);
                case 'instrument-datafile':
                    console.log('function called: getDatafilesByInstrumentId');
                    return DataManager.getDatafilesByInstrumentId(sessions, facility, $stateParams.id);
                case 'cycle-instrument':
                    console.log('function called: getInstrumentsByCycleId');
                    return DataManager.getInstrumentsByCycleId(sessions, facility, $stateParams.id);
                case 'cycle-investigation':
                    console.log('function called: getCyclesByInvestigationId');
                    return DataManager.getInvestigationsByCycleId(sessions, facility, $stateParams.id);
                case 'cycle-dataset':
                    console.log('function called: getDatasetsByCycleId');
                    return DataManager.getDatasetsByCycleId(sessions, facility, $stateParams.id);
                case 'cycle-datafile':
                    console.log('function called: getDatafilesByCycleId');
                    return DataManager.getDatafilesByCycleId(sessions, facility, $stateParams.id);
                case 'investigation-dataset':
                    console.log('function called: getDatasetsByInvestigationId');
                    return DataManager.getDatasetsByInvestigationId(sessions, facility, $stateParams.id);
                case 'investigation-datafile':
                    console.log('function called: getDatafilesByInvestigationId');
                    return DataManager.getDatafilesByInvestigationId(sessions, facility, $stateParams.id);
                case 'dataset-datafile':
                    console.log('function called: getDatafilesByDatasetId');
                    return DataManager.getDatafilesByDatasetId(sessions, facility, $stateParams.id);
                default:
                    console.log('function called: default');
                    return;
            }
        }*/



        //set the columns to display from config
        for (var i in column) {
            var columnTitle;

            if (angular.isDefined(column[i].title)) {
                columnTitle = column[i].title;
            }

            if (angular.isDefined(column[i].translateTitle)) {
                columnTitle = $translate.instant(column[i].translateTitle);
            }

            var col = DTColumnBuilder.newColumn(column[i].name).withTitle(columnTitle).withOption('defaultContent', '');

            if (angular.isDefined(column[i].checkbox) && column[i].checkbox === true) {
                col.renderWith(function(data, type, full) {
                    //return '<input type="checkbox" ng-model="cart.selected[' + full.id + ']"/>';

                    $rootScope.ref[full.id] = {
                        'id': full.id,
                        'facility' : facilityName,
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
                        if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true && nextEntityType !== false) {
                            //add facility id to $stateParams
                            $stateParams.id = full.id;

                            return "<a ui-sref='home.browse.facilities." + RouteUtils.getNextRouteSegmentName(structure, currentEntityType) + '(' + JSON.stringify($stateParams) + ")'>" + data + '</a>'; // jshint ignore:line

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
                            if (angular.isDefined(column[meta.col].link) && column[meta.col].link === true && nextEntityType !== false) {
                                //add facility id to $stateParams
                                $stateParams.id = full.id;

                                return "<a ui-sref='home.browse.facilities." + RouteUtils.getNextRouteSegmentName(structure, currentEntityType) + '(' + JSON.stringify($stateParams) + ")'>" + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + '</a>'; // jshint ignore:line
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
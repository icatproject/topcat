'use strict';

var BrowseDataController = ['$rootScope', '$state', '$scope', '$resource', '$log', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', function($rootScope, $state, $scope, $resource, $log, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG) {
    $scope.initialise = function() {

    };

    $log.info(APP_CONFIG);

    //holds the data-table object
    $scope.browseData = {};
    //holds the selected checkboxes
    $scope.selected = {};

    //holds the colum array
    var browseColumns = [];

    //var browseType = 'investigation';    //possible options: facility, cycle, instrument, investigation dataset, datafile
    var browseType = 'investigation';
    if(angular.isDefined($state.params['browse'])) {
        browseType = $state.params['browse'];
    }

    var column = APP_CONFIG.servers[0].facility[0].browseColumns[browseType];
    var pagingType = APP_CONFIG.site.pagingType;

    if(angular.isDefined($state.params['pagingType'])) {
        pagingType = $state.params['pagingType'];
    }

    var source = '';

    switch(browseType) {
    case 'investigation':
        source = '/data/investigations.json';
        break;
    case 'dataset':
    default:
        source = '/data/dataset.json';
        break;
}



    //set the columns to display from user configuration file
    //determine paging style type. Options are page and scroll where scroll is the default
    switch(pagingType) {
        case 'page':
            //$scope.browseData.dtOptions = DTOptionsBuilder.fromSource('/data/investigations.json')
            $scope.browseData.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
                return $resource(source).query().$promise;
            })
            .withPaginationType('full_numbers')
            .withDOM('frtip')
            .withDisplayLength(5)
            .withOption('createdRow', function (row, data, dataIndex) {
                // Recompiling so we can bind Angular directive to the DT
                $compile(angular.element(row).contents())($scope);
            });
            break;
        case 'scroll':
        default:
            //$scope.browseData.dtOptions = DTOptionsBuilder.fromSource('/data/investigations-small-set.json')
            $scope.browseData.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
                return $resource(source).query().$promise;
            })
            .withDOM('frti')
            .withScroller()
            .withOption('deferRender', true)
            // Do not forget to add the scorllY option!!!
            .withOption('scrollY', 180)
            .withOption('createdRow', function (row, data, dataIndex) {
                // Recompiling so we can bind Angular directive to the DT
                $compile(angular.element(row).contents())($scope);
            });
            break;
    }



    $log.info(column);

    //setup select checkbox column
    var checkboxCol = DTColumnBuilder.newColumn(null).withTitle('').notSortable()
        .renderWith(function(data, type, full, meta) {
        return '<input ng-model="selected[' + full.id + ']" type="checkbox">';
    });

    //add select checkbox column
    browseColumns.push(checkboxCol);

    for (var i in column) {
        var col = DTColumnBuilder.newColumn(column[i].name).withTitle(column[i].title).withOption('defaultContent', '');

        /*
        if (angular.isDefined(column[i].expressionFilter)) {
            $log.info(column[i].expressionFilter.name);
        }
        */

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

        //set the expressionFilter based on user config.
        //Current 2 options available:
        //"string" for truncating the number of characters using angular-truncate
        //"date" for formating ISO 8601 date string
        if (angular.isDefined(column[i].expressionFilter)) {
            var expressionFilter = column[i].expressionFilter;

            if (angular.isDefined(expressionFilter.type)) {
                //apply truncate filter
                if ('string' === expressionFilter.type) {
                    col.renderWith(function(data, type, full, meta){
                        return '<a ui-sref="home.browse-dataset({browseType : ' + browseType + ', id : ' + full.id + '})">' + $filter(column[meta.col - 1].expressionFilter.name)(data, column[meta.col - 1].expressionFilter.characters) + "</a>";
                    });
                }

                //apply date format filter
                if (expressionFilter.type === 'date') {
                    col.renderWith(function(data, type, full, meta){
                        return $filter('date')(data, column[meta.col - 1].expressionFilter.format, column[meta.col - 1].expressionFilter.timezone);
                    });
                }

                expressionFilter = undefined;
            }
        }

        //add column
        browseColumns.push(col);
    }

    $scope.browseData.dtColumns = browseColumns;


    $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams){
                $scope.data = toParams;
            });

    $scope.initialise();


}];

angular.module('angularApp').controller('BrowseDataController', BrowseDataController);
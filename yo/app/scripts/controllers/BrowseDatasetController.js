'use strict';

var BrowseDatasetController = ['$rootScope', '$state', '$scope', '$log', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', function($rootScope, $state, $scope, $log, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG) {
    $scope.initialise = function() {

    };

    $log.info(APP_CONFIG);

    $scope.browseData = {};

    var pagingType = APP_CONFIG.site.pagingType;

    //determine paging style type. Options are page and scroll where scroll is the default
    switch(pagingType) {
        case 'page':
            $scope.browseData.dtOptions = DTOptionsBuilder.fromSource('/data/investigations.json')
            .withPaginationType('full_numbers')
            .withDOM('frtip')
            .withDisplayLength(5)
            .withOption('fnRowCallback', function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                $compile(nRow)($scope);
            });
            break;
        case 'scroll':
        default:
            $scope.browseData.dtOptions = DTOptionsBuilder.fromSource('/data/investigations-small-set.json')
            .withDOM('frti')
            .withScroller()
            .withOption('deferRender', true)
            // Do not forget to add the scorllY option!!!
            .withOption('scrollY', 180)
            .withOption('fnRowCallback', function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                $compile(nRow)($scope);
            });
            break;
    }

    //set the columns to display from config
    var browseColumns = [];
    var browseType = 'dataset';    //possible options: facility, cycle, instrument, investigation dataset, datafile
    var column = APP_CONFIG.servers[0].facility[0].browseColumns[browseType];

    $log.info(column);

    for (var i in column) {
        $log.info("column: " + i);

        var col = DTColumnBuilder.newColumn(column[i].name).withTitle(column[i].title).withOption('defaultContent', '');

        if (angular.isDefined(column[i].expressionFilter)) {
            $log.info(column[i].expressionFilter.name);
        }

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

        //set the expressionFilter

        if (angular.isDefined(column[i].expressionFilter)) {
            var expressionFilter = column[i].expressionFilter;

            //console.log(expressionFilter);

            if (angular.isDefined(expressionFilter.type)) {
                if ('string' === expressionFilter.type) {
                    col.renderWith(function(data, type, full, meta){
                        return '<a ui-sref="home.browse-dataset({id : ' + full.id + '})">' + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + "</a>";
                    });
                }

                //TODO something is broken here, the filter only applies to the endDate column for some reason
                if (expressionFilter.type === 'date') {
                    col.renderWith(function(data, type, full, meta){
                        return $filter('date')(data, column[meta.col].expressionFilter.format, column[meta.col].expressionFilter.timezone);
                    });
                }

                expressionFilter = undefined;
            }
        }

        browseColumns.push(col);
    }

    $scope.browseData.dtColumns = browseColumns;


    $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams){
                $scope.data = toParams;
            });

    $scope.initialise();
}];

angular.module('angularApp').controller('BrowseDatasetController', BrowseDatasetController);
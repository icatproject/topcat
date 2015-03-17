'use strict';

var BrowsePanelCtrl = ['$rootScope', '$state', '$scope', '$filter', '$compile', 'DTOptionsBuilder', 'DTColumnBuilder', 'APP_CONFIG', function($rootScope, $state, $scope, $filter, $compile, DTOptionsBuilder, DTColumnBuilder, APP_CONFIG) {
  //$log.info(APP_CONFIG);

  //$state.transitionTo('home.browse');

  var vm = this;
  var dtOptions = {}; //dtoptions for the datatable
  var dtColumns = []; //dtColumns for the datatable
  var pagingType = APP_CONFIG.site.pagingType; //the pagination type. 'scroll' or 'page'
  var browseType = 'dataset';    //possible options: facility, cycle, instrument, investigation dataset, datafile
  var column = APP_CONFIG.servers[0].facility[0].browseColumns[browseType]; //the column configuration

  //determine paging style type. Options are page and scroll where scroll is the default
  switch(pagingType) {
    case 'page':
      dtOptions = DTOptionsBuilder.fromSource('data/investigations.json')
        .withPaginationType('full_numbers')
        .withDOM('frtip')
        .withDisplayLength(5)
        /*jshint -W083 */
        .withOption('fnRowCallback', function (nRow) {
          $compile(nRow)($scope);
        });
      break;
    case 'scroll':
    /* falls through */
    default:
      dtOptions = DTOptionsBuilder.fromSource('data/investigations-small-set.json')
        .withDOM('frti')
        .withScroller()
        .withOption('deferRender', true)
        // Do not forget to add the scorllY option!!!
        .withOption('scrollY', 180)
        /*jshint -W083 */
        .withOption('fnRowCallback', function (nRow) {
          $compile(nRow)($scope);
        });
      break;
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

    //set the expressionFilter
    if (angular.isDefined(column[i].expressionFilter)) {
      var expressionFilter = column[i].expressionFilter;

      if (angular.isDefined(expressionFilter.type)) {
        if ('string' === expressionFilter.type) {
          col.renderWith(function(data, type, full, meta){
            return '<a ui-sref="home.browse-dataset({id : ' + full.id + '})">' + $filter(column[meta.col].expressionFilter.name)(data, column[meta.col].expressionFilter.characters) + '</a>';
          });
        }

        //TODO something is broken here, the filter only applies to the endDate column for some reason
        if (expressionFilter.type === 'date') {
          col.renderWith(function(data, type, full, meta){
            return $filter('date')(data, column[meta.col].expressionFilter.format, column[meta.col].expressionFilter.timezone);
          });
        }

        expressionFilter = undefined; //unset the filter
      }
    }

    dtColumns.push(col);
  }

  vm.dtColumns = dtColumns;


  /*$rootScope.$on('$stateChangeStart',
    function(event, toState, toParams, fromState, fromParams){
      $scope.data = toParams;
    });*/
}];

angular.module('angularApp').controller('BrowsePanelCtrl', BrowsePanelCtrl);

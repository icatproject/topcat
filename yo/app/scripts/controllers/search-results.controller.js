(function() {
  'use strict';

  var app = angular.module('topcat');

  app.controller('SearchResultsController', function($stateParams, $scope, $rootScope, $q, $timeout, tc, helpers){
    var that = this;
    var facilities = $stateParams.facilities ? JSON.parse($stateParams.facilities) : [];
    var text = $stateParams.text;
    var startDate = $stateParams.startDate;
    var endDate = $stateParams.endDate;
    var parameters = $stateParams.parameters ? JSON.parse($stateParams.parameters) : [];
    var samples = $stateParams.samples ? JSON.parse($stateParams.samples) : [];
    var queryCommon = {};

    if(text) queryCommon.text = text;
    if(startDate && !endDate) endDate = "90000-12-31";
    if(endDate && !startDate) startDate = "00000-01-01";
    if(startDate && endDate){
      queryCommon.lower = startDate.replace(/-/g, '') + "0000";
      queryCommon.upper = endDate.replace(/-/g, '') + "2359";
    }
    if(parameters.length > 0){
        queryCommon.parameters = _.map(parameters, function(parameter){
          var out = {};
          out.name = parameter.name;
          if(parameter.valueType === 'text'){
              out.stringValue = parameter.value;
          } else if(parameter.valueType === 'number'){
              out.lowerNumericValue = parameter.value;
              out.upperNumericValue = parameter.value;
          } else if(parameter.valueType === 'date'){
              var date = parameter.value.replace(/-/g, '') + "0000";
              out.lowerDateValue = date;
              out.upperDateValue = date;
          }
          return out;
        });
    }
    if(samples.length > 0) queryCommon.samples = samples;
    var timeout = $q.defer();
    var _updateSelections;

    //creates a function that unsubscribes the callback from broadcast events
    var stopListeningForCartChanges =  $rootScope.$on('cart:change', function(){
      if(_updateSelections) _updateSelections();
    });
    
    $scope.$on('$destroy', function(){
        timeout.resolve();
        stopListeningForCartChanges();
    });

    this.investigation = $stateParams.investigation == 'true';
    this.dataset = $stateParams.dataset == 'true';
    this.datafile = $stateParams.datafile == 'true';
    if(this.investigation){
      this.currentTab = 'investigation';
    } else if(this.dataset){
      this.currentTab = 'dataset';
    } else if(this.datafile){
      this.currentTab = 'datafile';
    }

    var promises = [];
    _.each(['investigation', 'dataset', 'datafile'], function(type){
      if(!that[type]) return;
      createGridOptions.call(that, type);
    });
    this.isLoading = true;
    $q.all(promises).then(function(){
      that.isLoading = false;
    });

    this.browse = function(row){
      timeout.resolve();
      row.browse();
    };

    this.showTabs = function(row){
        $rootScope.$broadcast('rowclick', {
            'type': row.entity.entityType,
            'id' : row.entity.id,
            'facilityName': row.entity.facilityName
        });
    };

    this.getSize = function($event, entity){
            $event.stopPropagation();
            entity.getSize(timeout.promise);
        };

    function createGridOptions(type){
      var gridApi;
      var gridOptions = _.merge({data: [], appScopeProvider: that, enableSelectAll: false}, tc.config().search.gridOptions[type]);
      helpers.setupIcatGridOptions(gridOptions, type);
      gridOptions.useExternalPagination =  false;
      var filter = function(){ return true; };
      var sorter = function(){ return true; };

      gridOptions.onRegisterApi = function(_gridApi) {

        gridApi = _gridApi;
        updateResults();

        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
            var identity = _.pick(row.entity, ['facilityName', 'id']);
            if(_.find(gridApi.selection.getSelectedRows(), identity)){
                row.entity.addToCart(timeout.promise);
            } else {
                tc.user(row.entity.facilityName).cart(timeout.promise).then(function(cart){
                    if(cart.isCartItem(row.entity.entityType, row.entity.id)){
                        row.entity.deleteFromCart(timeout.promise);
                    }
                });
            }
        });

        gridApi.core.on.sortChanged($scope, function(grid, sortColumns){
          var _timeout = $timeout(function(){
              sorter = helpers.generateEntitySorter(sortColumns);
              updateResults();
          });
          timeout.promise.then(function(){ $timeout.cancel(_timeout); });
        });

        gridApi.core.on.filterChanged($scope, function(){
          var _timeout = $timeout(function(){
            filter = helpers.generateEntityFilter(gridOptions);
            updateResults();
          });
          timeout.promise.then(function(){ $timeout.cancel(_timeout); });
        });

        _updateSelections = updateSelections;

      };

      var query = _.merge(queryCommon, {target: type});
      var searchPromise = tc.search(facilities, timeout.promise, query);
      promises.push(searchPromise);

      var isFileCountColumnDef = _.select(gridOptions.columnDefs,  function(columnDef){ return columnDef.field == 'fileCount' }).length > 0;

      function getResults(){
        function processResults(results){
          var out = _.select(results, filter);
          out.sort(sorter);
          _.each(out, function(entity){
            if(isFileCountColumnDef && entity.getFileCount) {
              entity.getFileCount(timeout.promise);
            }
          });
          return out;
        }
        return searchPromise.then(processResults, function(){}, processResults);
      }

      function updateResults(){
        getResults().then(function(results){
          gridOptions.data = results;
          updateSelections();
        });
      }

      function updateSelections(){
        var _timeout = $timeout(function(){
          _.each(gridOptions.data, function(row){
              tc.user(row.facilityName).cart(timeout.promise).then(function(cart){
                if(gridApi){
                  if (cart.isCartItem(that.currentTab, row.id)){
                      gridApi.selection.selectRow(row);
                  } else {
                      gridApi.selection.unSelectRow(row);
                  }
                }
              });
          });
        });
        timeout.promise.then(function(){ $timeout.cancel(_timeout); });
      }

      this[type + "GridOptions"] = gridOptions;
    }

  });

})();


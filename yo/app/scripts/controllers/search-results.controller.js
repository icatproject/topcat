(function() {
  'use strict';

  var app = angular.module('angularApp');

  app.controller('SearchResultsController', function($stateParams, $scope, $rootScope, $q, $timeout, tc){
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
    this.visable = false;
    if(this.investigation){
      this.currentTab = 'investigation';
    } else if(this.dataset){
      this.currentTab = 'dataset';
    } else if(this.datafile){
      this.currentTab = 'datafile';
    }

    _.each(['investigation', 'dataset', 'datafile'], function(type){
      if(!that[type]) return;
      createGridOptions.call(that, type);
    })

    this.browse = function(row){
      timeout.resolve();
      row.browse();
    };

    function createGridOptions(type){
      var gridApi;
      var gridOptions = _.merge({data: [], appScopeProvider: this, enableSelectAll: false}, tc.config().search.gridOptions[type]);
      _.each(gridOptions.columnDefs, function(columnDef){
        if(columnDef.link && !columnDef.cellTemplate){
          if(typeof columnDef.link == "string"){
            columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.browse(row.entity.' + columnDef.link + ')">{{row.entity.' + columnDef.field + '}}</a></div>';
          } else {
            columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.browse(row.entity)">{{row.entity.' + columnDef.field + '}}</a></div>';
          }
        }
        if(columnDef.field == 'size'){
            columnDef.cellTemplate = '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
            columnDef.enableSorting = false;
            columnDef.enableFiltering = false;
        }
        if(columnDef.translateDisplayName){
            columnDef.displayName = columnDef.translateDisplayName;
            columnDef.headerCellFilter = 'translate';
        }
      });

      gridOptions.onRegisterApi = function(_gridApi) {
        gridApi = _gridApi;
        updateSelections();

        
        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
            if(_.find(gridApi.selection.getSelectedRows(), _.pick(row.entity, ['facilityName', 'id']))){
                row.entity.addToCart(timeout.promise);
            } else {
                row.entity.deleteFromCart(timeout.promise);
            }
        });

        _updateSelections = updateSelections;

      };

      var query = _.merge(queryCommon, {target: type});
      tc.search(facilities, timeout.promise, query).then(function(results){
        _.each(results, function(entity){
            entity.getSize(timeout.promise);
        });
        updateSelections();
      }, function(){

      }, function(results){
        gridOptions.data = results;
        updateSelections();
      });



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


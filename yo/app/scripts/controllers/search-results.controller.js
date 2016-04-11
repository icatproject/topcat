(function() {
  'use strict';

  var app = angular.module('angularApp');

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

    var promises = [];
    _.each(['investigation', 'dataset', 'datafile'], function(type){
      if(!that[type]) return;
      createGridOptions.call(that, type);
    });
    $q.all(promises).then(function(){
      _.each(['investigation', 'dataset', 'datafile'], function(type){
        if(!that[type]) return;
        var gridOptions = that[type + "GridOptions"];
        _.each(gridOptions.data, function(entity){
          entity.getSize(timeout.promise);
        });
      });
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

    function createGridOptions(type){
      var gridApi;
      var gridOptions = _.merge({data: [], appScopeProvider: that, enableSelectAll: false}, tc.config().search.gridOptions[type]);
      helpers.setupIcatGridOptions(gridOptions, type);
      gridOptions.useExternalPagination =  false;
      gridOptions.useExternalSorting =  false;
      gridOptions.useExternalFiltering =  false;


      gridOptions.onRegisterApi = function(_gridApi) {
        gridApi = _gridApi;
        updateSelections();


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

        gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows){
          var entitiesToAdd = {};
          var entitiesToRemove = {};

          _.each(rows, function(row){
              var identity = _.pick(row.entity, ['facilityName', 'id']);
              var facilityName = row.entity.facilityName;
              if(_.find(gridApi.selection.getSelectedRows(), identity)){
                  if(!entitiesToAdd[facilityName]) entitiesToAdd[facilityName] = [];
                  entitiesToAdd[facilityName].push({
                      entityType: row.entity.entityType.toLowerCase(),
                      entityId: row.entity.id
                  });
              } else {
                  if(!entitiesToRemove[facilityName]) entitiesToRemove[facilityName] = [];
                  entitiesToRemove[facilityName].push({
                      entityType: row.entity.entityType.toLowerCase(),
                      entityId: row.entity.id
                  });
              }
          });

          _.each(entitiesToAdd, function(entities, facilityName){
            if(entities.length > 0) tc.user(facilityName).addCartItems(entities);
          });
          _.each(entitiesToRemove, function(entities, facilityName){
            if(entities.length > 0) tc.user(facilityName).deleteCartItems(entities);
          });
        });

        _updateSelections = updateSelections;

      };

      var query = _.merge(queryCommon, {target: type});
      promises.push(tc.search(facilities, timeout.promise, query).then(function(results){
        updateSelections();
      }, function(){

      }, function(results){
        gridOptions.data = results;
        updateSelections();
      }));



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


(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('SearchResultsController', function($stateParams, $scope, $q, $timeout, tc, Cart, APP_CONFIG){
    	var that = this;
      var facilities = $stateParams.facilities ? JSON.parse($stateParams.facilities) : [];
    	var text = $stateParams.text;
    	var type = $stateParams.type;
    	var startDate = $stateParams.startDate;
    	var endDate = $stateParams.endDate;
      var parameters = $stateParams.parameters ? JSON.parse($stateParams.parameters) : [];
      var samples = $stateParams.samples ? JSON.parse($stateParams.samples) : [];
      var investigation = $stateParams.investigation == 'true';
      var dataset = $stateParams.dataset == 'true';
      var datafile = $stateParams.datafile == 'true';
      var gridApi;

      var timeout = $q.defer();
      $scope.$on('$destroy', function(){ timeout.resolve(); });

      var gridOptionsCommon = {data: [], appScopeProvider: this};
      var entityGridOptions = {};
      _.each(['investigation', 'dataset', 'datafile'], function(type){
        var gridOptions = _.merge(APP_CONFIG.site.searchGridOptions[type], gridOptionsCommon);
        entityGridOptions[type] = gridOptions;
        _.each(gridOptions.columnDefs, function(columnDef){
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
      });
      this.entityGridOptions = entityGridOptions;

      this.investigation = investigation;
      this.dataset = dataset;
      this.datafile = datafile;
      if(investigation){
        this.currentTab = 'investigation';
      } else if(dataset){
        this.currentTab = 'dataset';
      } else if(datafile){
        this.currentTab = 'datafile';
      }


     	var queryCommon = {}
     	if(text) queryCommon.text = text;
      if(startDate && !endDate) endDate = "90000-12-31";
      if(endDate && !startDate) startDate = "00000-01-01";
      if(startDate && endDate){
     	  queryCommon.lower = startDate.replace(/-/g, '') + "0000";
     	  queryCommon.upper = endDate.replace(/-/g, '') + "2359";
      }

      /*
      if(parameters.length > 0){
          query.parameters = _.map(parameters, function(parameter){
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
      if(samples.length > 0) query.samples = samples;
      */

      _.each(entityGridOptions, function(gridOptions, type){
          if(!that[type]) return;
          console.log('searching: ' + type);
          var query = _.merge(queryCommon, {target: type});
          tc.search(facilities, timeout.promise, query).then(function(results){
            _.each(results, function(entity){
                entity.getSize(timeout.promise);
            });
          }, function(){

          }, function(results){
            gridOptions.data = results;
            /*
            $timeout(function(){
                _.each(results, function(row){
                    if (Cart.hasItem(row.facilityName, type, row.id)) {
                        gridApi.selection.selectRow(row);
                    } else {
                        gridApi.selection.unSelectRow(row);
                    }
                });
            });
*/
          });
      });

      function addItem(row){
          Cart.addItem(row.facilityName, type.toLowerCase(), row.id, row.name, []);
      }

      function removeItem(row){
          Cart.removeItem(row.facilityName, type.toLowerCase(), row.id);
      }
      /*
      gridOptions.onRegisterApi = function(_gridApi) {
          gridApi = _gridApi;

          gridApi.selection.on.rowSelectionChanged($scope, function(row) {
              if(_.find(gridApi.selection.getSelectedRows(), _.pick(row.entity, ['facilityName', 'id']))){
                  addItem(row.entity);
              } else {
                  removeItem(row.entity);
              }
          });

          gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
              _.each(rows, function(row){
                  if(_.find(gridApi.selection.getSelectedRows(), _.pick(row.entity, ['facilityName', 'id']))){
                      addItem(row.entity.entity);
                  } else {
                      removeItem(row.entity);
                  }
              });
          });
      };
      */

      this.browse = function(row){
        timeout.resolve();
        row.browse();
      };

    });


})();
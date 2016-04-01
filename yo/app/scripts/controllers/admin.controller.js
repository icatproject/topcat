
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, $scope, $state, $timeout, $q, tc, helpers){
    	var that = this;
      var page = 1;
      var gridApi;
      var pageSize = 10;
      var filters = ["1 = 1"];
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){
          timeout.resolve();
      });

    	this.facilities = tc.adminFacilities();

      if($state.params.facilityName == ''){
          $state.go('admin', {facilityName: this.facilities[0].config().facilityName});
          return;
      }
      var admin = tc.admin($state.params.facilityName);

      this.gridOptions = _.merge({data: [], appScopeProvider: this}, admin.facility().config().admin.gridOptions);
      helpers.setupTopcatGridOptions(this.gridOptions, 'download');
    
      this.gridOptions.columnDefs.push({
          name : 'actions',
          visible: true,
          translateDisplayName: 'BROWSE.COLUMN.ACTIONS.NAME',
          enableFiltering: false,
          enable: false,
          enableColumnMenu: false,
          enableSorting: false,
          enableHiding: false,
          cellTemplate : '<div class="ui-grid-cell-contents"><button ng-click="grid.appScope.delete(row.entity)" ng-show="!row.entity.isDeleted">Delete</button><button ng-click="grid.appScope.restore(row.entity)" ng-show="row.entity.isDeleted">Restore</button> <button ng-click="grid.appScope.pause(row.entity)" ng-show="row.entity.status == \'RESTORING\'">Pause</button><button ng-click="grid.appScope.resume(row.entity)" ng-show="row.entity.status == \'PAUSED\'">Resume</button></div>'
      });

      function updateScroll(resultCount){
          $timeout(function(){
              var isMore = resultCount == pageSize;
              if(page == 1) gridApi.infiniteScroll.resetScroll(false, isMore);
              gridApi.infiniteScroll.dataLoaded(false, isMore);
          });
      }

      function getPage(){
        return admin.downloads(filters).then(function(downloads){
          _.each(downloads, function(download){ download.getSize(timeout.promise); });
          return downloads;
        });
      }

      function updateFilterQuery(){
          filters = ['1 = 1'];
          _.each(that.gridOptions.columnDefs, function(columnDef){
              if(columnDef.type == 'date' && columnDef.filters){
                  var from = columnDef.filters[0].term || '';
                  var to = columnDef.filters[1].term || '';
                  if(from != '' || to != ''){
                      from = helpers.completePartialFromDate(from);
                      to = helpers.completePartialToDate(to);
                      filters.push([
                          "and download.? between {ts ?} and {ts ?}",
                          columnDef.field.safe(),
                          from,
                          to
                      ]);
                  }
              } else if(columnDef.type == 'string' && columnDef.filter && columnDef.filter.term) {
                  filters.push([
                      "and UPPER(download.?) like concat('%', ?, '%')", 
                      columnDef.field.safe(),
                      columnDef.filter.term.toUpperCase()
                  ]);
              }
          });
      }

      this.pause = function(download){
        admin.setDownloadStatus(download.id, 'PAUSED').then(function(){
          download.status = 'PAUSED';
        });
      };

      this.resume = function(download){
        admin.setDownloadStatus(download.id, 'RESTORING').then(function(){
          download.status = 'RESTORING';
        });
      };

      this.delete = function(download){
        admin.deleteDownload(download.id).then(function(){
          download.isDeleted = true;
        });
      };

      this.restore = function(download){
        admin.restoreDownload(download.id).then(function(){
          download.isDeleted = false;
        });
      };

    	this.gridOptions.onRegisterApi = function(_gridApi) {
            gridApi = _gridApi;

            getPage().then(function(downloads){
                that.gridOptions.data = downloads;
                updateScroll(downloads.length);
            });

            gridApi.core.on.filterChanged($scope, function() {
                page = 1;

                updateFilterQuery();

                getPage().then(function(downloads){
                    that.gridOptions.data = downloads;
                    updateScroll(downloads.length);
                });
            });

            gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                page++;
                getPage().then(function(downloads){
                    _.each(downloads, function(download){ that.gridOptions.data.push(download); });
                    updateScroll(downloads.length);
                });
            });

        };

    });

})();
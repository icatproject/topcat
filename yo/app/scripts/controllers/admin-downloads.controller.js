
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminDownloadsController', function($translate, $scope, $state, $timeout, $q, tc, helpers){
    	var that = this;
      var page = 1;
      var gridApi;
      var pageSize = 10;
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){
          timeout.resolve();
      });

      this.facilities = tc.adminFacilities();

      if(!$state.params.facilityName){
        $state.go('admin.downloads', {facilityName: this.facilities[0].config().name});
        return;
      } 

      var admin = tc.admin($state.params.facilityName);

      this.gridOptions = _.merge({data: [], appScopeProvider: this}, admin.facility().config().admin.gridOptions);
      helpers.setupTopcatGridOptions(this.gridOptions, 'download');
    
      this.gridOptions.columnDefs.push({
          name : 'actions',
          visible: true,
          title: 'BROWSE.COLUMN.ACTIONS.NAME',
          enableFiltering: false,
          enable: false,
          enableColumnMenu: false,
          enableSorting: false,
          enableHiding: false,
          cellTemplate : '<div class="ui-grid-cell-contents"><button ng-click="grid.appScope.delete(row.entity)" ng-show="!row.entity.isDeleted">Delete</button><button ng-click="grid.appScope.restore(row.entity)" ng-show="row.entity.isDeleted">Restore</button> <button ng-click="grid.appScope.pause(row.entity)" ng-show="row.entity.status == \'RESTORING\'">Pause</button><button ng-click="grid.appScope.resume(row.entity)" ng-show="row.entity.status == \'PAUSED\'">Resume</button></div>'
      });

      var sortColumns = [];
      _.each(this.gridOptions.columnDefs, function(columnDef){
        if(columnDef.sort){
          sortColumns.push({
            colDef: {field: columnDef.field},
            sort: columnDef.sort
          })
        }
      });
      sortColumns = _.sortBy(sortColumns, function(sortColumn){
        return sortColumn.sort.priority;
      });

      function updateScroll(resultCount){
          $timeout(function(){
              var isMore = resultCount == pageSize;
              if(page == 1) gridApi.infiniteScroll.resetScroll(false, isMore);
              gridApi.infiniteScroll.dataLoaded(false, isMore);
          });
      }

      function getPage(){
        return admin.downloads(generateQuery());
      }

      function generateQuery(){
          var out = ['1 = 1'];
          _.each(that.gridOptions.columnDefs, function(columnDef){
              if(columnDef.type == 'date' && columnDef.filters){
                  var from = columnDef.filters[0].term || '';
                  var to = columnDef.filters[1].term || '';
                  if(from != '' || to != ''){
                      from = helpers.completePartialFromDate(from);
                      to = helpers.completePartialToDate(to);
                      out.push([
                          "and download.? between {ts ?} and {ts ?}",
                          columnDef.field.safe(),
                          from,
                          to
                      ]);
                  }
              } else if(columnDef.type == 'string' && columnDef.filter && columnDef.filter.term) {
                  out.push([
                      "and UPPER(download.?) like concat('%', ?, '%')", 
                      columnDef.field.safe(),
                      columnDef.filter.term.toUpperCase()
                  ]);
              }
          });
          
          if(sortColumns.length > 0){
            out.push("order by");
            _.each(sortColumns, function(sortColumn, i){
                if(sortColumn.colDef){
                    out.push(['download.? ?', sortColumn.colDef.field.safe(), sortColumn.sort.direction.safe()]);
                    if(i < sortColumns.length - 1) out.push(',');
                }
            });
            // Always order by ID to force an order on rows that are otherwise sort-identical;
            // This should avoid pagination duplication problems - see issue #453
            out.push(", download.id asc");
          }

          out.push(["limit ?, ?", (page - 1) * pageSize, pageSize])          

          return out;
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
                getPage().then(function(downloads){
                    that.gridOptions.data = downloads;
                    updateScroll(downloads.length);
                });
            });

            gridApi.core.on.sortChanged($scope, function(grid, _sortColumns){
                sortColumns = _sortColumns;
                page = 1;
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
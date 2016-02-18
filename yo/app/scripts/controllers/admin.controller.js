
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.controller('AdminController', function($translate, $scope, $state, $timeout, $q, tc){
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

      this.gridOptions = _.merge({
          data: [],
          appScopeProvider: this,
          enableFiltering: true,
          enableSelectAll: false,
          enableRowSelection: false,
          enableRowHeaderSelection: false,
          infiniteScrollDown: true,
          useExternalPagination: true,
          useExternalFiltering: true
      }, tc.config().adminGridOptions);

      _.each(this.gridOptions.columnDefs, function(columnDef){
          columnDef.enableSorting = false;
          columnDef.enableHiding = false;
          columnDef.enableColumnMenu = false;

          if(columnDef.type == 'date'){
            columnDef.filterHeaderTemplate = '<div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[0].term" placeholder="From..."></div><div class="ui-grid-filter-container" datetime-picker ng-model="col.filters[1].term" placeholder="To..."></div>';
          }

          if(columnDef.field == 'size'){
              columnDef.cellTemplate = columnDef.cellTemplate || '<div class="ui-grid-cell-contents"><span us-spinner="{radius:2, width:2, length: 2}"  spinner-on="row.entity.size === undefined" class="grid-cell-spinner"></span><span>{{row.entity.size|bytes}}</span></div>';
              columnDef.enableSorting = false;
              columnDef.enableFiltering = false;
          }
          
      });

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

      if($state.params.facilityName == ''){
          $state.go('admin', {facilityName: this.facilities[0].config().facilityName});
          return;
      }

      var admin = tc.admin($state.params.facilityName);

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
                      from = completePartialFromDate(from);
                      to = completePartialToDate(to);
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

      function completePartialFromDate(date){
            var segments = date.split(/[-:\s]+/);
            var year = segments[0];
            var month = segments[1] || "01";
            var day = segments[2] || "01";
            var hours = segments[3] || "00";
            var minutes = segments[4] || "00";
            var seconds = segments[5] || "00";

            year = year + '0000'.slice(year.length, 4);
            month = month + '00'.slice(month.length, 2);
            day = day + '00'.slice(day.length, 2);
            hours = hours + '00'.slice(hours.length, 2);
            minutes = minutes + '00'.slice(minutes.length, 2);
            seconds = seconds + '00'.slice(seconds.length, 2);

            if(parseInt(month) == 0) month = '01';
            if(parseInt(day) == 0) day = '01';

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        }

        function completePartialToDate(date){
            var segments = date.split(/[-:\s]+/);
            var year = segments[0] || "";
            var month = segments[1] || "";
            var day = segments[2] || "";
            var hours = segments[3] || "23";
            var minutes = segments[4] || "59";
            var seconds = segments[5] || "59";
            year = year + '9999'.slice(year.length, 4);
            month = month + '99'.slice(month.length, 2);
            day = day + '99'.slice(day.length, 2);
            hours = hours + '33'.slice(hours.length, 2);
            minutes = minutes + '99'.slice(minutes.length, 2);
            seconds = seconds + '99'.slice(seconds.length, 2);

            if(parseInt(month) > 12) month = '12';
            var daysInMonth = new Date(year, day, 0).getDate();
            if(parseInt(day) > daysInMonth) day = daysInMonth;

            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
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
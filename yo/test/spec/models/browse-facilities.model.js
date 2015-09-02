'use strict';

//var mockConfigService;

describe('Service: BrowseFacilitiesModel', function() {
    var JSONFile = {};

    //load mock json file
    beforeEach(function() {
        JSONFile = readJSON('test/mock/data/mock-config-multi.json');
    });

    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', JSONFile);
            $provide.constant('SMARTCLIENTPING', {ping: 'offline'});
        });
    });


    // load the service's module
    beforeEach(module('angularApp'));

    var uiGridConstants;
    beforeEach(inject(function(_uiGridConstants_) {
        uiGridConstants = _uiGridConstants_;
    }));


    // instantiate service
    var BrowseFacilitiesModel;
    beforeEach(inject(function(_BrowseFacilitiesModel_) {
        BrowseFacilitiesModel = _BrowseFacilitiesModel_;
    }));


    it('should do something', function() {
        expect(!!BrowseFacilitiesModel).toBe(true);
    });


    it('configToUIGridOptions', function() {
        var gridOptions = BrowseFacilitiesModel.configToUIGridOptions(JSONFile);

        //console.log(JSON.stringify(gridOptions, null, 2));

        expect(gridOptions).toEqual({
          'enableFiltering': true,
          'columnDefs': [
            {
              'field': 'fullName',
              'displayName': 'BROWSE.COLUMN.FACILITY.FULLNAME',
              'enableSorting': false,
              'sortDirection': 'asc',
              'filter': {
                'condition': 2,
                'placeholder': 'Containing...',
                'flags': {
                  'caseSensitive': false
                },
                'type': 'input',
                'disableCancelFilterButton': false
              },
              'headerCellFilter': 'translate',
              'cellTemplate': "<div class=\"ui-grid-cell-contents\"><a ng-click=\"$event.stopPropagation();\" ui-sref=\"home.browse.facility.{{grid.appScope.getNextRouteSegment(row)}}({facilityName : '{{row.entity.name}}'})\">{{row.entity.fullName}}</a></div>" //jshint ignore: line
            },
            {
              'field': 'name',
              'displayName': 'BROWSE.COLUMN.FACILITY.NAME',
              'filter': {
                'condition': 16,
                'placeholder': 'Containing...',
                'flags': {
                  'caseSensitive': false
                },
                'type': 'input',
                'disableCancelFilterButton': false
              },
              'headerCellFilter': 'translate'
            }
          ]
        });

    });


});
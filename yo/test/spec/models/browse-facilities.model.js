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

        expect(gridOptions).toEqual(jasmine.objectContaining(
            {
                'enableFiltering': true
            }
        ));

        expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'field': 'name'
            }
        ));

        expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'displayName': 'BROWSE.COLUMN.FACILITY.NAME'
            }
        ));

        expect(gridOptions.columnDefs[0].translateDisplayName).toBeUndefined();

        /*expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'translateDisplayName': 'BROWSE.COLUMN.FACILITY.NAME'
            }
        ));*/

        expect(gridOptions.columnDefs[0].filter).toEqual(jasmine.objectContaining(
            {
                'condition': uiGridConstants.filter.CONTAINS
            }
        ));

        expect(gridOptions.columnDefs[0].filter).toEqual(jasmine.objectContaining(
            {
                'placeholder': 'Containing...'
            }
        ));

        expect(gridOptions.columnDefs[0].filter.flags).toEqual(jasmine.objectContaining(
            {
                 'caseSensitive': false
            }
        ));

        expect(gridOptions.columnDefs[0].filter).toEqual(jasmine.objectContaining(
            {
                'type': 'input'
            }
        ));

        expect(gridOptions.columnDefs[0].filter).toEqual(jasmine.objectContaining(
            {
                'disableCancelFilterButton': true
            }
        ));

        expect(gridOptions.columnDefs[0].link).toBeUndefined();

        expect(gridOptions.columnDefs[0].cellTemplate).toBeDefined();

        expect(gridOptions.columnDefs[1].link).toBeUndefined();

        expect(gridOptions.columnDefs[1].cellTemplate).toBeUndefined();


    });


});
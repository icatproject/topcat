'use strict';

//var mockConfigService;

describe('Service: BrowseEntitiesModel', function() {
    /*
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
    var BrowseEntitiesModel;
    beforeEach(inject(function(_BrowseEntitiesModel_) {
        BrowseEntitiesModel = _BrowseEntitiesModel_;
    }));


    it('should do something', function() {
        expect(!!BrowseEntitiesModel).toBe(true);
    });


    it('configToUIGridOptions for instrument', function() {
        var gridOptions = BrowseEntitiesModel.configToUIGridOptions(JSONFile.facilities.dls, 'instrument');

        expect(gridOptions).toEqual(jasmine.objectContaining(
            {
                'enableFiltering': true
            }
        ));

        expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'field': 'id'
            }
        ));

        expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'type': 'number'
            }
        ));

        expect(gridOptions.columnDefs[0]).toEqual(jasmine.objectContaining(
            {
                'displayName': 'Id'
            }
        ));

        expect(gridOptions.columnDefs[1]).toEqual(jasmine.objectContaining(
            {
                'field': 'name'
            }
        ));

        expect(gridOptions.columnDefs[1]).toEqual(jasmine.objectContaining(
            {
                'type': 'string'
            }
        ));


        expect(gridOptions.columnDefs[1]).toEqual(jasmine.objectContaining(
            {
                'displayName': 'BROWSE.COLUMN.INSTRUMENT.NAME'
            }
        ));

        expect(gridOptions.columnDefs[0].translateDisplayName).toBeUndefined();


        expect(gridOptions.columnDefs[1]).toEqual(jasmine.objectContaining(
            {
                'displayName': 'BROWSE.COLUMN.INSTRUMENT.NAME'
            }
        ));

        expect(gridOptions.columnDefs[1].filter).toEqual(jasmine.objectContaining(
            {
                'condition': uiGridConstants.filter.CONTAINS
            }
        ));

        expect(gridOptions.columnDefs[1].filter).toEqual(jasmine.objectContaining(
            {
                'placeholder': 'Containing...'
            }
        ));

        expect(gridOptions.columnDefs[1].filter.flags).toEqual(jasmine.objectContaining(
            {
                 'caseSensitive': false
            }
        ));

        expect(gridOptions.columnDefs[1].filter).toEqual(jasmine.objectContaining(
            {
                'type': 'input'
            }
        ));

        expect(gridOptions.columnDefs[1].filter).toEqual(jasmine.objectContaining(
            {
                'disableCancelFilterButton': false
            }
        ));

        //expect type to defaukt to string if no defined
        expect(gridOptions.columnDefs[2]).toEqual(jasmine.objectContaining(
            {
                'type': 'string'
            }
        ));

        expect(gridOptions.columnDefs[0].link).toBeUndefined();

        expect(gridOptions.columnDefs[1].link).toBeUndefined();

        expect(gridOptions.columnDefs[2].link).toBeUndefined();

    });*/


});
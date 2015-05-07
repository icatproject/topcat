'use strict';

/*jshint unused:false*/
function urldecode(str) {
  return decodeURIComponent((str + '')
    .replace(/%(?![\da-f]{2})/gi, function() {
      // PHP tolerates poorly formed escape sequences
      return '%25';
    })
    .replace(/\+/g, '%20'));
}

describe('Service: DataTableAODataBuilder', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var DataTableAODataBuilder;
    beforeEach(inject(function(_DataTableAODataBuilder_) {
        DataTableAODataBuilder = _DataTableAODataBuilder_;
    }));

    var SERVICE_URL = 'https://localhost:3001/icat/entityManager';



    it('should do something', function() {
        expect(!!DataTableAODataBuilder).toBe(true);
    });


    // buildUrlTest
    it('Test buildUrl with single param', function() {
        var params =DataTableAODataBuilder.buildUrl('http://example.com', {key: 'value'});

        expect(params).toBe('http://example.com?key=value');
    });

    it('buildUrl with no param', function() {
        var params =DataTableAODataBuilder.buildUrl('http://example.com');

        expect(params).toBe('http://example.com');
    });


    it('buildUrl with multiple param', function() {
        var params =DataTableAODataBuilder.buildUrl('http://example.com', {key1: 'value1', key2: 'value2'});

        expect(params).toBe('http://example.com?key1=value1&key2=value2');
    });

    it('buildUrl with no url', function() {
        var params =DataTableAODataBuilder.buildUrl('', {key1: 'value1', key2: 'value2'});

        expect(params).toBe('?key1=value1&key2=value2');
    });

    it('buildUrl with array of objects', function() {
        var params =DataTableAODataBuilder.buildUrl('http://example.com', ['value1', 'value2']);

        expect(params).toBe('http://example.com?0=value1&1=value2');
    });

    it('buildUrl with no url and param', function() {
        var params =DataTableAODataBuilder.buildUrl();

        expect(params).toBe(undefined);
    });

    it('buildUrl with url as null', function() {
        //expects an exception be thrown
        expect(function(){
            DataTableAODataBuilder.buildUrl(null, {key: 'value'});
        }).toThrow();
    });

    it('buildUrl with url as undefined', function() {
        //expects an exception be thrown
        expect(function(){
            DataTableAODataBuilder.buildUrl(undefined, {key: 'value'});
        }).toThrow();
    });

    //getInstruments tests
    it('getInstruments without ordering, non absUrl', function() {
        var params = DataTableAODataBuilder.getInstruments(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10
            }
        );

        console.log(params);

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });

    it('getInstruments without ordering, absUrl, with search', function() {
        var params = DataTableAODataBuilder.getInstruments(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: 'clf'
            },
            true
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\')')
        }));
    });


    it('getInstruments undefined session argument', function() {
        expect(function(){
            DataTableAODataBuilder.getInstruments(
                undefined,
                {
                    keyName: 'dls',
                    title: 'DIAMOND',
                    icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,
                },
                {
                    start: 0,
                    numRows: 10
                },
                true
            );
        })
        .toThrow();
    });


    it('getInstruments null session argument', function() {
        expect(function(){
            DataTableAODataBuilder.getInstruments(
                null,
                {
                    keyName: 'dls',
                    title: 'DIAMOND',
                    icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,
                },
                {
                    start: 0,
                    numRows: 10
                },
                true
            );
        })
        .toThrow(new Error('Invalid arguments. Session string is expected'));
    });

    it('getInstruments undefined facility argument', function() {
        expect(function(){
            DataTableAODataBuilder.getInstruments(
                '1234567890',
                undefined,
                {
                    start: 0,
                    numRows: 10
                },
                true
            );
        })
        .toThrow(new Error('Invalid arguments. facility object is expected'));
    });

    it('getInstruments undefined query parameter argument', function() {

        var params = DataTableAODataBuilder.getInstruments(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            undefined,
            false
        );


        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    it('getInstruments empty query parameter argument', function() {

        var params = DataTableAODataBuilder.getInstruments(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {},
            false
        );


        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();


        /*.toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890');*/
    });


    it('getInstruments empty facility and query parameter argument', function() {
        expect(function(){
            DataTableAODataBuilder.getInstruments(
                '1234567890',
                {
                    /*keyName: 'dls',
                    title: 'DIAMOND',
                    icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,*/
                },
                {},
                false
            );
        })
        .toThrow(new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl'));
    });

    it('getInstruments missing facility.icatUrl and query parameter argument', function() {
        expect(function(){
            DataTableAODataBuilder.getInstruments(
                '1234567890',
                {
                    keyName: 'dls',
                    title: 'DIAMOND',
                    //icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,
                },
                {},
                false
            );
        })
        .toThrow(new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl'));
    });



    it('getInstruments essential arguments only', function() {

        var params = DataTableAODataBuilder.getInstruments(
            '1234567890',
            {
                icatUrl: 'https://example.com',
                facilityId: 1,
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigationsByInstrumentId tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getInvestigationsByInstrumentId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                instrumentId: 8
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigationsByInstrumentId with search tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl with search', function() {
        var params = DataTableAODataBuilder.getInvestigationsByInstrumentId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                instrumentId: 8,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\')')
        }));
    });


    //getDatasetsByInvestigationId tests
    it('getDatasetsByInvestigationId without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatasetsByInvestigationId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                investigationId: 12345
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasetsByInvestigationId with search tests
    it('getDatasetsByInvestigationId without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatasetsByInvestigationId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                investigationId: 12345,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\')')
        }));
    });


    //getDatafilesByDatasetId tests
    it('getDatafilesByDatasetId without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatafilesByDatasetId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                datasetId: 98765
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafilesByDatasetId with search tests
    it('getDatafilesByDatasetId without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatafilesByDatasetId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                datasetId: 98765,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) AND (UPPER(df.name) LIKE \'%CLF%\')')
        }));
    });


    //getInvestigations tests
    it('getInvestigations without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getInvestigations(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigations with search tests
    it('getInvestigations without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getInvestigations(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\')')
        }));
    });


    //getDatasets tests
    it('getDatasets without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatasets(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasets with search tests
    it('getDatasets without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatasets(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(ds.name) LIKE \'%CLF%\')')
        }));
    });


    //getDatafiles tests
    it('getDatafiles without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatafiles(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafiles with search tests
    it('getDatafiles without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatafiles(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(df.name) LIKE \'%CLF%\')')
        }));
    });

    //getDatasetsByInstrumentId tests
    it('getDatasetsByInstrumentId without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatasetsByInstrumentId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                instrumentId: 12345
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasetsByInstrumentId with search tests
    it('getDatasetsByInstrumentId without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatasetsByInstrumentId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                instrumentId: 12345,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\')')
        }));
    });


    //getDatafilesByInvestigationId tests
    it('getDatafilesByInvestigationId without ordering, non absUrl', function() {
        var params =DataTableAODataBuilder.getDatafilesByInvestigationId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                investigationId: 12345
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafilesByInvestigationId with search tests
    it('getDatafilesByInvestigationId without ordering, non absUrl with search', function() {
        var params =DataTableAODataBuilder.getDatafilesByInvestigationId(
            '1234567890',
            {
                keyName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                investigationId: 12345,
                search: 'clf'
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            server: encodeURIComponent('https://example.com')
        }));

        expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(df.name) LIKE \'%CLF%\')')
        }));
    });



});



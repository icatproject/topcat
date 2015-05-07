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

describe('Service: DataTableQueryBuilder', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var DataTableQueryBuilder;
    beforeEach(inject(function(_DataTableQueryBuilder_) {
        DataTableQueryBuilder = _DataTableQueryBuilder_;
    }));

    var SERVICE_URL = 'https://localhost:3001/icat/entityManager';



    it('should do something', function() {
        expect(!!DataTableQueryBuilder).toBe(true);
    });


    // buildUrlTest
    it('Test buildUrl with single param', function() {
        var url = DataTableQueryBuilder.buildUrl('http://example.com', {key: 'value'});

        expect(url).toBe('http://example.com?key=value');
    });

    it('buildUrl with no param', function() {
        var url = DataTableQueryBuilder.buildUrl('http://example.com');

        expect(url).toBe('http://example.com');
    });


    it('buildUrl with multiple param', function() {
        var url = DataTableQueryBuilder.buildUrl('http://example.com', {key1: 'value1', key2: 'value2'});

        expect(url).toBe('http://example.com?key1=value1&key2=value2');
    });

    it('buildUrl with no url', function() {
        var url = DataTableQueryBuilder.buildUrl('', {key1: 'value1', key2: 'value2'});

        expect(url).toBe('?key1=value1&key2=value2');
    });

    it('buildUrl with array of objects', function() {
        var url = DataTableQueryBuilder.buildUrl('http://example.com', ['value1', 'value2']);

        expect(url).toBe('http://example.com?0=value1&1=value2');
    });

    it('buildUrl with no url and param', function() {
        var url = DataTableQueryBuilder.buildUrl();

        expect(url).toBe(undefined);
    });

    it('buildUrl with url as null', function() {
        //expects an exception be thrown
        expect(function(){
            DataTableQueryBuilder.buildUrl(null, {key: 'value'});
        }).toThrow();
    });

    it('buildUrl with url as undefined', function() {
        //expects an exception be thrown
        expect(function(){
            DataTableQueryBuilder.buildUrl(undefined, {key: 'value'});
        }).toThrow();
    });

    //getInstruments tests
    it('getInstruments without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getInstruments(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });

    it('getInstruments without ordering, absUrl', function() {
        var url = DataTableQueryBuilder.getInstruments(
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
            },
            true
        );

        expect(url).toBe(SERVICE_URL + '?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });

    it('getInstruments without ordering, absUrl, with search', function() {
        var url = DataTableQueryBuilder.getInstruments(
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

        expect(url).toBe(SERVICE_URL + '?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    it('getInstruments undefined session argument', function() {
        expect(function(){
            DataTableQueryBuilder.getInstruments(
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
            DataTableQueryBuilder.getInstruments(
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
            DataTableQueryBuilder.getInstruments(
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
        expect(
            DataTableQueryBuilder.getInstruments(
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
            )
        )
        .toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890');
    });


    it('getInstruments empty query parameter argument', function() {
        expect(
            DataTableQueryBuilder.getInstruments(
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
            )
        )
        .toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890');
    });


    it('getInstruments empty facility and query parameter argument', function() {
        expect(function(){
            DataTableQueryBuilder.getInstruments(
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
            DataTableQueryBuilder.getInstruments(
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
        expect(
            DataTableQueryBuilder.getInstruments(
                '1234567890',
                {
                    icatUrl: 'https://example.com',
                    facilityId: 1,
                }
            )
        )
        .toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&entity=Instrument' +
            '&query=' + encodeURIComponent('SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890');
    });


    //getInvestigationsByInstrumentId tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getInvestigationsByInstrumentId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)') +
            '&entity=Investigation' +
            '&query=' + encodeURIComponent('SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getInvestigationsByInstrumentId with search tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getInvestigationsByInstrumentId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)') +
            '&entity=Investigation' +
            '&query=' + encodeURIComponent('SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) AND (UPPER(inv.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatasetsByInvestigationId tests
    it('getDatasetsByInvestigationId without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatasetsByInvestigationId(
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

        //console.log(urldecode(url));

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatasetsByInvestigationId with search tests
    it('getDatasetsByInvestigationId without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatasetsByInvestigationId(
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

        //console.log(urldecode(url));

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafilesByDatasetId tests
    it('getDatafilesByDatasetId without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatafilesByDatasetId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafilesByDatasetId with search tests
    it('getDatafilesByDatasetId without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatafilesByDatasetId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getInvestigations tests
    it('getInvestigations without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getInvestigations(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Investigation' +
            '&query=' + encodeURIComponent('SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getInvestigations with search tests
    it('getInvestigations without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getInvestigations(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Investigation' +
            '&query=' + encodeURIComponent('SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(inv.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatasets tests
    it('getDatasets without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatasets(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatasets with search tests
    it('getDatasets without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatasets(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafiles tests
    it('getDatafiles without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatafiles(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafiles with search tests
    it('getDatafiles without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatafiles(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });

    //getDatasetsByInstrumentId tests
    it('getDatasetsByInstrumentId without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatasetsByInstrumentId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatasetsByInstrumentId with search tests
    it('getDatasetsByInstrumentId without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatasetsByInstrumentId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)') +
            '&entity=Dataset' +
            '&query=' + encodeURIComponent('SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafilesByInvestigationId tests
    it('getDatafilesByInvestigationId without ordering, non absUrl', function() {
        var url = DataTableQueryBuilder.getDatafilesByInvestigationId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });


    //getDatafilesByInvestigationId with search tests
    it('getDatafilesByInvestigationId without ordering, non absUrl with search', function() {
        var url = DataTableQueryBuilder.getDatafilesByInvestigationId(
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

        expect(url).toBe('?countQuery=' + encodeURIComponent('SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)') +
            '&entity=Datafile' +
            '&query=' + encodeURIComponent('SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10') +
            '&server=' + encodeURIComponent('https://example.com') +
            '&sessionId=1234567890'
            );
    });



});



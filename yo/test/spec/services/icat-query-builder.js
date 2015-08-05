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

describe('Service: ICATQueryBuilder', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var ICATQueryBuilder;
    beforeEach(inject(function(_ICATQueryBuilder_) {
        ICATQueryBuilder = _ICATQueryBuilder_;
    }));

    var SERVICE_URL = 'https://localhost:3001/icat/entityManager';



    it('should do something', function() {
        expect(!!ICATQueryBuilder).toBe(true);
    });

    //getInstruments tests
    it('getInstruments without ordering, non absUrl', function() {
        var params = ICATQueryBuilder.getInstruments(
            '1234567890',
            {
                facilityName: 'dls',
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

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });

    it('getInstruments without ordering, absUrl, with search', function() {
        var params = ICATQueryBuilder.getInstruments(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\')'
        }));*/
    });

    it('getInstruments with ordering, absUrl, with search', function() {
        var params = ICATQueryBuilder.getInstruments(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: [{search:'clf', field: 'name'}],
                sortField: 'name',
                order: 'desc'

            },
            true
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\') ORDER BY ins.name DESC LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1) AND (UPPER(ins.name) LIKE \'%CLF%\')'
        }));*/
    });


    it('getInstruments undefined session argument', function() {
        expect(function(){
            ICATQueryBuilder.getInstruments(
                undefined,
                {
                    facilityName: 'dls',
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
            ICATQueryBuilder.getInstruments(
                null,
                {
                    facilityName: 'dls',
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
            ICATQueryBuilder.getInstruments(
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

        var params = ICATQueryBuilder.getInstruments(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            undefined
        );


        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    it('getInstruments empty query parameter argument', function() {

        var params = ICATQueryBuilder.getInstruments(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {}
        );


        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params.filterCountQuery).toBeUndefined();*/


        /*.toBe('?countQuery=' + 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)' +
            '&entity=Instrument' +
            '&query=' + 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)' +
            '&server=' + 'https://example.com' +
            '&sessionId=1234567890');*/
    });


    it('getInstruments empty facility and query parameter argument', function() {
        expect(function(){
            ICATQueryBuilder.getInstruments(
                '1234567890',
                {
                    /*facilityName: 'dls',
                    title: 'DIAMOND',
                    icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,*/
                },
                {}
            );
        })
        .toThrow(new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl'));
    });

    it('getInstruments missing facility.icatUrl and query parameter argument', function() {
        expect(function(){
            ICATQueryBuilder.getInstruments(
                '1234567890',
                {
                    facilityName: 'dls',
                    title: 'DIAMOND',
                    //icatUrl: 'https://example.com',
                    connectProxyPath: 'dls/',
                    idsUrl: 'https://example.com',
                    facilityId: 1,
                },
                {}
            );
        })
        .toThrow(new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl'));
    });



    it('getInstruments essential arguments only', function() {

        var params = ICATQueryBuilder.getInstruments(
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
            query: 'SELECT ins FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Instrument'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigationsByInstrumentId tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getInvestigationsByInstrumentId(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigationsByInstrumentId with search tests
    it('getInvestigationsByInstrumentId without ordering, non absUrl with search', function() {
        var params = ICATQueryBuilder.getInvestigationsByInstrumentId(
            '1234567890',
            {
                facilityName: 'dls',
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
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT inv FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) AND (UPPER(inv.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 8) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\')'
        }));*/
    });


    //getDatasetsByInvestigationId tests
    it('getDatasetsByInvestigationId without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatasetsByInvestigationId(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasetsByInvestigationId with search tests
    it('getDatasetsByInvestigationId without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatasetsByInvestigationId(
            '1234567890',
            {
                facilityName: 'dls',
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
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\')'
        }));*/
    });


    //getDatafilesByDatasetId tests
    it('getDatafilesByDatasetId without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatafilesByDatasetId(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafilesByDatasetId with search tests
    it('getDatafilesByDatasetId without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatafilesByDatasetId(
            '1234567890',
            {
                facilityName: 'dls',
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
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND ds.id = 98765) AND (UPPER(df.name) LIKE \'%CLF%\')'
        }));*/
    });


    //getInvestigations tests
    it('getInvestigations without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getInvestigations(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getInvestigations with search tests
    it('getInvestigations without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getInvestigations(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(inv.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Investigation'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(inv) FROM Investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(inv.name) LIKE \'%CLF%\' OR UPPER(inv.title) LIKE \'%CLF%\' OR UPPER(inv.visitId) LIKE \'%CLF%\')'
        }));*/
    });


    //getInvestigations tests
    it('getInvestigations without ordering with 1 level include', function() {
        var params =ICATQueryBuilder.getInvestigations(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                includes: [
                    'investigation.investigationInstruments'
                ]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) INCLUDE inv.investigationInstruments LIMIT 0, 10'
        }));
    });

    it('getInvestigations without ordering with 2 level include', function() {
        var params =ICATQueryBuilder.getInvestigations(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                includes: [
                    'investigation.investigationInstruments.instrument'
                ]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) INCLUDE inv.investigationInstruments.instrument LIMIT 0, 10'
        }));
    });


    it('getInvestigations without ordering with 4 level include', function() {
        var params =ICATQueryBuilder.getInvestigations(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                includes: [
                    'investigation.investigationInstruments.instrument.instrumentScientists.user'
                ]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT inv FROM Investigation inv, inv.facility f WHERE (f.id = 1) INCLUDE inv.investigationInstruments.instrument.instrumentScientists.user LIMIT 0, 10'
        }));
    });


    //getDatasets tests
    it('getDatasets without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatasets(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasets with search tests
    it('getDatasets without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatasets(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(ds.name) LIKE \'%CLF%\')'
        }));*/
    });


    //getDatafiles tests
    it('getDatafiles without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatafiles(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafiles with search tests
    it('getDatafiles without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatafiles(
            '1234567890',
            {
                facilityName: 'dls',
                title: 'DIAMOND',
                icatUrl: 'https://example.com',
                connectProxyPath: 'dls/',
                idsUrl: 'https://example.com',
                facilityId: 1,
            },
            {
                start: 0,
                numRows: 10,
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1) AND (UPPER(df.name) LIKE \'%CLF%\')'
        }));*/
    });

    //getDatasetsByInstrumentId tests
    it('getDatasetsByInstrumentId without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatasetsByInstrumentId(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatasetsByInstrumentId with search tests
    it('getDatasetsByInstrumentId without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatasetsByInstrumentId(
            '1234567890',
            {
                facilityName: 'dls',
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
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT ds FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Dataset'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(ds) FROM Dataset ds, ds.investigation inv, inv.investigationInstruments invins, invins.instrument ins, inv.facility f WHERE (f.id = 1 AND ins.id = 12345) AND (UPPER(ds.name) LIKE \'%CLF%\')'
        }));*/
    });


    //getDatafilesByInvestigationId tests
    it('getDatafilesByInvestigationId without ordering, non absUrl', function() {
        var params =ICATQueryBuilder.getDatafilesByInvestigationId(
            '1234567890',
            {
                facilityName: 'dls',
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
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        //expect(params.filterCountQuery).toBeUndefined();
    });


    //getDatafilesByInvestigationId with search tests
    it('getDatafilesByInvestigationId without ordering, non absUrl with search', function() {
        var params =ICATQueryBuilder.getDatafilesByInvestigationId(
            '1234567890',
            {
                facilityName: 'dls',
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
                search: [{search:'clf', field: 'name'}]
            }
        );

        expect(params).toEqual(jasmine.objectContaining({
            sessionId: '1234567890'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            query: 'SELECT df FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(df.name) LIKE \'%CLF%\') LIMIT 0, 10'
        }));

        expect(params).toEqual(jasmine.objectContaining({
            countQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345)'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            entity: 'Datafile'
        }));*/

        expect(params).toEqual(jasmine.objectContaining({
            server: 'https://example.com'
        }));

        /*expect(params).toEqual(jasmine.objectContaining({
            filterCountQuery: 'SELECT COUNT(df) FROM Datafile df, df.dataset ds, ds.investigation inv, inv.facility f WHERE (f.id = 1 AND inv.id = 12345) AND (UPPER(df.name) LIKE \'%CLF%\')'
        }));*/
    });



});



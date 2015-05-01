'use strict';

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


});
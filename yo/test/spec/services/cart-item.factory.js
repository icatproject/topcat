'use strict';

describe('Service: CartItem', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var CartItem;
    beforeEach(inject(function(_CartItem_) {
        CartItem = _CartItem_;
    }));

    it('create cart item', function() {
        var facilityName = 'dls';
        var userName = 'simple/jane';
        var entityType = 'dataset';
        var id = 123456;
        var name = 'my test name';
        var size = null;
        var availability = 'ONLINE';
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var item = new CartItem(facilityName, userName, entityType, id, name, size, availability, parentEntities);

        expect(item).toEqual(jasmine.any(CartItem));

        expect(item.getFacilityName()).toEqual('dls');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('dataset');
        expect(item.getId()).toEqual(123456);
        expect(item.getName()).toEqual('my test name');
        expect(item.getSize()).toEqual(null);
        expect(item.getAvailability()).toEqual('ONLINE');
        expect(item.getParentEntities()).toEqual([
            {
              'entityType': 'investigation',
              'id': 7654321
            }
        ]);

        expect(item.toObject()).toEqual({
            'facilityName': 'dls',
            'userName': 'simple/jane',
            'entityType': 'dataset',
            'id': 123456,
            'name': 'my test name',
            'size': null,
            'availability': 'ONLINE',
            'parentEntities': [
                {
                  'entityType': 'investigation',
                  'id': 7654321
                }
            ]
        });
    });

    it('test cart item setters', function() {
        var facilityName = 'dls';
        var userName = 'simple/jane';
        var entityType = 'dataset';
        var id = 123456;
        var name = 'my test name';
        var size = null;
        var availability = 'ONLINE';
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var item = new CartItem(facilityName, userName, entityType, id, name, size, availability, parentEntities);

        expect(item.getFacilityName()).toEqual('dls');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('dataset');
        expect(item.getId()).toEqual(123456);
        expect(item.getName()).toEqual('my test name');
        expect(item.getSize()).toEqual(null);
        expect(item.getAvailability()).toEqual('ONLINE');
        expect(item.getParentEntities()).toEqual([
            {
              'entityType': 'investigation',
              'id': 7654321
            }
        ]);

        item.setFacilityName('isis');

        item.setEntityType('datafile');
        item.setId(888);
        item.setName('test datafile');
        item.setSize(777);
        item.setAvailability('ARCHIVE');
        item.setParentEntities([
            {
                'entityType': 'investigation',
                'id': 666
            },
            {
                'entityType': 'dataset',
                'id': 555
            }
        ]);

        expect(item.getFacilityName()).toEqual('isis');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('datafile');
        expect(item.getId()).toEqual(888);
        expect(item.getName()).toEqual('test datafile');
        expect(item.getSize()).toEqual(777);
        expect(item.getAvailability()).toEqual('ARCHIVE');
        expect(item.getParentEntities()).toEqual([
            {
                'entityType': 'investigation',
                'id': 666
            },
            {
                'entityType': 'dataset',
                'id': 555
            }
        ]);

    });

});



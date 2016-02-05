'use strict';

describe('Service: CartItem', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('test/mock/data/mock-config-multi.json'));
            $provide.constant('SMARTCLIENTPING', {ping: 'offline'});
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
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var item = new CartItem(facilityName, userName, entityType, id, name, parentEntities);

        expect(item).toEqual(jasmine.any(CartItem));

        expect(item.getFacilityName()).toEqual('dls');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('dataset');
        expect(item.getEntityId()).toEqual(123456);
        expect(item.getName()).toEqual('my test name');
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
            'entityId': 123456,
            'name': 'my test name',
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
        var parentEntities = [{
            entityType: 'investigation',
            id: 7654321
        }];

        var item = new CartItem(facilityName, userName, entityType, id, name, parentEntities);

        expect(item.getFacilityName()).toEqual('dls');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('dataset');
        expect(item.getEntityId()).toEqual(123456);
        expect(item.getName()).toEqual('my test name');
        expect(item.getParentEntities()).toEqual([
            {
              'entityType': 'investigation',
              'id': 7654321
            }
        ]);

        item.setFacilityName('isis');

        item.setEntityType('datafile');
        item.setEntityId(888);
        item.setName('test datafile');
        item.setParentEntities([
            {
                'entityType': 'investigation',
                'entityId': 666
            },
            {
                'entityType': 'dataset',
                'entityId': 555
            }
        ]);

        expect(item.getFacilityName()).toEqual('isis');
        expect(item.getUserName()).toEqual('simple/jane');
        expect(item.getEntityType()).toEqual('datafile');
        expect(item.getEntityId()).toEqual(888);
        expect(item.getName()).toEqual('test datafile');
        expect(item.getParentEntities()).toEqual([
            {
                'entityType': 'investigation',
                'entityId': 666
            },
            {
                'entityType': 'dataset',
                'entityId': 555
            }
        ]);

    });

});



'use strict';

describe('tc icat entity service', function () {


    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('app/config/topcat_dev.json'));
        });
    });

    beforeEach(module('topcat'));

    describe('find', function(){
        var entity;

        beforeEach(inject(function(tcIcatEntity){
            var mockFacility = {
                icat: function(){ return {}; },
                config: function(){ return {name: ""}; }
            };
            var mockAttributes = {
                entityType: "investigation"
            };

            entity = {
                find: tcIcatEntity.create(mockAttributes, mockFacility).find
            };
        }));

        describe('for investigationUserPivot', function(){

            beforeEach(function(){


                entity = _.merge(entity, {
                    entityType: "investigationUser",
                    role: "apple",
                    user: {
                        entityType: "user",
                        fullName: "orange"
                    }
                });

            });

            it("should be able to find the investigation users role", function(){
                expect(entity.find('role')[0]).toEqual('apple');
            });

            
            it("should be able to find the users fullname", function(){
                expect(entity.find('investigationUser.fullName')[0]).toEqual('orange');
            });
            

        });

    });

});
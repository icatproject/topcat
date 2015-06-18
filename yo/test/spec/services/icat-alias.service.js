'use strict';

describe('Service: ICATAlias', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', {});
        });
    });

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var ICATAlias;
    beforeEach(inject(function(_ICATAlias_) {
        ICATAlias = _ICATAlias_;
    }));

    it('get getAlias', function() {
        var alias = ICATAlias.getAlias('investigation');

        expect(alias).toEqual('inv');
    });

    it('get getAlias', function() {
        var alias = ICATAlias.getAlias('instrument');

        expect(alias).toEqual('ins');
    });

});



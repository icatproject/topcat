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

    it('should do something', function() {
        expect(!!DataTableQueryBuilder).toBe(true);
    });

});
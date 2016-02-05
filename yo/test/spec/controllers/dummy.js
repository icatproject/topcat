'use strict';

describe('DummyTest', function () {

    beforeEach(function () {
        module(function ($provide) {
            $provide.constant('APP_CONFIG', { someUrl: '/data/config.json' });
        });
    });

    it('Dummy test to stop karma complaining', function () {
        expect(true).toBe(true);
    });
});
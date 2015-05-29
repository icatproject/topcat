'use strict';

describe('service:test', function () {

    beforeEach(function () {
        module(function ($provide) {
            //var validRespond = readJSON('/data/config.json');
            //var data = $httpBackend.whenGET(/.*/).respond(validRespond);

            console.log('hellofdsafdsfsdfsdfsdafdsafdsfsda');


            $provide.constant('APP_CONFIG', { someUrl: '/data/config.json' });
        });
    });

    it('Dummy test to stop karma complaining', function () {
        expect(true).toBe(true);
    });
});
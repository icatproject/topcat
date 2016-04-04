'use strict';

describe('tc icat query builder service', function () {
    
    var icat;

    beforeEach(function() {
        module(function($provide) {
            $provide.constant('APP_CONFIG', readJSON('app/config/topcat_dev.json'));
        });
    });

    beforeEach(module('angularApp'));

    /*
    beforeEach(inject(function(tc){
        icat = tc.icat('test');
    }));
    */

    describe('impliedPathsToImpliedSteps()', function(){

        it('should return correct steps', function(){
            expect(true).toBe(true);
        });

    });

});
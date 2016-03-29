
//this is a temporary workaround until I can get Karma working.

(function() {
  'use strict';

  var app = angular.module('angularApp');

  app.service('tcIcatQueryBuilderTests', function(tc){
    var that = this;

    function fail(){
      throw "Test failed";
    }

    this.testImpliedPathsToImpliedSteps = function(){
      var queryBuilder = tc.icat('test').queryBuilder('investigation');

      var expectations = [
        {
          'in': {
            'investigationUser': ['investigationUsers', 'user']
          },
          'out': {
            'investigation.investigationUsers': 'investigationUserPivot',
            'investigationUserPivot.user': 'investigationUser'
          }
        }
      ];

      _.each(expectations, function(expectation){
        var out = queryBuilder.impliedPathsToImpliedSteps(expectation.in);
        if(!_.isEqual(out, expectation.out)){
          console.error('expected: ', expectation.out, ' got:', out);
          fail();
        }
      });

    };

    this.run = function(){
      _.each(this, function(fn, name){
        if(name.match(/^test/)){
          fn.call(that);
          console.log('pass: ' + name)
        }
      });
    };

  });

})();
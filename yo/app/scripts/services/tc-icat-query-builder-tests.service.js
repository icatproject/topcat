
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
    
      var entityTypeExpectations = {
        'investigation': [
          {
            'in': {
              'investigationParameter': ['parameters']
            },
            'out': {
              'investigation.parameters': 'investigationParameter'
            }
          },
          {
            'in': {
              'investigationParameterType': ['parameters', 'type']
            },
            'out': {
              'investigation.parameters': 'investigationParameter',
              'investigationParameter.type': 'investigationParameterType'
            }
          },
          {
            'in': {
              'investigationUser': ['investigationUsers', 'user']
            },
            'out': {
              'investigation.investigationUsers': 'investigationUserPivot',
              'investigationUserPivot.user': 'investigationUser'
            }
          },
          {
            'in': {
              'datafileParameterType': ['datasets', 'datafiles', 'parameters', 'type']
            },
            'out': {
              'investigation.datasets': 'dataset',
              'dataset.datafiles': 'datafile',
              'datafile.parameters': 'datafileParameter',
              'datafileParameter.type': 'datafileParameterType'
            }
          }
        ],
        'dataset': [
          {
            'in': {
              'investigation': ['investigation']
            },
            'out': {
              'dataset.investigation': 'investigation'
            }
          },
          {
            'in': {
              'datasetParameter': ['parameters']
            },
            'out': {
              'dataset.parameters': 'datasetParameter'
            }
          },
          {
            'in': {
              'datasetParameterType': ['parameters', 'type']
            },
            'out': {
              'dataset.parameters': 'datasetParameter',
              'datasetParameter.type': 'datasetParameterType'
            }
          },
        ],
        'datafile': [
          {
            'in': {
              'dataset': ['dataset']
            },
            'out': {
              'datafile.dataset': 'dataset'
            }
          },
          {
            'in': {
              'datafileParameter': ['parameters']
            },
            'out': {
              'datafile.parameters': 'datafileParameter'
            }
          },
          {
            'in': {
              'datafileParameterType': ['parameters', 'type']
            },
            'out': {
              'datafile.parameters': 'datafileParameter',
              'datafileParameter.type': 'datafileParameterType'
            }
          }
        ]
      };


      _.each(entityTypeExpectations, function(expectations, entityType){
        var queryBuilder = tc.icat('test').queryBuilder(entityType);
        _.each(expectations, function(expectation){
          var out = queryBuilder.impliedPathsToImpliedSteps(expectation.in);
          if(!_.isEqual(out, expectation.out)){
            console.error('expected: ', expectation.out, ' got:', out);
            fail();
          }
        });
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
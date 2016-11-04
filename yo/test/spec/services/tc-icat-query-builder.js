'use strict';

describe('tc icat query builder service', function () {

    var icat;

    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('app/config/topcat_dev.json'));
        });
    });

    beforeEach(module('topcat'));

    beforeEach(inject(function(tc){
        icat = tc.icat('TEST');
    }));

    describe('impliedPathsToImpliedSteps()', function(){

        it('should return correct steps', function(){

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
                ],
                'datafileParameter': [
                    {
                        'in': {
                            'investigation': ['datafile', 'dataset', 'investigation']
                        },
                        'out': {
                            'datafileParameter.datafile': 'datafile',
                            'datafile.dataset': 'dataset',
                            'dataset.investigation': 'investigation'
                        }
                    }
                ]
            };

            _.each(entityTypeExpectations, function(expectations, entityType){
                var queryBuilder = icat.queryBuilder(entityType);
                _.each(expectations, function(expectation){
                var out = queryBuilder.impliedPathsToImpliedSteps(expectation.in);
                    expect(out).toEqual(expectation.out);
                });
            });

        });

    });

    describe('build()', function() {
        describe('with left join', function() {
            it('should ensure a left join', function() {
                console.log(icat.queryBuilder('dataset').where('datasetParameterType.name = \'blah\'').leftJoin('datasetParameter').build());
                var queryBuilder = icat.queryBuilder('dataset');
                queryBuilder.leftJoin('datasetParameter');
                queryBuilder.leftJoin('datasetParameterType');
                queryBuilder.where('datasetParameterType.name = \'blah\'');
                var query = queryBuilder.build();
                expect(query).toContain('from Dataset dataset'
                                        + ' LEFT OUTER JOIN dataset.parameters datasetParameter'
                                        + ' LEFT OUTER JOIN datasetParameter.type datasetParameterType'
                                        + ' where');
            });
        });
        describe('without left join', function() {
            it('should not have a left join', function() {
                var queryBuilder = icat.queryBuilder('dataset');
                queryBuilder.where('datasetParameterType.name = \'blah\'');
                var query = queryBuilder.build();
                expect(query).toContain('from Dataset dataset'
                                        + ' , dataset.parameters as datasetParameter'
                                        + ' , datasetParameter.type as datasetParameterType'
                                        + ' where');
            });
        });
    });

});
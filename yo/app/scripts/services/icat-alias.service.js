(function() {
    'use strict';

    angular
        .module('angularApp')
        .service('ICATAlias', ICATAlias);

    ICATAlias.$inject = [];

    function ICATAlias() {
        this.relationshipMap = {
            'application' : {
                'alias' : 'app',
                'relationship' : {
                    'jobs' : 'job',
                    'facility' : 'f'
                }
            },
            'dataCollection' : {
                'alias' : 'dc',
                'relationship' : {
                    'jobsAsOutput' : 'job',
                    'dataCollectionDatasets' : 'dcds',
                    'parameters' : 'dcp',
                    'jobsAsInput' : 'job',
                    'dataCollectionDatafiles' : 'dcdf'
                }
            },
            'dataCollectionDatafile' : {
                'alias' : 'dcdf',
                'relationship' : {
                    'dataCollection' : 'dc',
                    'datafile' : 'df'
                }
            },
            'dataCollectionDataset' : {
                'alias' : 'dcds',
                'relationship' : {
                    'dataset' : 'ds',
                    'dataCollection' : 'dc'
                }
            },
            'dataCollectionParameter' : {
                'alias' : 'dcp',
                'relationship' : {
                    'type' : 'pt',
                    'dataCollection' : 'dc'
                }
            },
            'datafile' : {
                'alias' : 'df',
                'relationship' : {
                    'datafileFormat' : 'dff',
                    'sourceDatafiles' : 'rdf',
                    'parameters' : 'dfp',
                    'destDatafiles' : 'rdf',
                    'dataCollectionDatafiles' : 'dcdf',
                    'dataset' : 'ds'
                }
            },
            'datafileFormat' : {
                'alias' : 'dff',
                'relationship' : {
                    'facility' : 'f',
                    'datafiles' : 'df'
                }
            },
            'datafileParameter' : {
                'alias' : 'dfp',
                'relationship' : {
                    'type' : 'pt',
                    'datafile' : 'df'
                }
            },
            'dataset' : {
                'alias' : 'ds',
                'relationship' : {
                    'type' : 'dst',
                    'sample' : 's',
                    'datafiles' : 'df',
                    'dataCollectionDatasets' : 'dcds',
                    'investigation' : 'inv',
                    'parameters' : 'dsp'
                }
            },
            'datasetParameter' : {
                'alias' : 'dsp',
                'relationship' : {
                    'dataset' : 'ds',
                    'type' : 'pt'
                }
            },
            'datasetType' : {
                'alias' : 'dst',
                'relationship' : {
                    'datasets' : 'ds',
                    'facility' : 'f'
                }
            },
            'facility' : {
                'alias' : 'f',
                'relationship' : {
                    'investigations' : 'inv',
                    'parameterTypes' : 'pt',
                    'facilityCycles' : 'fc',
                    'investigationTypes' : 'invt',
                    'instruments' : 'ins',
                    'datafileFormats' : 'dff',
                    'applications' : 'app',
                    'sampleTypes' : 'st',
                    'datasetTypes' : 'dst'
                }
            },
            'facilityCycle' : {
                'alias' : 'fc',
                'relationship' : {
                    'facility' : 'f'
                }
            },
            'grouping' : {
                'alias' : 'g',
                'relationship' : {
                    'userGroups' : 'ug',
                    'investigationGroups' : 'invg',
                    'rules' : 'r'
                }
            },
            'instrument' : {
                'alias' : 'ins',
                'relationship' : {
                    'investigationInstruments' : 'invins',
                    'instrumentScientists' : 'ins',
                    'facility' : 'f'
                }
            },
            'instrumentScientist' : {
                'alias' : 'inss',
                'relationship' : {
                    'user' : 'u',
                    'instrument' : 'ins'
                }
            },
            'investigation' : {
                'alias' : 'inv',
                'relationship' : {
                    'type' : 'invt',
                    'investigationGroups' : 'invg',
                    'publications' : 'pub',
                    'parameters' : 'invp',
                    'datasets' : 'fs',
                    'studyInvestigations' : 'stuinv',
                    'keywords' : 'k',
                    'shifts' : 'sh',
                    'investigationUsers' : 'invu',
                    'samples' : 's',
                    'investigationInstruments' : 'invins',
                    'facility' : 'f'
                }
            },
            'investigationGroup' : {
                'alias' : 'invg',
                'relationship' : {
                    'investigation' : 'inv',
                    'grouping' : 'g'
                }
            },
            'investigationInstrument' : {
                'alias' : 'invins',
                'relationship' : {
                    'investigation' : 'inv',
                    'instrument' : 'ins'
                }
            },
            'investigationParameter' : {
                'alias' : 'invp',
                'relationship' : {
                    'investigation' : 'inv',
                    'type' : 'pt'
                }
            },
            'investigationType' : {
                'alias' : 'invt',
                'relationship' : {
                    'facility' : 'f',
                    'investigations' : 'inv'
                }
            },
            'InvestigationUser' : {
                'alias' : 'invu',
                'relationship' : {
                    'user' : 'u',
                    'investigation' : 'inv'
                }
            },
            'job' : {
                'alias' : 'job',
                'relationship' : {
                    'outputDataCollection' : 'dc',
                    'inputDataCollection' : 'dc',
                    'application' : 'app'
                }
            },
            'keyword' : {
                'alias' : 'k',
                'relationship' : {
                    'investigation' : 'inv'
                }
            },
            'log' : {
                'alias' : 'l'
            },
            'parameterType' : {
                'alias' : 'pt',
                'relationship' : {
                    'datafileParameters' : 'dfp',
                    'sampleParameters' : 'sp',
                    'permissibleStringValues' : 'psv',
                    'investigationParameters' : 'invp',
                    'facility' : 'f',
                    'dataCollectionParameters' : 'dcp',
                    'datasetParameters' : 'dsp'
                }
            },
            'permissibleStringValue' : {
                'alias' : 'psv',
                'relationship' : {
                    'type' : 'pt'
                }
            },
            'publicStep' : {
                'alias' : 'ps'
            },
            'publication' : {
                'alias' : 'pub',
                'relationship' : {
                    'investigation' : 'inv'
                }
            },
            'relatedDatafile' : {
                'alias' : 'rdf',
                'relationship' : {
                    'sourceDatafile' : 'df',
                    'destDatafile' : 'df'
                }
            },
            'rule' : {
                'alias' : 'r',
                'relationship' : {
                    'grouping' : 'g'
                }
            },
            'sample' : {
                'alias' : 's',
                'relationship' : {
                    'datasets' : 'ds',
                    'type' : 'st',
                    'investigation' : 'inv',
                    'parameters' : 'sp'
                }
            },
            'sampleParameter' : {
                'alias' : 'sp',
                'relationship' : {
                    'sample' : 's',
                    'type' : 'pt'
                }
            },
            'SampleType' : {
                'alias' : 'st',
                'relationship' : {
                    'facility' : 'f',
                    'samples' : 's'
                }
            },
            'shift' : {
                'alias' : 'sh',
                'relationship' : {
                    'investigation' : 'inv'
                }
            },
            'study' : {
                'alias' : 'stu',
                'relationship' : {
                    'user' : 'u',
                    'studyInvestigations' : 'stuInv'
                }
            },
            'studyInvestigation' : {
                'alias' : 'stuinv',
                'relationship' : {
                    'investigation' : 'inv',
                    'study' : 'stu'
                }
            },
            'user' : {
                'alias' : 'u',
                'relationship' : {
                    'investigationUsers' : 'invu',
                    'userGroups' : 'ug',
                    'studies' : 'stu',
                    'instrumentScientists' : 'inss',
                }
            },
            'userGroup' : {
                'alias' : 'ug',
                'relationship' : {
                    'grouping' : 'g',
                    'user' : 'u'
                }
            }
        };

        this.getAlias = function(entityName) {
            return this.relationshipMap[entityName].alias;
        };
    }
})();
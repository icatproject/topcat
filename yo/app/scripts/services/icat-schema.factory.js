
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.factory('icatSchema', function(){
    	var out = {
			'entityTypes': {
				'application': {
					'fields':{
					'modId': 'string',
					'name': 'string',
					'createId': 'string',
					'version': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'jobs': {
							'entityType': 'job',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						}
					}
				},
				'dataCollection': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'dataCollectionDatasets': {
							'entityType': 'dataCollectionDataset',
							'cardinality': '0,*'
						},
						'jobsAsInput': {
							'entityType': 'job',
							'cardinality': '0,*'
						},
						'jobsAsOutput': {
							'entityType': 'job',
							'cardinality': '0,*'
						},
						'dataCollectionDatafiles': {
							'entityType': 'dataCollectionDatafile',
							'cardinality': '0,*'
						},
						'parameters': {
							'entityType': 'dataCollectionParameter',
							'cardinality': '0,*'
						}
					}
				},
				'dataCollectionDatafile': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'dataCollection': {
							'entityType': 'dataCollection',
							'cardinality': '1,1'
						},
						'datafile': {
							'entityType': 'datafile',
							'cardinality': '1,1'
						}
					}
				},
				'dataCollectionDataset': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'dataCollection': {
							'entityType': 'dataCollection',
							'cardinality': '1,1'
						},
						'dataset': {
							'entityType': 'dataset',
							'cardinality': '1,1'
						}
					}
				},
				'dataCollectionParameter': {
					'fields':{
					'stringValue': 'string',
					'rangeBottom': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'dateTimeValue': 'date',
					'modId': 'string',
					'error': 'number',
					'numericValue': 'number',
					'rangeTop': 'number'
					},
					'relationships':{
						'dataCollection': {
							'entityType': 'dataCollection',
							'cardinality': '1,1'
						},
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1'
						}
					}
				},
				'datafile': {
					'fields':{
					'fileSize': 'number',
					'location': 'string',
					'checksum': 'string',
					'name': 'string',
					'createId': 'string',
					'doi': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'datafileModTime': 'date',
					'modId': 'string',
					'datafileCreateTime': 'date',
					'description': 'string'
					},
					'relationships':{
						'dataCollectionDatafiles': {
							'entityType': 'dataCollectionDatafile',
							'cardinality': '0,*'
						},
						'datafileFormat': {
							'entityType': 'datafileFormat',
							'cardinality': '0,1'
						},
						'sourceDatafiles': {
							'entityType': 'relatedDatafile',
							'cardinality': '0,*'
						},
						'parameters': {
							'entityType': 'datafileParameter',
							'cardinality': '0,*',
							'variableName': 'datafileParameter'
						},
						'dataset': {
							'entityType': 'dataset',
							'cardinality': '1,1',
							'variableName': 'dataset'
						},
						'destDatafiles': {
							'entityType': 'relatedDatafile',
							'cardinality': '0,*'
						}
					}
				},
				'datafileFormat': {
					'fields':{
					'modId': 'string',
					'description': 'string',
					'name': 'string',
					'version': 'string',
					'createId': 'string',
					'type': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						},
						'datafiles': {
							'entityType': 'datafile',
							'cardinality': '0,*'
						}
					}
				},
				'datafileParameter': {
					'fields':{
					'stringValue': 'string',
					'rangeBottom': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'dateTimeValue': 'date',
					'modId': 'string',
					'error': 'number',
					'numericValue': 'number',
					'rangeTop': 'number'
					},
					'relationships':{
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1',
							'variableName': 'datafileParameterType'
						},
						'datafile': {
							'entityType': 'datafile',
							'cardinality': '1,1',
							'variableName': 'datafile'
						}
					}
				},
				'dataset': {
					'fields':{
					'name': 'string',
					'location': 'string',
					'complete': 'boolean',
					'startDate': 'date',
					'doi': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'description': 'string',
					'modId': 'string',
					'endDate': 'date'
					},
					'relationships':{
						'sample': {
							'entityType': 'sample',
							'cardinality': '0,1',
							'variableName': 'datasetSample'
						},
						'dataCollectionDatasets': {
							'entityType': 'dataCollectionDataset',
							'cardinality': '0,*'
						},
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1',
							'variableName': 'investigation'
						},
						'datafiles': {
							'entityType': 'datafile',
							'cardinality': '0,*',
							'variableName': 'datafile'
						},
						'type': {
							'entityType': 'datasetType',
							'cardinality': '1,1',
							'variableName': 'datasetType'
						},
						'parameters': {
							'entityType': 'datasetParameter',
							'cardinality': '0,*',
							'variableName': 'datasetParameter'
						}
					}
				},
				'datasetParameter': {
					'fields':{
					'stringValue': 'string',
					'rangeBottom': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'dateTimeValue': 'date',
					'modId': 'string',
					'error': 'number',
					'numericValue': 'number',
					'rangeTop': 'number'
					},
					'relationships':{
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1',
							'variableName': 'datasetParameterType'
						},
						'dataset': {
							'entityType': 'dataset',
							'cardinality': '1,1'
						}
					}
				},
				'datasetType': {
					'fields':{
					'description': 'string',
					'name': 'string',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'datasets': {
							'entityType': 'dataset',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						}
					}
				},
				'facility': {
					'fields':{
					'daysUntilRelease': 'string',
					'url': 'string',
					'createId': 'string',
					'description': 'string',
					'fullName': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'modId': 'string',
					'name': 'string'
					},
					'relationships':{
						'instruments': {
							'entityType': 'instrument',
							'cardinality': '0,*',
							'variableName': 'instrument'
						},
						'facilityCycles': {
							'entityType': 'facilityCycle',
							'cardinality': '0,*',
							'variableName': 'facilityCycle'
						},
						'investigationTypes': {
							'entityType': 'investigationType',
							'cardinality': '0,*'
						},
						'datafileFormats': {
							'entityType': 'datafileFormat',
							'cardinality': '0,*'
						},
						'investigations': {
							'entityType': 'investigation',
							'cardinality': '0,*',
							'variableName': 'investigation'
						},
						'sampleTypes': {
							'entityType': 'sampleType',
							'cardinality': '0,*'
						},
						'parameterTypes': {
							'entityType': 'parameterType',
							'cardinality': '0,*'
						},
						'datasetTypes': {
							'entityType': 'datasetType',
							'cardinality': '0,*'
						},
						'applications': {
							'entityType': 'application',
							'cardinality': '0,*'
						}
					}
				},
				'facilityCycle': {
					'fields':{
					'name': 'string',
					'description': 'string',
					'modId': 'string',
					'startDate': 'date',
					'createId': 'string',
					'endDate': 'date',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1',
							'variableName': 'facility'
						}
					}
				},
				'grouping': {
					'fields':{
					'modId': 'string',
					'name': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'investigationGroups': {
							'entityType': 'investigationGroup',
							'cardinality': '0,*'
						},
						'rules': {
							'entityType': 'rule',
							'cardinality': '0,*'
						},
						'userGroups': {
							'entityType': 'userGroup',
							'cardinality': '0,*'
						}
					}
				},
				'instrument': {
					'fields':{
					'name': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'url': 'string',
					'type': 'string',
					'modId': 'string',
					'description': 'string',
					'fullName': 'string'
					},
					'relationships':{
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1',
							'variableName': 'facility'
						},
						'instrumentScientists': {
							'entityType': 'instrumentScientist',
							'cardinality': '0,*'
						},
						'investigationInstruments': {
							'entityType': 'investigationInstrument',
							'cardinality': '0,*'
						}
					}
				},
				'instrumentScientist': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'instrument': {
							'entityType': 'instrument',
							'cardinality': '1,1'
						},
						'user': {
							'entityType': 'user',
							'cardinality': '1,1'
						}
					}
				},
				'investigation': {
					'fields':{
					'name': 'string',
					'startDate': 'date',
					'doi': 'string',
					'summary': 'string',
					'modId': 'string',
					'releaseDate': 'date',
					'visitId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'endDate': 'date',
					'title': 'string'
					},
					'relationships':{
						'keywords': {
							'entityType': 'keyword',
							'cardinality': '0,*'
						},
						'shifts': {
							'entityType': 'shift',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1',
							'variableName': 'facility'
						},
						'datasets': {
							'entityType': 'dataset',
							'cardinality': '0,*',
							'variableName': 'dataset'
						},
						'investigationGroups': {
							'entityType': 'investigationGroupPivot',
							'cardinality': '0,*'
						},
						'publications': {
							'entityType': 'publication',
							'cardinality': '0,*',
							'variableName': 'publication'
						},
						'type': {
							'entityType': 'investigationType',
							'cardinality': '1,1'
						},
						'investigationInstruments': {
							'entityType': 'investigationInstrument',
							'cardinality': '0,*',
							'variableName': 'investigationInstrumentPivot'
						},
						'parameters': {
							'entityType': 'investigationParameter',
							'cardinality': '0,*',
							'variableName': 'investigationParameter'
						},
						'studyInvestigations': {
							'entityType': 'studyInvestigation',
							'cardinality': '0,*'
						},
						'samples': {
							'entityType': 'sample',
							'cardinality': '0,*',
							'variableName': 'investigationSample'
						},
						'investigationUsers': {
							'entityType': 'investigationUser',
							'cardinality': '0,*',
							'variableName': 'investigationUserPivot'
						}
					}
				},
				'investigationGroup': {
					'fields':{
					'role': 'string',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						},
						'grouping': {
							'entityType': 'grouping',
							'cardinality': '1,1',
							'variableName': 'investigationGroup'
						}
					}
				},
				'investigationInstrument': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						},
						'instrument': {
							'entityType': 'instrument',
							'cardinality': '1,1',
							'variableName': 'investigationInstrument'
						}
					}
				},
				'investigationParameter': {
					'fields':{
					'stringValue': 'string',
					'rangeBottom': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'dateTimeValue': 'date',
					'modId': 'string',
					'error': 'number',
					'numericValue': 'number',
					'rangeTop': 'number'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						},
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1',
							'variableName': 'investigationParameterType'
						}
					}
				},
				'investigationType': {
					'fields':{
					'modId': 'string',
					'description': 'string',
					'name': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'investigations': {
							'entityType': 'investigation',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						}
					}
				},
				'investigationUser': {
					'fields':{
					'modId': 'string',
					'role': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'user': {
							'entityType': 'user',
							'cardinality': '1,1',
							'variableName': 'investigationUser'
						},
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'job': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'arguments': 'string'
					},
					'relationships':{
						'inputDataCollection': {
							'entityType': 'dataCollection',
							'cardinality': '0,1'
						},
						'outputDataCollection': {
							'entityType': 'dataCollection',
							'cardinality': '0,1'
						},
						'application': {
							'entityType': 'application',
							'cardinality': '1,1'
						}
					}
				},
				'keyword': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'name': 'string'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'log': {
					'fields':{
					'operation': 'string',
					'modId': 'string',
					'entityId': 'number',
					'entityType': 'string',
					'query': 'string',
					'duration': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
					}
				},
				'parameterType': {
					'fields':{
					'units': 'string',
					'description': 'string',
					'modId': 'string',
					'applicableToSample': 'boolean',
					'valueType': 'string',
					'verified': 'boolean',
					'applicableToDataset': 'boolean',
					'applicableToDataCollection': 'boolean',
					'minimumNumericValue': 'number',
					'enforced': 'boolean',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'applicableToInvestigation': 'boolean',
					'name': 'string',
					'unitsFullName': 'string',
					'applicableToDatafile': 'boolean',
					'maximumNumericValue': 'number'
					},
					'relationships':{
						'investigationParameters': {
							'entityType': 'investigationParameter',
							'cardinality': '0,*'
						},
						'permissibleStringValues': {
							'entityType': 'permissibleStringValue',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						},
						'dataCollectionParameters': {
							'entityType': 'dataCollectionParameter',
							'cardinality': '0,*'
						},
						'sampleParameters': {
							'entityType': 'sampleParameter',
							'cardinality': '0,*'
						},
						'datasetParameters': {
							'entityType': 'datasetParameter',
							'cardinality': '0,*'
						},
						'datafileParameters': {
							'entityType': 'datafileParameter',
							'cardinality': '0,*'
						}
					}
				},
				'permissibleStringValue': {
					'fields':{
					'modId': 'string',
					'value': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1'
						}
					}
				},
				'proposal': {
					'fields':{
					'name': 'string'
					},
					'relationships':{
						'investigations': {
							'entityType': 'investigation',
							'cardinality': '1,*',
							'variableName': 'investigation'
						}
					}
				},
				'publicStep': {
					'fields':{
					'field': 'string',
					'origin': 'string',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
					}
				},
				'publication': {
					'fields':{
					'fullReference': 'string',
					'modId': 'string',
					'repository': 'string',
					'url': 'string',
					'doi': 'string',
					'repositoryId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'relatedDatafile': {
					'fields':{
					'modId': 'string',
					'relation': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'destDatafile': {
							'entityType': 'datafile',
							'cardinality': '1,1'
						},
						'sourceDatafile': {
							'entityType': 'datafile',
							'cardinality': '1,1'
						}
					}
				},
				'rule': {
					'fields':{
					'crudFlags': 'string',
					'modId': 'string',
					'what': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'grouping': {
							'entityType': 'grouping',
							'cardinality': '0,1'
						}
					}
				},
				'sample': {
					'fields':{
					'name': 'string',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'parameters': {
							'entityType': 'sampleParameter',
							'cardinality': '0,*'
						},
						'datasets': {
							'entityType': 'dataset',
							'cardinality': '0,*'
						},
						'type': {
							'entityType': 'sampleType',
							'cardinality': '0,1'
						},
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'sampleParameter': {
					'fields':{
					'stringValue': 'string',
					'rangeBottom': 'number',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'dateTimeValue': 'date',
					'modId': 'string',
					'error': 'number',
					'numericValue': 'number',
					'rangeTop': 'number'
					},
					'relationships':{
						'sample': {
							'entityType': 'sample',
							'cardinality': '1,1'
						},
						'type': {
							'entityType': 'parameterType',
							'cardinality': '1,1'
						}
					}
				},
				'sampleType': {
					'fields':{
					'modId': 'string',
					'molecularFormula': 'string',
					'name': 'string',
					'safetyInformation': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'samples': {
							'entityType': 'sample',
							'cardinality': '0,*'
						},
						'facility': {
							'entityType': 'facility',
							'cardinality': '1,1'
						}
					}
				},
				'shift': {
					'fields':{
					'comment': 'string',
					'startDate': 'date',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date',
					'endDate': 'date'
					},
					'relationships':{
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'study': {
					'fields':{
					'modId': 'string',
					'startDate': 'date',
					'status': 'string',
					'name': 'string',
					'createId': 'string',
					'description': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'user': {
							'entityType': 'user',
							'cardinality': '0,1'
						},
						'studyInvestigations': {
							'entityType': 'studyInvestigation',
							'cardinality': '0,*'
						}
					}
				},
				'studyInvestigation': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'study': {
							'entityType': 'study',
							'cardinality': '1,1'
						},
						'investigation': {
							'entityType': 'investigation',
							'cardinality': '1,1'
						}
					}
				},
				'user': {
					'fields':{
					'name': 'string',
					'fullName': 'string',
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'userGroups': {
							'entityType': 'userGroup',
							'cardinality': '0,*'
						},
						'studies': {
							'entityType': 'study',
							'cardinality': '0,*'
						},
						'investigationUsers': {
							'entityType': 'investigationUser',
							'cardinality': '0,*'
						},
						'instrumentScientists': {
							'entityType': 'instrumentScientist',
							'cardinality': '0,*'
						}
					}
				},
				'userGroup': {
					'fields':{
					'modId': 'string',
					'createId': 'string',
					'createTime': 'date',
					'modTime': 'date'
					},
					'relationships':{
						'grouping': {
							'entityType': 'grouping',
							'cardinality': '1,1'
						},
						'user': {
							'entityType': 'user',
							'cardinality': '1,1'
						}
					}
				}
			}
		};

		function findPossibleVariablePaths(entityType, path, visited, paths){
			if(!path){
				path = [];
				visited = [];
				paths = {};
			}
			if(out.entityTypes[entityType]){
				_.each(out.entityTypes[entityType]['relationships'], function(relationship, relationshipName){
					if(!relationship['variableName']) return;
					var currentPath = _.clone(path);
					var currentVisited = _.clone(visited);
					currentPath.push(relationshipName);
					if(!paths[relationship['variableName']]) paths[relationship['variableName']] = [];
					paths[relationship['variableName']].push(currentPath);
					if(!_.include(visited, relationship['entityType'])){
						currentVisited.push(relationship['entityType']);
						findPossibleVariablePaths(relationship['entityType'], currentPath, currentVisited, paths);
					}
				});
			}

			return paths;
		}

		function findVariablePaths(entityType){
			var out = {}
			_.each(findPossibleVariablePaths(entityType), function(paths, variableName){
				paths = _.sortBy(paths, function(path){
					return path.length;
				});
				out[variableName] = paths[0];
			});
			return out;
		}

		_.each(out.entityTypes, function(entitySchema, entityType){
			entitySchema['variablePaths'] = findVariablePaths(entityType);
		});

		out.variableEntityTypes = {};

		_.each(out.entityTypes, function(entityTypeSchema){
			_.each(entityTypeSchema.relationships, function(relationship){
				if(relationship.variableName){
					out.variableEntityTypes[relationship.variableName] = relationship.entityType;
				}
			});
		});

		return out;
    });
    	

})();
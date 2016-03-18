
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.factory('icatSchema', function(){
    	var out = {
    		'entities': {
				'Application': {
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
							'entity': 'Job',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1'
						}
					}
				},
				'DataCollection': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'dataCollectionDatasets': {
							'entity': 'DataCollectionDataset',
							'cardinality': '0,*'
						},
						'jobsAsInput': {
							'entity': 'Job',
							'cardinality': '0,*'
						},
						'jobsAsOutput': {
							'entity': 'Job',
							'cardinality': '0,*'
						},
						'dataCollectionDatafiles': {
							'entity': 'DataCollectionDatafile',
							'cardinality': '0,*'
						},
						'parameters': {
							'entity': 'DataCollectionParameter',
							'cardinality': '0,*'
						}
					}
				},
				'DataCollectionDatafile': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'dataCollection': {
							'entity': 'DataCollection',
							'cardinality': '1,1'
						},
						'datafile': {
							'entity': 'Datafile',
							'cardinality': '1,1'
						}
					}
				},
				'DataCollectionDataset': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'dataCollection': {
							'entity': 'DataCollection',
							'cardinality': '1,1'
						},
						'dataset': {
							'entity': 'Dataset',
							'cardinality': '1,1'
						}
					}
				},
				'DataCollectionParameter': {
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
							'entity': 'DataCollection',
							'cardinality': '1,1'
						},
						'type': {
							'entity': 'ParameterType',
							'cardinality': '1,1'
						}
					}
				},
				'Datafile': {
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
							'entity': 'DataCollectionDatafile',
							'cardinality': '0,*'
						},
						'datafileFormat': {
							'entity': 'DatafileFormat',
							'cardinality': '0,1'
						},
						'sourceDatafiles': {
							'entity': 'RelatedDatafile',
							'cardinality': '0,*'
						},
						'parameters': {
							'entity': 'DatafileParameter',
							'cardinality': '0,*'
						},
						'dataset': {
							'entity': 'Dataset',
							'cardinality': '1,1',
							'variableName': 'dataset'
						},
						'destDatafiles': {
							'entity': 'RelatedDatafile',
							'cardinality': '0,*'
						}
					}
				},
				'DatafileFormat': {
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
							'entity': 'Facility',
							'cardinality': '1,1'
						},
						'datafiles': {
							'entity': 'Datafile',
							'cardinality': '0,*'
						}
					}
				},
				'DatafileParameter': {
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
							'entity': 'ParameterType',
							'cardinality': '1,1'
						},
						'datafile': {
							'entity': 'Datafile',
							'cardinality': '1,1'
						}
					}
				},
				'Dataset': {
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
							'entity': 'Sample',
							'cardinality': '0,1'
						},
						'dataCollectionDatasets': {
							'entity': 'DataCollectionDataset',
							'cardinality': '0,*'
						},
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1',
							'variableName': 'investigation'
						},
						'datafiles': {
							'entity': 'Datafile',
							'cardinality': '0,*',
							'variableName': 'datafile'
						},
						'type': {
							'entity': 'DatasetType',
							'cardinality': '1,1'
						},
						'parameters': {
							'entity': 'DatasetParameter',
							'cardinality': '0,*'
						}
					}
				},
				'DatasetParameter': {
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
							'entity': 'ParameterType',
							'cardinality': '1,1'
						},
						'dataset': {
							'entity': 'Dataset',
							'cardinality': '1,1'
						}
					}
				},
				'DatasetType': {
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
							'entity': 'Dataset',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1'
						}
					}
				},
				'Facility': {
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
							'entity': 'Instrument',
							'cardinality': '0,*'
						},
						'facilityCycles': {
							'entity': 'FacilityCycle',
							'cardinality': '0,*',
							'variableName': 'facilityCycle'
						},
						'investigationTypes': {
							'entity': 'InvestigationType',
							'cardinality': '0,*'
						},
						'datafileFormats': {
							'entity': 'DatafileFormat',
							'cardinality': '0,*'
						},
						'investigations': {
							'entity': 'Investigation',
							'cardinality': '0,*',
							'variableName': 'investigation'
						},
						'sampleTypes': {
							'entity': 'SampleType',
							'cardinality': '0,*'
						},
						'parameterTypes': {
							'entity': 'ParameterType',
							'cardinality': '0,*'
						},
						'datasetTypes': {
							'entity': 'DatasetType',
							'cardinality': '0,*'
						},
						'applications': {
							'entity': 'Application',
							'cardinality': '0,*'
						}
					}
				},
				'FacilityCycle': {
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
							'entity': 'Facility',
							'cardinality': '1,1',
							'variableName': 'facility'
						}
					}
				},
				'Grouping': {
					'fields':{
						'modId': 'string',
						'name': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'investigationGroups': {
							'entity': 'InvestigationGroup',
							'cardinality': '0,*'
						},
						'rules': {
							'entity': 'Rule',
							'cardinality': '0,*'
						},
						'userGroups': {
							'entity': 'UserGroup',
							'cardinality': '0,*'
						}
					}
				},
				'Instrument': {
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
							'entity': 'Facility',
							'cardinality': '1,1'
						},
						'instrumentScientists': {
							'entity': 'InstrumentScientist',
							'cardinality': '0,*'
						},
						'investigationInstruments': {
							'entity': 'InvestigationInstrument',
							'cardinality': '0,*'
						}
					}
				},
				'InstrumentScientist': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'instrument': {
							'entity': 'Instrument',
							'cardinality': '1,1'
						},
						'user': {
							'entity': 'User',
							'cardinality': '1,1'
						}
					}
				},
				'Investigation': {
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
							'entity': 'Keyword',
							'cardinality': '0,*'
						},
						'shifts': {
							'entity': 'Shift',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1',
							'variableName': 'facility'
						},
						'datasets': {
							'entity': 'Dataset',
							'cardinality': '0,*',
							'variableName': 'dataset'
						},
						'investigationGroups': {
							'entity': 'InvestigationGroup',
							'cardinality': '0,*'
						},
						'publications': {
							'entity': 'Publication',
							'cardinality': '0,*'
						},
						'type': {
							'entity': 'InvestigationType',
							'cardinality': '1,1'
						},
						'investigationInstruments': {
							'entity': 'InvestigationInstrument',
							'cardinality': '0,*',
							'variableName': 'investigationInstrument'
						},
						'parameters': {
							'entity': 'InvestigationParameter',
							'cardinality': '0,*'
						},
						'studyInvestigations': {
							'entity': 'StudyInvestigation',
							'cardinality': '0,*'
						},
						'samples': {
							'entity': 'Sample',
							'cardinality': '0,*'
						},
						'investigationUsers': {
							'entity': 'InvestigationUser',
							'cardinality': '0,*',
							'variableName': 'investigationUser'
						}
					}
				},
				'InvestigationGroup': {
					'fields':{
						'role': 'string',
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						},
						'grouping': {
							'entity': 'Grouping',
							'cardinality': '1,1'
						}
					}
				},
				'InvestigationInstrument': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						},
						'instrument': {
							'entity': 'Instrument',
							'cardinality': '1,1',
							'variableName': 'investigationInstrumentInstrument'
						}
					}
				},
				'InvestigationParameter': {
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
							'entity': 'Investigation',
							'cardinality': '1,1'
						},
						'type': {
							'entity': 'ParameterType',
							'cardinality': '1,1'
						}
					}
				},
				'InvestigationType': {
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
							'entity': 'Investigation',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1'
						}
					}
				},
				'InvestigationUser': {
					'fields':{
						'modId': 'string',
						'role': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'user': {
							'entity': 'User',
							'cardinality': '1,1',
							'variableName': 'investigationUserUser'
						},
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'Job': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date',
						'arguments': 'string'
					},
					'relationships':{
						'inputDataCollection': {
							'entity': 'DataCollection',
							'cardinality': '0,1'
						},
						'outputDataCollection': {
							'entity': 'DataCollection',
							'cardinality': '0,1'
						},
						'application': {
							'entity': 'Application',
							'cardinality': '1,1'
						}
					}
				},
				'Keyword': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date',
						'name': 'string'
					},
					'relationships':{
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'Log': {
					'fields':{
						'operation': 'string',
						'modId': 'string',
						'entityId': 'number',
						'entityName': 'string',
						'query': 'string',
						'duration': 'number',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
					}
				},
				'ParameterType': {
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
							'entity': 'InvestigationParameter',
							'cardinality': '0,*'
						},
						'permissibleStringValues': {
							'entity': 'PermissibleStringValue',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1'
						},
						'dataCollectionParameters': {
							'entity': 'DataCollectionParameter',
							'cardinality': '0,*'
						},
						'sampleParameters': {
							'entity': 'SampleParameter',
							'cardinality': '0,*'
						},
						'datasetParameters': {
							'entity': 'DatasetParameter',
							'cardinality': '0,*'
						},
						'datafileParameters': {
							'entity': 'DatafileParameter',
							'cardinality': '0,*'
						}
					}
				},
				'PermissibleStringValue': {
					'fields':{
						'modId': 'string',
						'value': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'type': {
							'entity': 'ParameterType',
							'cardinality': '1,1'
						}
					}
				},
				'Proposal': {
					'fields': {
						'name': 'string'
					},
					'relationships':{
						'investigations': {
							'entity': 'Investigation',
							'cardinality': '1,*',
							'variableName': 'investigation'
						}
					}
				},
				'PublicStep': {
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
				'Publication': {
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
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'RelatedDatafile': {
					'fields':{
						'modId': 'string',
						'relation': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'destDatafile': {
							'entity': 'Datafile',
							'cardinality': '1,1'
						},
						'sourceDatafile': {
							'entity': 'Datafile',
							'cardinality': '1,1'
						}
					}
				},
				'Rule': {
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
							'entity': 'Grouping',
							'cardinality': '0,1'
						}
					}
				},
				'Sample': {
					'fields':{
						'name': 'string',
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'parameters': {
							'entity': 'SampleParameter',
							'cardinality': '0,*'
						},
						'datasets': {
							'entity': 'Dataset',
							'cardinality': '0,*'
						},
						'type': {
							'entity': 'SampleType',
							'cardinality': '0,1'
						},
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'SampleParameter': {
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
							'entity': 'Sample',
							'cardinality': '1,1'
						},
						'type': {
							'entity': 'ParameterType',
							'cardinality': '1,1'
						}
					}
				},
				'SampleType': {
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
							'entity': 'Sample',
							'cardinality': '0,*'
						},
						'facility': {
							'entity': 'Facility',
							'cardinality': '1,1'
						}
					}
				},
				'Shift': {
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
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'Study': {
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
							'entity': 'User',
							'cardinality': '0,1'
						},
						'studyInvestigations': {
							'entity': 'StudyInvestigation',
							'cardinality': '0,*'
						}
					}
				},
				'StudyInvestigation': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'study': {
							'entity': 'Study',
							'cardinality': '1,1'
						},
						'investigation': {
							'entity': 'Investigation',
							'cardinality': '1,1'
						}
					}
				},
				'User': {
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
							'entity': 'UserGroup',
							'cardinality': '0,*'
						},
						'studies': {
							'entity': 'Study',
							'cardinality': '0,*'
						},
						'investigationUsers': {
							'entity': 'InvestigationUser',
							'cardinality': '0,*'
						},
						'instrumentScientists': {
							'entity': 'InstrumentScientist',
							'cardinality': '0,*'
						}
					}
				},
				'UserGroup': {
					'fields':{
						'modId': 'string',
						'createId': 'string',
						'createTime': 'date',
						'modTime': 'date'
					},
					'relationships':{
						'grouping': {
							'entity': 'Grouping',
							'cardinality': '1,1'
						},
						'user': {
							'entity': 'User',
							'cardinality': '1,1'
						}
					}
				}
			}
		};

		function findPossibleVariablePaths(entityName, path, visited, paths){
			if(!path){
				path = [];
				visited = [];
				paths = {};
			}
			if(out.entities[entityName]){
				_.each(out.entities[entityName]['relationships'], function(relationship, relationshipName){
					if(!relationship['variableName']) return;
					var currentPath = _.clone(path);
					var currentVisited = _.clone(visited);
					currentPath.push(relationshipName);
					if(!paths[relationship['variableName']]) paths[relationship['variableName']] = [];
					paths[relationship['variableName']].push(currentPath);
					if(!_.include(visited, relationship['entity'])){
						currentVisited.push(relationship['entity']);
						findPossibleVariablePaths(relationship['entity'], currentPath, currentVisited, paths);
					}
				});
			}

			return paths;
		}

		function findVariablePaths(entityName){
			var out = {}
			_.each(findPossibleVariablePaths(entityName), function(paths, variableName){
				paths = _.sortBy(paths, function(path){
					return path.length;
				});
				out[variableName] = paths[0];
			});
			return out;
		}

		_.each(out.entities, function(entitySchema, entityName){
			entitySchema['variablePaths'] = findVariablePaths(entityName);
		});

		out.variables = {};

		_.each(out.entities, function(entityTypeSchema){
			_.each(entityTypeSchema.relationships, function(relationship){
				if(relationship.variableName){
					out.variables[relationship.variableName] = relationship.entity;
				}
			});
		});

		return out;
    });
    	

})();
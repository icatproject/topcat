'use strict';

describe('MetaDataManagerTest', function () {

	var $rootScope, scope, metaDataManager, mockInstrument, mockInvestigation, mockDataSet, constructExpectedResults, compareResults, tabs, config;

	beforeEach(function() {
		module(function($provide) {
			$provide.constant('LANG', {});
			$provide.constant('APP_CONFIG', readJSON('test/mock/data/mock-config-multi.json'));
		});
	});

	beforeEach(function () {
		module('angularApp');
	});

	beforeEach(inject(function ($injector) {
		$rootScope = $injector.get('$rootScope');
		scope = $rootScope.$new();

		metaDataManager = $injector.get('MetaDataManager');
		config = $injector.get('Config');

		mockInstrument = [{
			'fullName': 'testIns',
			'description': 'test instrument',
			'type': 'test instrument type',
			'url': 'test URL'
		}];
		mockInvestigation = [{
			'name' : 'test investigation',
			'title' : 'test investigation title',
			'summary' : 'test investigation summary',
			'startDate' : 'test investigation start date',
			'endDate' : 'test investigation end date',
			'investigationUsers' : [ { 'user' : { 'fullName' : 'test user' } } ]
		}];
		mockDataSet = [{
			'name': 'testDS',
			'title': 'test DS title',
			'description': 'test DS description',
			'startDate': 'test DS start date',
			'endDate': 'test DS endDate',
			'type' : [ { 'name' : 'test type', 'description' : 'test Ds type description' } ]
		}];


		constructExpectedResults = function(mockResult, tabs) {

			var expectedResults = [];

			for (var count in tabs) {

				var results = mockResult;
				var tab = tabs[count];
				var expectedResult = {
					'title': tab['title'],
					'content': ''
				};

				if(tab['default'] === false) {
					results = results[0][tab['icatName']];
				}

				for (var i in tab['data']) {

					var data = tab['data'][i];

					while (data['data'] != undefined) 
					{
						results = results[0][data['icatName']];
						data = data['data'][0];
					}
					if(!Array.isArray(results)) {
						results = [results];
					}
					expectedResult['content'] += data['title'] + ': ' + results[0][data['icatName']] + '<br>';
				}
				expectedResults.push(expectedResult);
			}
			return expectedResults;
		};

		compareResults = function(expectedResults, results) {

			expect(results.length).toEqual(expectedResults.length);

			for (var index in results) {
				var currentTab = results[index];
				expect(currentTab).toEqual(expectedResults[index]);
			}
		};

	}));

	it('should correctly extract the meta data from an ICAT object with no relationships', function() {
		var tabs = config.getMetaTabsByEntityType(readJSON('test/mock/data/mock-config-multi.json'), 'isis', 'instrument');

		var expectedResults = constructExpectedResults(mockInstrument, tabs);
		var results = metaDataManager.updateTabs(mockInstrument, tabs);
		compareResults(expectedResults, results);
	});

	it('should correctly extract the meta data from an ICAT object with one level of relationships', function() {
		var tabs = config.getMetaTabsByEntityType(readJSON('test/mock/data/mock-config-multi.json'), 'isis', 'dataset');

		var expectedResults = constructExpectedResults(mockDataSet, tabs);
		var results = metaDataManager.updateTabs(mockDataSet, tabs);
		compareResults(expectedResults, results);
	});

	it('should correctly extract the meta data from an ICAT object with multiple levels of relationships', function () {
		var tabs = config.getMetaTabsByEntityType(readJSON('test/mock/data/mock-config-multi.json'), 'isis', 'investigation');

		var expectedResults = constructExpectedResults(mockInvestigation, tabs);
		var results = metaDataManager.updateTabs(mockInvestigation, tabs);
		compareResults(expectedResults, results);
	});

	it('should return an error if the config file is invalid', function () {

	});

	it('should not return any meta data that is undefined', function() {

	});

	it('should correctly extract the ICAT query options', function() {

	});

});
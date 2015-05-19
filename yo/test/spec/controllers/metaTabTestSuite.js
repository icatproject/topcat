'use strict';

describe('MetatabTest', function() {

	var $rootScope, controller, createController, scope, stateParams, state, createDataManager, dataManager, config, testRowClickMessage, mockInstrument, createMockRouteUtils, routeUtils;

	beforeEach(function() {
		module(function($provide) {
			$provide.constant('LANG', {});
			$provide.constant('APP_CONFIG',
				readJSON('test/mock/data/mock-config-multi.json')
			);
		});
	});

	beforeEach(function() {
		module('angularApp');
	});

	beforeEach(inject(function($injector, $q) {

		$rootScope = $injector.get('$rootScope');
		scope = $rootScope.$new();
		var $controller = $injector.get('$controller');
		config = $injector.get('Config');

		stateParams = {
			'facilityName': 'isis'
		};
		state = {
			'current': {
				'param': {
					'entityType': ''
				}
			}
		};

		mockInstrument = [{
			'name': 'testIns',
			'description': 'test instrument',
			'type': 'test instrument type',
			'url': 'test URL'
		}];

		testRowClickMessage = {
			'Type': 'Test',
			'Id': 101
		};

		dataManager = {};
		routeUtils = {};

		createMockRouteUtils = function() {
			routeUtils.getCurrentEntityType = function(state) {
				return state.current.param.entityType;
			};
		};

		createDataManager = function(mockResult) {
			dataManager.getEntityById = function() {
				var deferred = $q.defer();
				deferred.resolve(mockResult);
				return deferred.promise;
			};
		};

		createController = function() {
			return $controller('MetaPanelController', {
				'$scope': scope,
				'Config': config,
				'DataManager': dataManager,
				'$stateParams': stateParams,
				'$state': state,
				'RouteUtils' : routeUtils
			});
		};
	}));

	it('should set message variable to broadcast value', function() {

		state.current.param.entityType = 'instrument';
		createDataManager(mockInstrument);
		createMockRouteUtils(state);
		controller = createController();

		$rootScope.$broadcast('rowclick', testRowClickMessage);
		scope.$digest();

		expect(scope.message.Type).toEqual('Test');
		expect(scope.message.Id).toEqual(101);
	});
});
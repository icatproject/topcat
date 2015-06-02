'use strict';

/*jshint unused:false*/
describe('Service: RouteService', function() {
    beforeEach(function() {
        module(function($provide) {
            $provide.constant('LANG', {});
            $provide.constant('APP_CONFIG', readJSON('test/mock/data/mock-config-multi.json'));
        });
    });

    var possibleRoutes = [
        //'facility-instrument',
        '/{facilityName}/instrument',
        //'facility-proposal',
        '/{facilityName}/proposal',
        //'facility-investigation',
        '/{facilityName}/investigation',
        //'facility-dataset',
        '/{facilityName}/dataset',
        //'facility-datafile',
        '/{facilityName}/datafile',
        //'instrument-proposal',
        '/{facilityName}/instrument/{instrumentId}/proposal',
        //'instrument-investigation',
        '/{facilityName}/instrument/{instrumentId}/investigation',
        //'instrument-dataset',
        '/{facilityName}/instrument/{instrumentId}/dataset',
        //'instrument-datafile',
        '/{facilityName}/instrument/{instrumentId}/datafile',
        //'proposal-investigation',
        '/{facilityName}/proposal/{proposalId}/investigation',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation',
        //'proposal-dataset',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/dataset',
        '/{facilityName}/proposal/{proposalId}/dataset',
        //'proposal-datafile',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/datafile',
        '/{facilityName}/proposal/{proposalId}/datafile',
        //'investigation-dataset',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/dataset',
        '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/dataset',
        '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/dataset',
        '/{facilityName}/investigation/{investigationId}/dataset',
        //'investigation-datafile',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/datafile',
        '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/datafile',
        '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/datafile',
        '/{facilityName}/investigation/{investigationId}/datafile',
        //'dataset-datafile',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
        '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/dataset/{datasetId}/datafile',
        '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
        '/{facilityName}/instrument/{instrumentId}/dataset/{datasetId}/datafile',
        '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
        '/{facilityName}/proposal/{proposalId}/dataset/{datasetId}/datafile',
        '/{facilityName}/investigation/{investigationId}/dataset/{datasetId}/datafile',
        '/{facilityName}/dataset/{datsetId}/datafile'
    ];

    // load the service's module
    beforeEach(module('angularApp'));


    // instantiate service
    var RouteService;
    beforeEach(inject(function(_RouteService_) {
        RouteService = _RouteService_;
    }));

    var SERVICE_URL = 'https://localhost:3001/icat/entityManager';



    it('should do something', function() {
        expect(!!RouteService).toBe(true);
    });

    it('Test get RouteService getRoutes 3 hierarchy', function() {
        var hierarchy = [
            'facility',
            'dataset',
            'datafile'
        ];
        var params = RouteService.getRoutes(hierarchy);

        expect(params.length).toEqual(3);

        expect(params).toEqual( [
            {
                'route': 'facility-dataset',
                'url': '/{facilityName}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-datafile',
                'url': '/{facilityName}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-dataset-datafile',
                'url': '/{facilityName}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            }
        ]);
    });

    it('Test get RouteService getRoutes', function() {
        var hierarchy = [
            'facility',
            'investigation',
            'dataset',
            'datafile'
        ];
        var params = RouteService.getRoutes(hierarchy);

        expect(params.length).toEqual(7);

        expect(params).toEqual([
          {
            'route': 'facility-investigation',
            'url': '/{facilityName}/investigation',
            'entity': 'investigation'
          },
          {
            'route': 'facility-dataset',
            'url': '/{facilityName}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-investigation-dataset',
            'url': '/{facilityName}/investigation/{investigationId}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-datafile',
            'url': '/{facilityName}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-investigation-datafile',
            'url': '/{facilityName}/investigation/{investigationId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-dataset-datafile',
            'url': '/{facilityName}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-investigation-dataset-datafile',
            'url': '/{facilityName}/investigation/{investigationId}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          }
        ]);
    });

    it('Test get RouteService getRoutes', function() {
        var hierarchy = [
            'facility',
            'facilityCycle',
            'investigation',
            'dataset',
            'datafile'
        ];
        var params = RouteService.getRoutes(hierarchy);

        expect(params.length).toEqual(15);

        expect(params).toEqual([
          {
            'route': 'facility-facilityCycle',
            'url': '/{facilityName}/facilityCycle',
            'entity': 'facilityCycle'
          },
          {
            'route': 'facility-investigation',
            'url': '/{facilityName}/investigation',
            'entity': 'investigation'
          },
          {
            'route': 'facility-facilityCycle-investigation',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation',
            'entity': 'investigation'
          },
          {
            'route': 'facility-dataset',
            'url': '/{facilityName}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-facilityCycle-dataset',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-investigation-dataset',
            'url': '/{facilityName}/investigation/{investigationId}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-facilityCycle-investigation-dataset',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset',
            'entity': 'dataset'
          },
          {
            'route': 'facility-datafile',
            'url': '/{facilityName}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-facilityCycle-datafile',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-investigation-datafile',
            'url': '/{facilityName}/investigation/{investigationId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-facilityCycle-investigation-datafile',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-dataset-datafile',
            'url': '/{facilityName}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-facilityCycle-dataset-datafile',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-investigation-dataset-datafile',
            'url': '/{facilityName}/investigation/{investigationId}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          },
          {
            'route': 'facility-facilityCycle-investigation-dataset-datafile',
            'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
            'entity': 'datafile'
          }
        ]);
    });

    it('Test get RouteService getRoutes 7 hierarachy full', function() {
        var hierarchy = [
            'facility',
            'instrument',
            'facilityCycle',
            'proposal',
            'investigation',
            'dataset',
            'datafile'
        ];

        var params = RouteService.getRoutes(hierarchy);

        expect(params.length).toEqual(63);

        expect(params).toEqual([
            {
                'route': 'facility-instrument',
                'url': '/{facilityName}/instrument',
                'entity': 'instrument'
            },
            {
                'route': 'facility-facilityCycle',
                'url': '/{facilityName}/facilityCycle',
                'entity': 'facilityCycle'
            },
            {
                'route': 'facility-instrument-facilityCycle',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle',
                'entity': 'facilityCycle'
            },
            {
                'route': 'facility-proposal',
                'url': '/{facilityName}/proposal',
                'entity': 'proposal'
            },
            {
                'route': 'facility-instrument-proposal',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal',
                'entity': 'proposal'
            },
            {
                'route': 'facility-facilityCycle-proposal',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal',
                'entity': 'proposal'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal',
                'entity': 'proposal'
            },
            {
                'route': 'facility-investigation',
                'url': '/{facilityName}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-instrument-investigation',
                'url': '/{facilityName}/instrument/{instrumentId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-facilityCycle-investigation',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-instrument-facilityCycle-investigation',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-proposal-investigation',
                'url': '/{facilityName}/proposal/{proposalId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-instrument-proposal-investigation',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-facilityCycle-proposal-investigation',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-investigation',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation',
                'entity': 'investigation'
            },
            {
                'route': 'facility-dataset',
                'url': '/{facilityName}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-facilityCycle-dataset',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-facilityCycle-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-proposal-dataset',
                'url': '/{facilityName}/proposal/{proposalId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-proposal-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-facilityCycle-proposal-dataset',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-investigation-dataset',
                'url': '/{facilityName}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-investigation-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-facilityCycle-investigation-dataset',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-facilityCycle-investigation-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-proposal-investigation-dataset',
                'url': '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-proposal-investigation-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-facilityCycle-proposal-investigation-dataset',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-investigation-dataset',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/dataset',
                'entity': 'dataset'
            },
            {
                'route': 'facility-datafile',
                'url': '/{facilityName}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-proposal-datafile',
                'url': '/{facilityName}/proposal/{proposalId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-proposal-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-proposal-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-investigation-datafile',
                'url': '/{facilityName}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-investigation-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-investigation-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-investigation-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-proposal-investigation-datafile',
                'url': '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-proposal-investigation-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-proposal-investigation-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-investigation-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-dataset-datafile',
                'url': '/{facilityName}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-dataset-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-proposal-dataset-datafile',
                'url': '/{facilityName}/proposal/{proposalId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-proposal-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-proposal-dataset-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-investigation-dataset-datafile',
                'url': '/{facilityName}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-investigation-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-investigation-dataset-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-investigation-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-proposal-investigation-dataset-datafile',
                'url': '/{facilityName}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-proposal-investigation-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-facilityCycle-proposal-investigation-dataset-datafile',
                'url': '/{facilityName}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            },
            {
                'route': 'facility-instrument-facilityCycle-proposal-investigation-dataset-datafile',
                'url': '/{facilityName}/instrument/{instrumentId}/facilityCycle/{facilityCycleId}/proposal/{proposalId}/investigation/{investigationId}/dataset/{datasetId}/datafile',
                'entity': 'datafile'
            }
        ]);


    });

    it('Test get RouteService getNextRouteSegmentName 3 entities', function() {
        var hierarchy = [
            'facility',
            'dataset',
            'datafile'
        ];

        var currentRouteName = 'facility-dataset';
        var currentEntityName = 'dataset';

        var params = RouteService.getNextRouteSegmentName(hierarchy, currentEntityName);

        expect(params).toEqual('facility-dataset-datafile');
    });


    it('Test get RouteService getNextRouteSegmentName 7 entities 1', function() {
        var hierarchy = [
            'facility',
            'instrument',
            'facilityCycle',
            'proposal',
            'investigation',
            'dataset',
            'datafile'
        ];

        var currentRouteName = 'facility-instrument';
        var currentEntityName = 'instrument';

        var params = RouteService.getNextRouteSegmentName(hierarchy, currentEntityName);

        expect(params).toEqual('facility-instrument-facilityCycle');
    });

    it('Test get RouteService getNextRouteSegmentName 7 entities 2', function() {
        var hierarchy = [
            'facility',
            'instrument',
            'facilityCycle',
            'proposal',
            'investigation',
            'dataset',
            'datafile'
        ];

        var currentRouteName = 'facility-instrument-facilityCycle-proposal-investigation-dataset';
        var currentEntityName = 'dataset';

        var params = RouteService.getNextRouteSegmentName(hierarchy, currentEntityName);

        expect(params).toEqual('facility-instrument-facilityCycle-proposal-investigation-dataset-datafile');
    });

    it('Test get RouteService getNextRouteSegmentName last entity', function() {
        var hierarchy = [
            'facility',
            'instrument',
            'facilityCycle',
            'proposal',
            'investigation',
            'dataset',
            'datafile'
        ];

        var currentRouteName = 'facility-instrument-facilityCycle-proposal-investigation-dataset-datafile';
        var currentEntityName = 'datafile';

        var params = RouteService.getNextRouteSegmentName(hierarchy, currentEntityName);

        expect(params).toEqual('facility-instrument-facilityCycle-proposal-investigation-dataset-datafile');
    });


    it('Test get RouteService getPossibleRoutes', function() {
        var APP_CONFIG = {
            facilities : {
                dls: {
                    hierarchy: [
                        'facility',
                        'dataset'
                    ]
                },
                isis: {
                    hierarchy: [
                        'facility',
                        'instrument',
                        'datafile'
                    ]
                },
                sig: {
                    hierarchy: [
                        'facility',
                        'instrument',
                        'proposal',
                        'datafile'
                    ]
                }
            }
        };

        var params = RouteService.getPossibleRoutes(APP_CONFIG);

        expect(params).toEqual([
            'facility',
            'instrument',
            'proposal',
            'dataset',
            'datafile'
        ]);
    });

    it('Test get RouteService getPossibleRoutes', function() {
        var APP_CONFIG = {
            facilities : {
                dls: {
                    hierarchy: [
                        'facility',
                        'facilityCycle',
                        'dataset'
                    ]
                },
                isis: {
                    hierarchy: [
                        'facility',
                        'instrument',
                        'datafile'
                    ]
                }
            }
        };

        var params = RouteService.getPossibleRoutes(APP_CONFIG);

        expect(params).toEqual([
            'facility',
            'instrument',
            'facilityCycle',
            'dataset',
            'datafile'
        ]);
    });

});



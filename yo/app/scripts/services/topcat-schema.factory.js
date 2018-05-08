
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.factory('topcatSchema', function(){
    	return {
			'entityTypes': {
				'cart':{
					'fields': {
						'createdAt': 'date',
						'facilityName': 'string',
						'updatedAt': 'date',
						'userName': 'string'
					}
				},
				'cartItem': {
					'fields': {
						'entityId': 'number',
						'entityType': 'string',
						'name': 'string'
					}
				},
				'download': {
					'fields': {
						'completedAt': 'date',
						'createdAt': 'date',
						'deletedAt': 'date',
						'email': 'string',
						'facilityName': 'string',
						'fileName': 'string',
						'fullName': 'string',
						'isDeleted': 'boolean',
						'isTwoLevel': 'boolean',
						'preparedId': 'string',
						'status': 'string',
						'transport': 'string',
						'userName': 'string'
					}
				},
				'downloadItem': {
					'fields': {
						'entityId': 'number',
						'entityType': 'string'
					}
				},
				'parentEntity': {
					'fields': {
						'entityId': 'number',
						'entityType': 'string'
					}
				}
			}
		};
    });
    	

})();


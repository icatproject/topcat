

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			pluginUrl + 'scripts/controllers/example-skeleton-plugin.controller.js'
		],

		stylesheets: [],

		configSchema: {
			//see https://github.com/icatproject/topcat/blob/master/yo/app/scripts/services/object-validator.service.js
		},

		setup: function($uibModal, tc){

			tc.ui().registerMainTab('skeleton-plugin', pluginUrl + 'views/example-skeleton-plugin-template.html', {
				insertAfter: 'my-data',
				controller: 'ExampleSkeletonPluginController as exampleSkeletonPluginController'
			});

			tc.ui().registerCartButton('skeleton-plugin', {insertBefore: 'cancel'}, function(){
				$uibModal.open({
                    templateUrl : pluginUrl + 'views/example-skeleton-plugin-template.html',
                    controller: 'ExampleSkeletonPluginController as exampleSkeletonPluginController',
                    size : 'sm'
                })
			});

			tc.ui().registerEntityActionButton('skeleton-plugin', function(){
				alert("Hello World!");
			});

		}
	};
});


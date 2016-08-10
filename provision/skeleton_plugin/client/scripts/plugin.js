

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			pluginUrl + 'scripts/controllers/hello-world.controller.js'
		],

		stylesheets: [],

		configSchema: function(){
			//see https://github.com/icatproject/topcat/blob/master/yo/app/scripts/services/object-validator.service.js
		},

		setup: function($uibModal, tc){

			tc.ui().registerMainTab('hello-world', pluginUrl + 'views/hello-world.html', {
				insertAfter: 'my-data',
				controller: 'HelloWorldController as helloWorldController'
			});

			tc.ui().registerCartButton('hello-world', {insertBefore: 'cancel'}, function(){
				$uibModal.open({
                    templateUrl : pluginUrl + 'views/hello-world.html',
                    controller: 'HelloWorldController as helloWorldController',
                    size : 'sm'
                })
			});

			tc.ui().registerEntityActionButton('hello-world', function(){
				alert("Hello World!");
			});

		}
	};
});




registerTopcatPlugin(function(){
	return {
		scripts: [],
		stylesheets: [],
		setup: function(tc){
			tc.ui().registerMainTab('my-jobs', 'views/login.html', {
				insertAfter: 'my-data',
				controller: 'LoginController as loginController',
				multiFacility: true
			});
		}
	};
});


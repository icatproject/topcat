(function() {
    'use strict';

    //see http://stackoverflow.com/a/25872852/4925354

    angular.
        module('topcat').provider('RuntimeStatesProvider', RuntimeStatesProvider);

    RuntimeStatesProvider.$inject = ['$stateProvider'];

    // config-time dependencies can be injected here at .provider() declaration
    function RuntimeStatesProvider($stateProvider) {
        // runtime dependencies for the service can be injected here, at the provider.$get() function.
        this.$get = function() {
            return {
                addState: function(name, state) {
                    $stateProvider.state(name, state);
                }
            };
        };
    }
})();
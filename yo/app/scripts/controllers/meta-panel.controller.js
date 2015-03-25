(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('MetaPanelController', MetaPanelController);

    MetaPanelController.$inject = [];

    function MetaPanelController(){
        var vm = this;

        vm.tabs = [
            {
                title: 'Meta 1',
                content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce varius eu odio lobortis efficitur. Pellentesque vitae commodo tortor. Fusce placerat lectus a diam facilisis dignissim. Suspendisse efficitur commodo nisl et dictum. Curabitur condimentum arcu nisi, et pretium turpis fermentum non. Donec sodales quam vel lorem tincidunt tempor. Fusce sit amet nisl malesuada, accumsan libero vitae, fermentum lectus. Cras vitae velit lectus. Nulla in arcu ullamcorper dolor eleifend pellentesque vel eu velit.'
            },
            {
                title: 'Meta 2',
                content: 'Maecenas ultricies sapien suscipit, dictum eros ut, vehicula leo. Duis rhoncus condimentum purus. Maecenas at facilisis elit. Nulla metus quam, aliquet at fermentum nec, imperdiet ac eros. Donec consectetur magna ut ante pretium, et laoreet urna bibendum. Morbi nec consequat nibh. Proin accumsan velit nec commodo placerat. Duis tempor placerat dictum. Nulla facilisi. Integer ornare mi a eros ultricies cursus. Praesent ultrices elementum mauris ac interdum.',
            },
            {
                title: 'Meta 3',
                content: 'Vivamus convallis odio sit amet felis congue, sit amet sagittis velit blandit. Maecenas elementum elit scelerisque urna cursus accumsan. Proin vel neque sit amet sem molestie convallis vel non quam. Ut in risus dolor. Aenean semper ullamcorper augue. Ut lacus mauris, aliquet in odio at, accumsan fermentum neque. Nullam gravida molestie porta.'
            }
        ];
    }
})();

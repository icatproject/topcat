'use strict';

var MetaPanelCtrl = function(){
    var vm = this;

    //tabs config
    var tabData = [
        {
            heading: 'Meta Info 1',
            route: 'home.browse',
            params: {
                meta: 1
            },
            options: {
            }
        },
        {
            heading: 'Meta Info 2',
            route: 'home.browse',
            params: {
                meta: 2
            },
            options: {
            }
        },
        {
            heading: 'Meta Info 3',
            route: 'home.browse',
            params: {
                meta: 3
            },
            options: {
            }
        }
    ];

    vm.tabData = tabData;
};

angular.module('angularApp').controller('MetaPanelCtrl', MetaPanelCtrl);
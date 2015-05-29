'use strict';

angular
    .module('angularApp')
    .factory('CartData', CartData);

CartData.$inject = [];

function CartData(){
    return {
        cart: [
            {
                'facility': 'dls',
                'type': 'investigation',
                'id': 123
            },
            {
                'facility': 'dls',
                'type': 'dataset',
                'id': 878
            }
        ]
    };
}


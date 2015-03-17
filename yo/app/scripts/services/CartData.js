'use strict';

var CartData = function(){
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
};

angular.module('angularApp').factory('CartData', CartData);



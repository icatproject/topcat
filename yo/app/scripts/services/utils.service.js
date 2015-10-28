(function() {
    'use strict';

    angular.
        module('angularApp').service('Utils', Utils);

    Utils.$inject = [];

    function Utils() {

        this.getFieldValuesAsHtmlList = function(row, field) {
            var split = field.split(/\[\d+\]/);
            var entities = _.get(row, split[0]);
            if (split[1][0] === '.') {
                split[1] = split[1].substr(1);
            }

            var html = '';

            if (typeof entities !== 'undefined') {
                html = html + '<ul class="list-noindent">';
                _.each(entities, function(entity){
                    var f = _.get(entity, split[1]);
                    html = html + '<li>' + f + '</li>';
                });

                html = html + '</ul>';
            }

            return html;
        };
    }
})();
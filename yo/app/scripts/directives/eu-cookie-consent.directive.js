(function() {
    'use strict';

    angular.
        module('angularApp').directive('consent', consent);

    consent.$inject = [];

    function consent() {
        return {
            restrict: 'A', //E = element, A = attribute, C = class, M = comment
            scope: {},
            template: '<div class="eu-cookie-law-consent">' +
                '    <div class="eu-cookie-law-consent-message" ng-hide="consent()">' +
                '        <p class="pull-left"><span translate="EU_COOKIE_LAW.HTML.MESSAGE"></span> <a ui-sref="{{\'EU_COOKIE_LAW.COOKIE_POLICY_STATENAME\' | translate}}" translate="EU_COOKIE_LAW.HTML.MORE_INFO"></a></p><button type="button" class="btn btn-success eu-cookie-law-consent-button" ng-click="consent(true)" translate="EU_COOKIE_LAW.HTML.CONSENT_BUTTON"></button>' +
                '    </div>' +
                '</div>',
            controller: 'ConsentController'
        };
    }


    angular.
        module('angularApp').controller('ConsentController', ConsentController);

    ConsentController.$inject = ['$scope', 'ipCookie'];

    function ConsentController($scope, ipCookie) {
        var _consent = ipCookie('consent');
        $scope.consent = function(consent) {
            if (consent === undefined) {
                return _consent;
            } else if (consent) {
                ipCookie('consent', true, { expires: 365 });
                _consent = true;
            }
        };
    }

})();
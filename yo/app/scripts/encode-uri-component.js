/*
    This is a temporary fix for the "Url hash containing colon issue" #115. i.e. 

    "Url hash with colon causes problem with the browser back button. The back button just doesn't
    work even though the history is saved and listed in the browser. This happens in Chrome (not sure
    if same for firefox).

    Example:
        https://localhost:9000/#/browse/facilities/dls/proposal/CAL_i11_2010-04-29T10:15:26/instrument/16/investigation."

    Angular has an internal funtion called fireUrlChange which updates the browser history twice.

    This is because when it compares urls they don't match, because ui router escapes the chars one way,
    and Angualar does it another way. The method below normalises the escaping as they both use the
    native encodeURIComponent method.

    A better (less hacky) fix might be to use ui-sref instead of href in the ui-grid cellTemplate.
*/

(function(){
    'use strict';

    var _encodeURIComponent = window.encodeURIComponent;

    window.encodeURIComponent = function(value){
        return _encodeURIComponent(value).
            replace(/%40/gi, '@').
            replace(/%3A/gi, ':').
            replace(/%24/g, '$').
            replace(/%2C/gi, ',').
            replace(/%3B/gi, ';');
    };

})();


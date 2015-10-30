
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


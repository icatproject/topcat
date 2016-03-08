
(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcCache', function($cacheFactory, $q, helpers){

      this.create = function(name){
        return new Cache(name);
      };

      function Cache(name){
        var store = $cacheFactory(name);

        this.get = helpers.overload({
          'string, function': function(key, fn){
            var out = store.get(key);
            if(out === undefined) {
              out = fn();
              store.put(key, out);
            }
            return out;
          },
          'string': function(key){
            return store.get(key);
          }
        });

        this.put = function(key, value){
          store.put(key, value);
        };

        this.remove = function(key){
          store.remove(key);
        };

        this.removeAll = function(){
          store.removeAll();
        };

        this.info = function(){
          return store.info();
        };

        this.getPromise = function(key, fn){
          var defered = $q.defer();
          var out = store.get(key);
          if(out === undefined){
            fn().then(function(value){
              store.put(key, value);
              defered.resolve(value);
            }, function(results){
              defered.reject(results);
            }, function(results){
              defered.notify(results);
            });
          } else {
            defered.resolve(out);
          }
          return defered.promise;
        };
      }

  	});

})();
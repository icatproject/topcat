
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcCache', function($cacheFactory, $q, $timeout, $rootScope, helpers){

      this.create = function(name){
        return new Cache(name);
      };

      function Cache(name){
        var store = $cacheFactory(name);

        this.get = helpers.overload({
          'string, number, function': function(key, seconds, fn){
            var out = store.get(key);
            var nowSeconds = (new Date).getTime() /  1000;
            var putSeconds = store.get("putSeconds:" + key);

            if(out === undefined || (putSeconds && (nowSeconds - putSeconds) > seconds)){
              out = fn();
              store.put(key, out);
              if(seconds > 0){
                store.put("putSeconds:" + key, nowSeconds);
              }
            }
            return out;
          },
          'string, function': function(key, fn){
            return this.get(key, 0, fn);
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

        this.getPromise = helpers.overload({
          'string, number, function': function(key, seconds, fn){
            var defered = $q.defer();
            var out = store.get(key);
            var nowSeconds = (new Date).getTime() /  1000;
            var putSeconds = store.get("putSeconds:" + key);

            $rootScope.requestCounter++;
            $rootScope.updateLoadingState();

            if(out === undefined || (putSeconds && (nowSeconds - putSeconds) > seconds)){
              fn().then(function(value){
                store.put(key, value);
                if(seconds > 0){
                  store.put("putSeconds:" + key, (new Date).getTime() /  1000);
                }
                defered.resolve(value);
              }, function(results){
                defered.reject(results);
              }, function(results){
                defered.notify(results);
              });
            } else {
              $timeout(function(){
                defered.resolve(out);
              });
            }
            
            return defered.promise.then(function(value){
              $rootScope.requestCounter--;
              $rootScope.updateLoadingState();
              return value;
            }, function(results){
              $rootScope.requestCounter--;
              $rootScope.updateLoadingState();
              return $q.reject(results);
            });
          },
          'string, function': function(key, fn){
            return this.getPromise(key, 0, fn);
          }
        });

      }

  	});

})();

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcCache', function($cacheFactory, $q, $timeout, $rootScope, helpers){

      this.create = function(name){
        return new Cache(name);
      };

      /**
       * @interface Cache
       */
      function Cache(name){
        var store = $cacheFactory(name);

        this.get = helpers.overload({
          /**
           * Tries to get a value from cache, if the value does not exist it will run the function, try and store the output for the specified seconds and then return the value.
           *
           * @method
           * @name Cache#get
           * @param  {string} key the key by which the cached value is accessed
           * @param  {number} seconds the length of time the cache tries to store the value for
           * @param  {Function} fn if no values has been cached it will run this function to create the value
           * @return {object}
           */
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

          /**
           * Tries to get a value from cache, if the value does not exist it will run the function, try and store the output for as long as it can and then return the value.
           *
           * @method
           * @name Cache#get
           * @param  {string} key the key by which the cached value is accessed
           * @param  {Function} fn if no values has been cached it will run this function to create the value
           * @return {object}
           */
          'string, function': function(key, fn){
            return this.get(key, 0, fn);
          },

          /**
           * Gets a value from cache.
           *
           * @method
           * @name Cache#get
           * @param  {string} key the key by which the cached value is accessed
           * @return {object}
           */
          'string': function(key){
            return store.get(key);
          }
        });

        /**
         * Puts a value into cache.
         *
         * @method
         * @name Cache#put
         * @param  {string} key the key by which the cached value is accessed
         * @param  {object} value the value to be cached
         * @return {object}
         */
        this.put = function(key, value){
          store.put(key, value);
        };

        /**
         * Removes a value from cache.
         *
         * @method
         * @name Cache#remove
         * @param  {string} key the key by which the cached value is accessed
         */
        this.remove = function(key){
          store.remove(key);
        };

        /**
         * Removes all values from cache i.e. resets it.
         *
         * @method
         * @name Cache#removeAll
         */
        this.removeAll = function(){
          store.removeAll();
        };

        /**
         * Provides info about the state of the cache; useful for debugging.
         *
         * @method
         * @name Cache#info
         */
        this.info = function(){
          return store.info();
        };

        this.getPromise = helpers.overload({
          /**
           * Tries to get a value from cache as a resolved promise, if the value not exist it will run the function which must return a promise, then returns a promise. Once the generated promise has been resolved it caches the returned value (and try and store it for the specified number of seconds), and then resolves the returned promise with this value.
           *
           * @method
           * @name Cache#getPromise
           * @param  {string} key the key by which the cached value is accessed
           * @param  {number} seconds the length of time the cache tries to store the value for
           * @param  {Function} fn if no values has been cached it will run this function to create a promise
           * @return {Promise<object>}
           */
          'string, number, function': function(key, seconds, fn){
            var defered = $q.defer();
            var out = store.get(key);
            var nowSeconds = (new Date).getTime() /  1000;
            var putSeconds = store.get("putSeconds:" + key);

            $rootScope.requestCounter++;

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
              return value;
            }, function(results){
              $rootScope.requestCounter--;
              return $q.reject(results);
            });
          },

          /**
           * Tries to get a value from cache as a resolved promise, if the value not exist it will run the function which must return a promise, then returns a promise. Once the generated promise has been resolved it caches the returned value (for as long as it can), and then resolves the returned promise with this value.
           *
           * @method
           * @name Cache#getPromise
           * @param  {string} key the key by which the cached value is accessed
           * @param  {Function} fn if no values has been cached it will run this function to create a promise
           * @return {Promise<object>}
           */
          'string, function': function(key, fn){
            return this.getPromise(key, 0, fn);
          }
        });

      }

  	});

})();
/**
 *
 * This is the main entry point for Javascript API. It has been exposed globally so can be accessed 
 * in the Browsers console simply by typing 'tc'.
 *
 * @interface tc
**/

(function() {
  'use strict';

  var app = angular.module('topcat');

  app.service('tc', function($sessionStorage, $q, $state, $timeout, $rootScope, helpers, tcFacility, tcIcatEntity, tcCache, tcUi, APP_CONFIG, plugins, $injector){
  	var tc = this;
  	window.tc = this;
  	var facilities = {};
    var cache;
    var ui;


    /**
     * Returns a facility by name.
     * 
     * @method
     * @name tc#facility
     * @param {string} name The name of the facility as it appears on the database
     * @return {Facility}
     **/
  	this.facility = function(name){
  		if(!facilities[name]) facilities[name] = tcFacility.create(tc, name);
  		return facilities[name];
  	};

    /**
     *  Returns an array of all the available facilities
     * 
     * @method
     * @name tc#facilities
     * @return {Facility[]}
     **/
  	this.facilities = function(){
  		return _.map(APP_CONFIG.facilities, function(facility){ return tc.facility(facility.name); });
  	};

    /**
     * Returns the site configuration in topcat.json
     *
     * @method
     * @name  tc#config
     * @return {object} the topcat.json site configuration
     */
  	this.config = function(){ 
      var out = APP_CONFIG.site;
      if(!out.topcatUrl) out.topcatUrl = window.location.href.replace(/^(https{0,1}:\/\/[^\/]+).*$/, '$1');
      return out;
    }

    /**
     *  Returns an object that can be used for caching
     *
     * @method
     * @name  tc#cache
     * @return {Cache}
     */
    this.cache = function(){
      if(!cache) cache = tcCache.create('topcat');
      return cache;
    };

    /**
     * Returns an object that is used to alter the Topcat interface.
     *
     * @method
     * @name  tc#ui
     * @return {UI}
     */
    this.ui = function(){
      if(!ui) ui = tcUi.create(this);
      return ui;
    };

    /**
     *
     * @method
     * @name  tc#version
     * @return {Promise<string>}
     */
  	this.version = function(){
			return this.get('version').then(function(version){
				return version;
			});
    };

		this.search = helpers.overload({
      /**
       *
       * @method
       * @name  tc#search
       * @param {string[]} facilityNames the names of the facilities to search across as they appear on the database
       * @param {object} query {@link https://repo.icatproject.org/site/icat/server/4.8.0/miredot/index.html#280599542|as specified in the Icat documentation}
       * @param {object} options {@ https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
       * @return {Promise<IcatEntity[]>}
       */
			'array, object, object': function(facilityNames, query, options){
				var defered = $q.defer();
				var promises = [];
				var results = [];
				query.target = query.target.replace(/^./, function(c){ return c.toUpperCase(); });
				var entityType = query.target;
				var entityInstanceName = helpers.uncapitalize(entityType);
				_.each(facilityNames, function(facilityName){
					var facility = tc.facility(facilityName);
					var icat = facility.icat();
          var key = "search:" + JSON.stringify(query);

          promises.push(icat.cache().getPromise(key, 10 * 60 * 60, function(){
            return icat.get('lucene/data', {
              sessionId: icat.session().sessionId,
              query: JSON.stringify(query),
              maxCount: 300
            }, options);
          }).then(function(data){
            if(data && data.length > 0){
              var ids = [];
              var scores = {};
              _.each(data, function(result){
                ids.push(result.id);
                scores[result.id] = result.score;
              });

              var promises = [];
              _.each(_.chunk(ids, 100), function(ids){
                var query = [
                  function(){
                    if(entityType == 'Investigation'){
                      return 'select investigation from Investigation investigation';
                    } else if(entityType == 'Dataset') {
                      return 'select dataset from Dataset dataset';
                    } else {
                      return 'select datafile from Datafile datafile';
                    }
                  },
                  'where ?.id in (?)', entityInstanceName.safe(), ids.join(', ').safe(),
                  function(){
                    if(entityType == 'Investigation'){
                      return 'include investigation.investigationInstruments.instrument';
                    } else if(entityType == 'Dataset'){
                      return 'include dataset.investigation';
                    } else if(entityType == 'Datafile') {
                      return 'include datafile.dataset.investigation';
                    }
                  }
                ];
                promises.push(icat.query(query, options).then(function(_results){
                  var _results = _.map(_results, function(result){
                    result.facilityName = facilityName;
                    result.score = scores[result.id];
                    result = tcIcatEntity.create(result, facility);
                    return result;
                  });

                  results = _.sortBy(_.flatten([results, _results]), 'score').reverse();
                  defered.notify(results);
                }));
              })
              return $q.all(promises);
            }
          }));

				});

				$q.all(promises).then(function(){
					defered.resolve(results);
				}, function(response){
					defered.reject(response);
				});

				return defered.promise;
			},
      /**
       * @method
       * @name  tc#search
       * @param {string[]} facilityNames the names of the facilities to search across as they appear on the database
       * @param {Promise} timeout will cancel request if resolved
       * @param {object} query {@link https://repo.icatproject.org/site/icat/server/4.8.0/miredot/index.html#280599542|as specified in the Icat documentation}
       * @return {Promise<IcatEntity[]>}
       */
			'array, promise, object': function(facilityNames, timeout, query){
				return this.search(facilityNames, query, {timeout: timeout});
			},
      /**
       * @method
       * @name  tc#search
       * @param {string[]} facilityNames the names of the facilities to search across as they appear on the database
       * @param {object} query {@link https://repo.icatproject.org/site/icat/server/4.8.0/miredot/index.html#280599542|as specified in the Icat documentation}
       * @return {Promise<IcatEntity[]>}
       */
			'array, object': function(facilityNames, query){
				return this.search(facilityNames, query, {});
			},
      /**
       * @method
       * @name  tc#search
       * @param {string[]} facilityNames the names of the facilities to search across as they appear on the database
       * @param {string} target the entity type e.g. 'investigation', 'dataset' or 'datafile'
       * @param {string} text the search phrase
       * @return {Promise<IcatEntity[]>}
       */
			'array, string, string': function(facilityNames, target, text){
				return this.search(facilityNames, {target: target, text: text}, {});
			}
		});
  
    /**
     * Returns an object that represents an Icat.
     *
     * This is a convenience method for <code>tc.facility(name).icat()</code>.
     * 
     * @method
     * @name  tc#icat
     * @param  {string} facilityName the name of the facility
     * @return {Icat}
     */
		this.icat = function(facilityName){ return this.facility(facilityName).icat(); };

    /**
     * Returns an object that represents an IDS.
     *
     * This is a convenience method for <code>tc.facility(name).ids()</code>.
     * 
     * @method
     * @name  tc#ids 
     * @param  {string} facilityName the name of the facility
     * @return {IDS}
     */
		this.ids = function(facilityName){ return this.facility(facilityName).ids(); };

    /**
     * Returns an object that represents a user with admin access.
     *
     * This is a convenience method for <code>tc.facility(name).admin()</code>.
     * 
     * @method
     * @name  tc#admin
     * @param  {string} facilityName the name of the facility
     * @return {Admin}
     */
		this.admin = function(facilityName){ return this.facility(facilityName).admin(); };

    /**
     * Returns an object that represents a general topcat user.
     *
     * This is a convenience method for <code>tc.facility(name).admin()</code>.
     * 
     * @method
     * @name  tc#user
     * @param  {string} facilityName the name of the facility
     * @return {User}
     */
		this.user = function(facilityName){ return this.facility(facilityName).user(); };

    /**
     * Returns an object that represents a smartclient.
     *
     * This is a convenience method for <code>tc.facility(name).admin()</code>.
     * 
     * @method
     * @name  tc#smartclient
     * @param  {string} facilityName the name of the facility
     * @return {Smartclient}
     */
    this.smartclient = function(facilityName){ return this.facility(facilityName).smartclient(); };

    /**
     * Returns a list of all the facilies where the user has admin access.
     *
     * @method
     * @name  tc#adminFacilities
     * @return {Facility[]}
     */
		this.adminFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().isAdmin; });
		};

    /**
     * Returns a list of all the facilies where the user is logged in.
     *
     * @method
     * @name  tc#userFacilities
     * @return {Facility[]}
     */
		this.userFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId !== undefined; });
		};

    /**
     * Returns a list of all the facilies where the user is <em>not</em> logged in.
     *
     * @method
     * @name  tc#nonUserFacilities
     * @return {Facility[]}
     */
		this.nonUserFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId === undefined; });
		};

    /**
     * Removes any non valid sessions.
     *
     * @method
     * @name  tc#purgeSessions
     * @return {Promise} resolves when purging is complete
     */
		this.purgeSessions = function(){
    	var promises = [];

    	_.each(this.userFacilities(), function(facility){
    		var icat = facility.icat();
    		promises.push(icat.get('session/' + icat.session().sessionId).then(function(){}, function(response){
    			if(response.code == "SESSION"){
    				return icat.logout();
    			}
    		}));
    	});

    	return $q.all(promises);
    };



    this.getConfVar = helpers.overload({
        /**
         * Gets a json object stored on the Topcat server.
         *
         * @method
         * @name  tc#getConfVar
         * @param {string} name the name of the json object
         * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
         * @return {Promise<object>} resolves when purging is complete
         */
        'string, object': function(name, options){
            return this.get('confVars/' + name, {}, options).then(function(data){
                try {
                    return JSON.parse(data.value);
                } catch(e){
                    return {};
                }
            });
        },
         /**
         * Gets a json object stored on the Topcat server.
         *
         * @method
         * @name  tc#getConfVar
         * @param {string} name the name of the json object
         * @param {Promise} timeout will cancel request if resolved
         * @return {Promise<object>} resolves when purging is complete
         */
        'string, promise': function(name, timeout){
            return this.getConfVar(name, {timeout: timeout});
        },
        /**
         * Gets a json object stored on the Topcat server.
         *
         * @method
         * @name  tc#getConfVar
         * @param {string} name the name of the json object
         * @return {Promise<object>} resolves when purging is complete
         */
        'string': function(name){
            return this.getConfVar(name, {});
        }
    });

		helpers.generateRestMethods(this, this.config().topcatUrl + "/topcat/");

    helpers.mixinPluginMethods('tc', this);

  });

})();
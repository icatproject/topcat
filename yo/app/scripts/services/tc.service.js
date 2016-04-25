
(function() {
  'use strict';

  var app = angular.module('angularApp');

  app.service('tc', function($sessionStorage, $q, $state, $timeout, $rootScope, helpers, tcFacility, tcIcatEntity, tcCache, APP_CONFIG){
  	var tc = this;
  	window.tc = this;
  	var facilities = {};
    var cache;

  	this.facility = function(facilityName){
  		if(!facilities[facilityName]) facilities[facilityName] = tcFacility.create(tc, facilityName);
  		return facilities[facilityName];
  	};

  	this.facilities = function(){
  		return _.map(APP_CONFIG.facilities, function(facility, facilityName){ return tc.facility(facilityName); });
  	};

  	this.config = function(){ 
      var out = APP_CONFIG.site;
      if(!out.topcatUrl) out.topcatUrl = "https://" + window.location.host;
      return out;
    }

    this.cache = function(){
      if(!cache) cache = tcCache.create('topcat');
      return cache;
    };

  	this.version = function(){
			var out = $q.defer();
			this.get('version').then(function(data){
				out.resolve(data.value);
			}, function(){ out.reject(); });
			return out.promise;
    };

		this.search = helpers.overload({
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
            if(data.length > 0){
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
			'array, promise, object': function(facilityNames, timeout, query){
				return this.search(facilityNames, query, {timeout: timeout});
			},
			'array, object': function(facilityNames, query){
				return this.search(facilityNames, query, {});
			},
			'array, string, string': function(facilityNames, target, text){
				return this.search(facilityNames, {target: target, text: text}, {});
			}
		});

		this.icat = function(facilityName){ return this.facility(facilityName).icat(); };

		this.ids = function(facilityName){ return this.facility(facilityName).ids(); };

		this.admin = function(facilityName){ return this.facility(facilityName).admin(); };

		this.user = function(facilityName){ return this.facility(facilityName).user(); };

		this.adminFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().isAdmin; });
		};

		this.userFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId !== undefined; });
		};

		this.nonUserFacilities = function(){
			return _.select(this.facilities(), function(facility){ return facility.icat().session().sessionId === undefined; });
		};

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

		helpers.generateRestMethods(this, this.config().topcatUrl + "/topcat/");

  });

})();
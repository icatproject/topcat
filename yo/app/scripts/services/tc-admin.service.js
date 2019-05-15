

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.service('tcAdmin', function(helpers){

    	this.create = function(facility){
    		return new Admin(facility);
    	};

        /**
         * @interface Admin
         */
        function Admin(facility){
            var that = this;

            this.facility = function(){
                return facility;
            };

            this.isValidSession = helpers.overload({
                /**
                 * Returns whether or not the user's session has admin priviliges.
                 *
                 * @method
                 * @name  Admin#isValidSession
                 * @param {string} sessionId the session id to be tested
                 * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<boolean>}
                 */
                'string, object': function(sessionId, options){
                    return this.get('isValidSession', {
                        facilityName: facility.config().name,
                        sessionId: sessionId
                    }, options);
                },

                /**
                 * Returns whether or not the user's session has admin priviliges.
                 *
                 * @method
                 * @name  Admin#isValidSession
                 * @param {string} sessionId the session id to be tested
                 * @param {Promise} timeout if resolved will cancel the request
                 * @return {Promise<boolean>}
                 */
                'string, promise': function(sessionId, timeout){
                    return this.isValidSession(sessionId, {timeout: timeout});
                },

                /**
                 * Returns whether or not the user's session has admin priviliges.
                 *
                 * @method
                 * @name  Admin#isValidSession
                 * @param {string} sessionId the session id to be tested
                 * @return {Promise<boolean>}
                 */
                'string': function(sessionId){
                    return this.isValidSession(sessionId, {});
                }
            });

            this.downloads = helpers.overload({
                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'array, object': function(queryOffset, options){
                    queryOffset = helpers.buildQuery(queryOffset);
                    queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().name) + (queryOffset ? " AND " + queryOffset.replace(/^\s*where\s*/, '') : "");

                    return this.get('downloads', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        queryOffset: queryOffset
                    }, options).then(function(downloads){
                        _.each(downloads, function(download){

                            download.delete = helpers.overload({
                                'object': function(options){
                                    return that.deleteDownload(this.id, options);
                                },
                                'promise': function(timeout){
                                    return this.delete({timeout: timeout});
                                },
                                '': function(){
                                    return this.delete({});
                                }
                            });

                            download.restore = helpers.overload({
                                'object': function(options){
                                    return that.restoreDownload(this.idd, options);
                                },
                                'promise': function(timeout){
                                    return this.restore(this.id, {timeout: timeout});
                                },
                                '': function(){
                                    return this.restore(this.id, {});
                                }
                            });

                        });

                        return downloads;
                    });
                },

                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise, array': function(timeout, queryOffset){
                    return this.downloads(queryOffset, {timeout: timeout});
                },

                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param  {array} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'array': function(queryOffset){
                    return this.downloads(queryOffset, {});
                },

                 /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @param  {string} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise, string': function(timeout, queryOffset){
                    return this.downloads([queryOffset], {timeout: timeout});
                },

                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param  {string} queryOffset any JPQL from the where clause onwards
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'string': function(queryOffset){
                    return this.downloads([queryOffset]);
                },

                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @param {Promise} timeout if resolved will cancel the request
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                'promise': function(timeout){
                    return this.downloads(params, {timeout: timeout});
                },

                /**
                 * Returns all downloads. 
                 *
                 * @method
                 * @name  Admin#downloads
                 * @return {Promise<object[]>} a deferred list of downloads
                 */
                '': function(){
                    return this.downloads({}, {});
                }
            });

            this.deleteDownload = helpers.overload({
                /**
                 * Soft deletes a download.
                 *
                 * @method
                 * @name Admin#deleteDownload
                 * @param  {string|number} id the id of the download
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        value: 'true'
                    }, options);
                },

                /**
                 * Soft deletes a download.
                 *
                 * @method
                 * @name Admin#deleteDownload
                 * @param  {string|number} id the id of the download
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise}
                 */
                'string, promise': function(id, timeout){
                    return this.deleteDownload(id, {timeout: timeout});
                },

                /**
                 * Soft deletes a download.
                 *
                 * @method
                 * @name Admin#deleteDownload
                 * @param  {string|number} id the id of the download
                 * @return {Promise}
                 */
                'string': function(id){
                    return this.deleteDownload(id, {});
                },
                'number, object': function(id, options){
                    return this.deleteDownload("" + id, options);
                },
                'number, promise': function(id, timeout){
                    return this.deleteDownload("" + id, {timeout: timeout});
                },
                'number': function(id){
                    return this.deleteDownload("" + id, {});
                }
            });

            this.restoreDownload = helpers.overload({
                 /**
                 * Restores a (soft deleted) download.
                 *
                 * @method
                 * @name Admin#restoreDownload
                 * @param  {string|number} id the id of the download
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        value: 'false'
                    }, options);
                },

                /**
                 * Restores a (soft deleted) download.
                 *
                 * @method
                 * @name Admin#restoreDownload
                 * @param  {string|number} id the id of the download
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise}
                 */
                'string, promise': function(id, timeout){
                    return this.restoreDownload(id, {timeout: timeout});
                },

                /**
                 * Restores a (soft deleted) download.
                 *
                 * @method
                 * @name Admin#restoreDownload
                 * @param  {string|number} id the id of the download
                 * @return {Promise}
                 */
                'string': function(id){
                    return this.restoreDownload(id, {});
                },
                'number, object': function(id, options){
                    return this.restoreDownload("" + id, options);
                },
                'number, promise': function(id, timeout){
                    return this.restoreDownload("" + id, {timeout: timeout});
                },
                'number': function(id){
                    return this.restoreDownload("" + id, {});
                }
            });

            this.setDownloadStatus = helpers.overload({
                /**
                 * Sets the status of a download.
                 *
                 * @method
                 * @name Admin#setDownloadStatus 
                 * @param  {string|number} id the id of the download
                 * @sparam {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, string, object': function(id, status, options){
                    return this.put('download/' + id + '/status', {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        value: status
                    }, options);
                },

                /**
                 * Sets the status of a download.
                 *
                 * @method
                 * @name Admin#setDownloadStatus 
                 * @param  {string|number} id the id of the download
                 * @param {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise}
                 */
                'string, string, promise': function(id, status, timeout){
                    return this.setDownloadStatus(id, status, {timeout: timeout});
                },

                /**
                 * Sets the status of a download.
                 *
                 * @method
                 * @name Admin#setDownloadStatus 
                 * @param  {string|number} id the id of the download
                 * @param {string} status can be 'ONLINE', 'ARCHIVE' or 'RESTORING'
                 * @return {Promise}
                 */
                'string, string': function(id, status){
                    return this.setDownloadStatus(id, status, {});
                },
                'number, string, object': function(id, status, options){
                    return this.setDownloadStatus("" + id, status, options);
                },
                'number, string, promise': function(id, status, timeout){
                    return this.setDownloadStatus("" + id, status, {timeout: timeout});
                },
                'number, string': function(id, status){
                    return this.setDownloadStatus("" + id, status, {});
                }
            });

            this.setDownloadTypeStatus = helpers.overload({
            	/**
            	 * @method
            	 * @name User#getDownloadTypeStatus
            	 * @param {string} downloadTypeName
            	 * @param {boolean} disabled - true to disable, false to enable
            	 * @param {string} message - displayed to user if they try to use this download type
            	 * @param {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
            	 * @return {Promise<object>} disabled flag and string message
            	 */
            	'string,boolean,string,object' : function(downloadType,disabled,message,options){
            		return this.put('downloadType/' + downloadType + '/status', {
                        facilityName: facility.config().name,
            			sessionId: facility.icat().session().sessionId,
            			disabled: disabled,
            			message: message
            		}, options).then(function(response){
            			return response;
            		}, function(response){
            			// error case - log but treat as not disabled
            			var msg = '(no message)';
            			if( response && response.message ) msg = response.msg
            			console.log('user.setDownloadTypeStatus failed - treat as not disabled: ' + msg);
            			return response;
            		})
            	},
            	/**
            	 * @method
            	 * @name User#getDownloadTypeStatus
            	 * @param {string} downloadTypeName
                 * @param  {Promise} timeout if resolved will cancel the request
            	 * @return {Promise<object>} disabled flag and string message
            	 */
            	'string,boolean,string,promise' : function(downloadType,disabled,message,timeout){
            		return this.setDownloadTypeStatus(downloadType,disabled,message,{timeout: timeout});
            	},
            	/**
            	 * @method
            	 * @name User#getDownloadTypeStatus
            	 * @param {string} downloadTypeName
            	 * @return {Promise<object>} disabled flag and string message
            	 */
            	'string,boolean,string': function(downloadType,disabled,message){
            		return this.setDownloadTypeStatus(downloadType,disabled,message,{});
            	}
            });

            this.setConfVar = helpers.overload({
                /**
                 * Stores a value on Topcat.
                 *
                 * @method
                 * @name Admin#setConfVar
                 * @param  {string} name the name of the value
                 * @param {object} value the value to be stored
                 * @param  {object} options {@link https://docs.angularjs.org/api/ng/service/$http#usage|as specified in the Angular documentation}
                 * @return {Promise}
                 */
                'string, object, object': function(name, value, options){
                    return this.put('confVars/' + name, {
                        facilityName: facility.config().name,
                        sessionId: facility.icat().session().sessionId,
                        value: JSON.stringify(value)
                    }, options);
                },

                /**
                 * Stores a value on Topcat.
                 *
                 * @method
                 * @name Admin#setConfVar
                 * @param  {string} name the name of the value
                 * @param {object} value the value to be stored
                 * @param  {Promise} timeout if resolved will cancel the request
                 * @return {Promise}
                 */
                'string, object, promise': function(name, value, timeout){
                    return this.setConfVar(name, value, {timeout: timeout});
                },

                /**
                 * Stores a value on Topcat.
                 *
                 * @method
                 * @name Admin#setConfVar
                 * @param  {string} name the name of the value
                 * @param {object} value the value to be stored
                 * @return {Promise}
                 */
                'string, object': function(name, value){
                    return this.setConfVar(name, value, {});
                }
            });

            helpers.generateRestMethods(this, facility.tc().config().topcatUrl + "/topcat/admin/");

            helpers.mixinPluginMethods('admin', this);
        }

	});

})();

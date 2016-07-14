

(function() {
    'use strict';

    var app = angular.module('angularApp');

    app.service('tcAdmin', function(helpers){

    	this.create = function(facility){
    		return new Admin(facility);
    	};

        function Admin(facility){
            var that = this;

            this.facility = function(){
                return facility;
            };

            this.isValidSession = helpers.overload({
                'string, object': function(sessionId, options){
                    return this.get('isValidSession', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: sessionId
                    });
                },
                'string, promise': function(sessionId, timeout){
                    return this.isValidSession(sessionId, {timeout: timeout});
                },
                'string': function(sessionId){
                    return this.isValidSession(sessionId, {});
                },
                'promise': function(timeout){
                    return this.isValidSession(facility.icat().session().sessionId, {timeout: timeout});
                },
                '': function(){
                    return this.isValidSession(facility.icat().session().sessionId, {});
                }
            });

            this.downloads = helpers.overload({
                'object, object': function(params, options){
                    params.queryOffset = "where download.facilityName = " + helpers.jpqlSanitize(facility.config().name) + (params.queryOffset ? " AND " + params.queryOffset.replace(/^\s*where\s*/, '') : "");

                    return this.get('downloads', _.merge({
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId
                    }, params), options).then(function(downloads){
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

                            download.getSize = helpers.overload({
                                'object': function(options){
                                    var that = this;

                                    var investigationIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'investigation'}), function(item){ return item.entityId});
                                    var datasetIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'dataset'}), function(item){ return item.entityId});
                                    var datafileIds = _.map(_.select(this.downloadItems, function(item){ return item.entityType == 'datafile'}), function(item){ return item.entityId});

                                    return facility.ids().getSize(investigationIds, datasetIds, datafileIds, options).then(function(size){
                                        that.size = size;
                                        return size;
                                    });
                                },
                                'promise': function(timeout){
                                    return this.getSize({timeout: timeout});
                                },
                                '': function(){
                                    return this.getSize({});
                                }
                            });


                        });

                        return downloads;
                    });
                },
                'promise, array': function(timeout, queryOffset){
                    return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {timeout: timeout});
                },
                'array': function(queryOffset){
                    return this.downloads({queryOffset: helpers.buildQuery(queryOffset)}, {});
                },
                'promise, string': function(timeout, queryOffset){
                    return this.downloads([queryOffset], {timeout: timeout});
                },
                'string': function(queryOffset){
                    return this.downloads([queryOffset]);
                },
                'promise': function(timeout){
                    return this.downloads(params, {timeout: timeout});
                },
                '': function(){
                    return this.downloads({}, {});
                }
            });

            this.deleteDownload = helpers.overload({
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        value: 'true'
                    }, options);
                },
                'string, promise': function(id, timeout){
                    return this.deleteDownload(id, {timeout: timeout});
                },
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
                'string, object': function(id, options){
                    return this.put('download/' + id + '/isDeleted', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        value: 'false'
                    }, options);
                },
                'string, promise': function(id, timeout){
                    return this.restoreDownload(id, {timeout: timeout});
                },
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
                'string, string, object': function(id, status, options){
                    return this.put('download/' + id + '/status', {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        value: status
                    }, options);
                },
                'string, string, promise': function(id, status, timeout){
                    return this.setDownloadStatus(id, status, {timeout: timeout});
                },
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

            this.setConfVar = helpers.overload({
                'string, string, object': function(name, value, options){
                    return this.put('confVars/' + name, {
                        icatUrl: facility.config().icatUrl,
                        sessionId: facility.icat().session().sessionId,
                        value: JSON.stringify(value)
                    }, options);
                },
                'string, string, promise': function(name, value, timeout){
                    return this.setConfVar(name, value, {timeout: timeout});
                },
                'string, string': function(name, value){
                    return this.setConfVar(name, value, {});
                }
            });

            this.getConfVar = helpers.overload({
                'string, object': function(name, value, options){
                    return facility.user().getConfVar(name, value, options);
                },
                'string, promise': function(name, value, timeout){
                    return this.getConfVar(name, value, {timeout: timeout});
                },
                'string': function(name, value){
                    return this.getConfVar(name, value);
                }
            });

            helpers.generateRestMethods(this, facility.tc().config().topcatUrl + "/topcat/admin/");
        }

	});

})();

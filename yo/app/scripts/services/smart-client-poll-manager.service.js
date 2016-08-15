(function() {
    'use strict';

    angular
        .module('topcat')
        .service('SmartClientPollManager', SmartClientPollManager);

    SmartClientPollManager.$inject = ['APP_CONFIG', 'SmartClientManager', '$sessionStorage', 'poller', 'inform', 'tc'];

    function SmartClientPollManager(APP_CONFIG, SmartClientManager, $sessionStorage,  poller, inform, tc) {
        var self = this;

        this.createPoller = function(facility, userName, preparedId) {
            var preparedIds = [];
            preparedIds.push(preparedId);

            var smartClientPoller = poller.get('https://localhost:8888/isReady', {
                delay: 20000,
                argumentsArray: [
                    {
                        //method: 'get',
                        params: {
                            json: JSON.stringify({
                                idsUrl: facility.idsUrl,
                                preparedIds : preparedIds
                            })
                        },
                        info: {
                            facilityTitle : 'SmartClient'
                        }
                    }
                ]
            });

            smartClientPoller.promise.then(null, null, function(result) {
                if (result.status === 200) {
                    _.each(result.data, function(data) {
                        if (data.toGet === 0) {
                            /*
                            TopcatManager.completeDownloadByPreparedId(facility, userName, preparedId).then(function(completeData) {
                                if (typeof completeData.value !== 'undefined' && completeData.value === preparedId) {
                                    smartClientPoller.stop();
                                    smartClientPoller.remove();
                                }
                            });
                            */
                        }
                    });
                }
            });
        };

        this.runOnStartUp = function() {
            _.each($sessionStorage.sessions, function(session, key) {
                var facility = null;
                try {
                    facility = tc.facility(key).config();
                } catch (error){

                }

                //check smartclient is online
                if (facility !== null) {
                    SmartClientManager.ping().then(function(pingData){
                        if (pingData.ping === 'online') {
                            //login to the smartclient
                            SmartClientManager.connect(session.sessionId, facility).then(function() {
                                //get list of smartclient downloads that has restoring status
                                TopcatManager.getMyRestoringSmartClientDownloads(facility, session.userName).then(function(data) {
                                    _.each(data, function(smartClientDownload) {
                                        //create a poller for each preparedId
                                        self.createPoller(facility, session.userName, smartClientDownload.preparedId);
                                    });
                                });
                            }, function(error) { //jshint ignore: line
                                inform.add(error, {
                                    'ttl': 0,
                                    'type': 'danger'
                                });
                            });
                        }
                    });
                }
            });
        };
    }
})();
var express = require('express');
var router = express.Router();
var request = require('request');
var async = require('async');
var _ = require('lodash');

/**
 * Version route
 */
router.get('/version', function(req, res, next) {
    request(req.query.server + '/icat/version', function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.end(body);
        } else {
            res.end("Failed");
        }
    });
});

/**
 * Session route
 */
router.post('/session', function(req, res, next) {
    var data = {
        plugin: req.body.plugin,
        credentials: [
            {username: req.body.username},
            {password: req.body.password}
        ]
    };

    request(
        {
            url: req.body.server + '/icat/session',
            method: 'POST',
            form: {
                json: JSON.stringify(data)
            }
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.end(body);
            } else {
                res.json(body);
            }
        }
    );
});

/**
 * Entities route
 */
router.get('/entityManager', function(req, res, next) {
    if (_.isUndefined(req.query.countQuery)) {
        request(
            {
                url: req.query.server + '/icat/entityManager',
                method: 'GET',
                qs: {
                    sessionId: req.query.sessionId,
                    query : req.query.query
                },
                json: true
            }, function (error, response, body) {
                if (!error && response.statusCode == 200) {
                    if (! _.isUndefined(req.query.entity)) {
                        body = _.pluck(body, req.query.entity);
                    }
                    res.json(body);
                } else {
                    res.json(body);
                }
            }
        );

    }  else {

        //use async to make calls in parallel to get result and total
        //count and combine them. See https://github.com/caolan/async#parallel
        async.parallel([
            function(callback) {
                //query count
                request(
                    {
                        url: req.query.server + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: req.query.sessionId,
                            query : req.query.countQuery
                        },
                        json: true
                    }, function (error, response, body) {
                        if (!error && response.statusCode == 200) {
                            callback(null, body);
                        } else {
                            callback(body);
                        }
                    }
                );
            },
            function(callback) {
                //query for entities
                request(
                    {
                        url: req.query.server + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: req.query.sessionId,
                            query : req.query.query
                        },
                        json: true
                    }, function (error, response, body) {
                        if (!error && response.statusCode == 200) {
                            //strip entity wrapper
                            if (! _.isUndefined(req.query.entity)) {
                                body = _.pluck(body, req.query.entity);
                            }

                            callback(null, body);
                        } else {
                            callback(body);
                            /*if (response.statusCode == 403){
                                callback({name: 'session', message: 'Session invalid'});
                            }

                            if (response.statusCode == 404){
                                callback({name: 'notfound', message: 'Not found'});
                            }

                            if (response.statusCode == 500){
                                if (body !== null) {
                                    //icat response of 500 may be a 404
                                    if(body.message.indexOf('404 Not Found') !== -1) {
                                        callback({name: 'notfound', message: 'Not found'});
                                    } else {
                                        callback({name: 'Internal', message: 'Internal server error'});
                                    }
                                }

                            }*/

                        }
                    }
                );

            }

        ],
        function(err, results){
            if (err) {
                res.json(err);
                res.end();
            } else {
                var total;
                var records;
                if (! _.isUndefined(results[0])) {
                    total = results[0][0];
                }

                if (! _.isUndefined(results[1])) {
                    records = results[1];
                }

                var data = {
                    page: req.query.page,
                    recordsTotal : total,
                    recordsFiltered : total,
                    data : records
                };

                res.json(data);
                res.end();
            }
        });
    }
});

module.exports = router;

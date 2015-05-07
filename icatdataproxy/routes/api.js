var express = require('express');
var router = express.Router();
var request = require('request');
var async = require('async');
var _ = require('lodash');

/**
 * Version route
 */
router.get('/version', function(req, res, next) {
    request(decodeURIComponent(req.query.server) + '/icat/version', function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.end(body);
        } else {
            if (_.isUndefined(response)) {
                    res.status(0).json(body);
            } else {
                res.status(response.statusCode).end("Failed");
            }
        }
    });
});

/**
 * login route
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
            url: decodeURIComponent(req.body.server) + '/icat/session',
            method: 'POST',
            form: {
                json: JSON.stringify(data)
            }
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.end(body);
            } else {
                if (_.isUndefined(response)) {
                    res.status(0).json(body);
                } else {
                    res.status(response.statusCode).json(body);
                }
            }
        }
    );
});

/**
 * Get session info route
 */
router.get('/session/:sessionId', function(req, res, next) {
    request(
        {
            url: decodeURIComponent(req.query.server) + '/icat/session/' + req.params.sessionId,
            method: 'GET'
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.end(body);
            } else {
                if (_.isUndefined(response)) {
                    res.status(0).json(body);
                } else {
                    res.status(response.statusCode).json(body);
                }
            }
        }
    );
});


/**
 * Refresh session route
 */
router.put('/session/:sessionId', function(req, res, next) {
    request(
        {
            url: decodeURIComponent(req.query.server) + '/icat/session/' + req.params.sessionId,
            method: 'PUT'
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.end();
            } else {
                if (_.isUndefined(response)) {
                    res.status(0).json(body);
                } else {
                    res.status(response.statusCode).json(body);
                }
            }
        }
    );
});


/**
 * logout route
 */
router.delete('/session/:sessionId', function(req, res, next) {
    request(
        {
            url: decodeURIComponent(req.query.server) + '/icat/session/' + req.params.sessionId,
            method: 'DELETE'
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.end();
            } else {
                if (_.isUndefined(response)) {
                    res.status(0).json(body);
                } else {
                    res.status(response.statusCode).json(body);
                }
            }
        }
    );
});


/**
 * Entities route
 */
router.get('/entityManager', function(req, res, next) {
    if (typeof req.query.query !== 'undefined' && typeof req.query.countQuery === 'undefined' && typeof req.query.filterCountQuery === 'undefined') {
        var query = req.query.query;
        console.log('query:', decodeURIComponent(req.query.query));

        request(
            {
                url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                method: 'GET',
                qs: {
                    sessionId: decodeURIComponent(req.query.sessionId),
                    query : decodeURIComponent(query)
                },
                json: true
            }, function (error, response, body) {
                if (!error && response.statusCode == 200) {
                    if (! _.isUndefined(req.query.entity)) {
                        body = _.pluck(body, req.query.entity);
                    }
                    res.json(body);
                } else {
                    if (_.isUndefined(response)) {
                        res.status(0).json(body);
                    } else {
                        res.status(response.statusCode).json(body);
                    }
                }
            }
        );

    }


    if (typeof req.query.query !== 'undefined' && typeof req.query.countQuery !== 'undefined' && typeof req.query.filterCountQuery === 'undefined') {
        console.log('countQuery y:', decodeURIComponent(req.query.countQuery));
        //use async to make calls in parallel to get result and total
        //count and combine them. See https://github.com/caolan/async#parallel
        async.parallel([
            function(callback) {
                //query count
                request(
                    {
                        url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: decodeURIComponent(req.query.sessionId),
                            query : decodeURIComponent(req.query.countQuery)
                        },
                        json: true
                    }, function (error, response, body) {
                        if (!error && response.statusCode == 200) {
                            callback(null, body);
                        } else {
                            callback(response, body);
                        }
                    }
                );
            },
            function(callback) {
                //query for entities
                request(
                    {
                        url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: req.query.sessionId,
                            query : decodeURIComponent(req.query.query)
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
                            callback(response, body);
                        }
                    }
                );

            }

        ],
        function(err, results){
            if (err) {
                res.status(err.statusCode).json(results);
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
                    data : records
                };

                res.json(data);
                res.end();
            }
        });
    }


    if (typeof req.query.query !== 'undefined' && typeof req.query.countQuery !== 'undefined' && typeof req.query.filterCountQuery !== 'undefined') {
        console.log('countQuery x:', decodeURIComponent(req.query.countQuery));
        console.log('filterCountQuery x:', decodeURIComponent(req.query.filterCountQuery));
        //use async to make calls in parallel to get result and total
        //count and combine them. See https://github.com/caolan/async#parallel
        async.parallel([
            function(callback) {
                //query count
                request(
                    {
                        url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: decodeURIComponent(req.query.sessionId),
                            query : decodeURIComponent(req.query.countQuery)
                        },
                        json: true
                    }, function (error, response, body) {
                        if (!error && response.statusCode == 200) {
                            callback(null, body);
                        } else {
                            callback(response, body);
                        }
                    }
                );
            },
            function(callback) {
                //query for filter count
                request(
                    {
                        url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: req.query.sessionId,
                            query : decodeURIComponent(req.query.filterCountQuery)
                        },
                        json: true
                    }, function (error, response, body) {
                        if (!error && response.statusCode == 200) {

                            callback(null, body);
                        } else {
                            callback(response, body);
                        }
                    }
                );
            },
            function(callback) {
                //query for entities
                request(
                    {
                        url: decodeURIComponent(req.query.server) + '/icat/entityManager',
                        method: 'GET',
                        qs: {
                            sessionId: req.query.sessionId,
                            query : decodeURIComponent(req.query.query)
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
                            callback(response, body);
                        }
                    }
                );

            }

        ],
        function(err, results){
            if (err) {
                res.status(err.statusCode).json(results);
                res.end();
            } else {
                var total;
                var records;
                var filterTotal;
                if (! _.isUndefined(results[0])) {
                    total = results[0][0];
                }

                if (! _.isUndefined(results[1])) {
                    filterTotal = results[1][0];
                }

                if (! _.isUndefined(results[2])) {
                    records = results[2];
                }

                var data = {
                    page: req.query.page,
                    recordsTotal : total,
                    recordsFiltered : filterTotal,
                    data : records
                };

                res.json(data);
                res.end();
            }
        });
    }


});

module.exports = router;

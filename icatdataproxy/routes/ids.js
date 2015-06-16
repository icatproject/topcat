var express = require('express');
var router = express.Router();
var request = require('request');
var async = require('async');
var _ = require('lodash');

/**
 * Version route
 */


/**
 * getSize route
 */
router.get('/getSize', function(req, res, next) {
    var qs = {};

    qs.sessionId = decodeURIComponent(req.query.sessionId);

    if (typeof req.query.investigationIds !== 'undefined') {
        qs.investigationIds = req.query.investigationIds;
    }

    if (typeof req.query.datasetIds !== 'undefined') {
        qs.datasetIds = req.query.datasetIds;
    }

    if (typeof req.query.datafileIds !== 'undefined') {
        qs.datafileIds = req.query.datafileIds;
    }

    var r = request(
        {
            url: decodeURIComponent(req.query.server) + '/ids/getSize',
            method: 'GET',
            qs: qs
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
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
});

/**
 * getSize route
 */
router.get('/getStatus', function(req, res, next) {
    var qs = {};

    qs.sessionId = decodeURIComponent(req.query.sessionId);

    if (typeof req.query.investigationIds !== 'undefined') {
        qs.investigationIds = req.query.investigationIds;
    }

    if (typeof req.query.datasetIds !== 'undefined') {
        qs.datasetIds = req.query.datasetIds;
    }

    if (typeof req.query.datafileIds !== 'undefined') {
        qs.datafileIds = req.query.datafileIds;
    }

    var r = request(
        {
            url: decodeURIComponent(req.query.server) + '/ids/getStatus',
            method: 'GET',
            qs: qs
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
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
});


module.exports = router;

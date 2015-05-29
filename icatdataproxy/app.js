var express = require('express');
var fs = require('fs');
var https = require('https');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var cors = require('cors');

var routes = require('./routes/index');
var api = require('./routes/api');

var app = express();

var env = process.env.NODE_ENV || 'development';

var key = (env === 'production') ? fs.readFileSync('certs/key.crt') : fs.readFileSync('certs/key.pem');
var cert = (env === 'production') ? fs.readFileSync('certs/cert.crt') : fs.readFileSync('certs/cert.pem');
var ca = (env === 'production') ? [fs.readFileSync('certs/root.crt'), fs.readFileSync('certs/intermediate.crt')] : false;

//ignore invalid ssl certificate
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

https.createServer({
      key: key,
      cert: cert,
      ca: ca
}, app).listen(3001);

app.use(cors());

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);
app.use('/icat', api);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


module.exports = app;

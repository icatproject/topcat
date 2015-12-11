// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2015-04-01 using
// generator-karma 0.9.0

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      'bower_components/es5-shim/es5-shim.js',
      'bower_components/jquery/dist/jquery.js',
      'bower_components/angular/angular.js',
      'bower_components/json3/lib/json3.js',
      'bower_components/bootstrap/dist/js/bootstrap.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/angular-deferred-bootstrap/angular-deferred-bootstrap.js',
      'bower_components/angularjs-truncate/src/truncate.js',
      'bower_components/ui-router-extras/release/ct-ui-router-extras.js',
      'bower_components/angular-inform/dist/angular-inform.js',
      'bower_components/lodash/lodash.js',
      'bower_components/pretty-bytes/pretty-bytes.js',
      'bower_components/angular-pretty-bytes/angular-pretty-bytes.js',
      'bower_components/ngstorage/ngStorage.js',
      'bower_components/angular-translate/angular-translate.js',
      'bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
      'bower_components/squel/squel.js',
      'bower_components/angular-ui-grid/ui-grid.js',
      'bower_components/spin.js/spin.js',
      'bower_components/angular-spinner/angular-spinner.js',
      'bower_components/re-tree/re-tree.js',
      'bower_components/ng-device-detector/ng-device-detector.js',
      'bower_components/moment/moment.js',
      'bower_components/angular-moment/angular-moment.js',
      'bower_components/angularpoller/angular-poller.min.js',
      'bower_components/angular-bind-html-compile/angular-bind-html-compile.js',
      'bower_components/angular-loading-bar/build/loading-bar.js',
      'bower_components/angular-cookie/angular-cookie.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/karma-read-json/karma-read-json.js',
      // endbower
      'app/scripts/**/*.js',
      'test/mock/**/*.js',
      'test/spec/controllers/**/*.js',
      'test/spec/models/**/*.js',
      'test/spec/services/**/*.js',

      // JSON fixture
      {
        pattern:  'app/data/*.json',
        watched:  true,
        served:   true,
        included: false
      },
      {
        pattern: 'test/mock/**/*.json',
        included: false
      }
    ],

    // list of files / patterns to exclude
    exclude: [
    ],

    proxies:  {
      'data/config': 'app/data/config'
    },

    // web server port
    port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      'PhantomJS'
    ],

    // Which plugins to enable
    plugins: [
      'karma-phantomjs-launcher',
      'karma-jasmine'
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    //logLevel: config.LOG_INFO,
    logLevel: config.LOG_DEBUG,

    // Uncomment the following lines if you are using grunt's server to run the tests
    // proxies: {
    //   '/': 'http://localhost:9000/'
    // },
    // URL root prevent conflicts with the site root
    // urlRoot: '_karma_'

    client : {
      captureConsole: true
    }
  });
};

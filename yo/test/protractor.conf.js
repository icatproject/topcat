exports.config = {
  // The address of a running selenium server.
  //seleniumAddress: 'http://localhost:4444/wd/hub',

  // Spec patterns are relative to the location of this config.
  specs: [
    'spec/e2e/*.js'
  ],

  // plugins: [{
  //   package: 'protractor-console',
  //   logLevels: ['debug']
  // }],


  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {'args': ['--headless', '--disable-gpu', '--disable-extensions',  '--start-maximized']}
  },

  directConnect: true,


  // A base URL for your application under test. Calls to protractor.get()
  // with relative paths will be prepended with this.
  baseUrl: 'http://localhost:8080',

  // Possible fix for arbitrary timeout failures?
  // See <https://github.com/angular/protractor/issues/2963>
  jasmineNodeOpts: {
  //   onComplete: null,
  //   isVerbose: true,
  //   showColors: true,
  //   includeStackTrace: true,
     defaultTimeoutInterval: 2500000
  }
};

'use strict';

describe('angularjs homepage', function() {
  it('should greet the named user', function() {
    browser.get('/');

    browser.waitForAngular();

    var userNameElement = element(by.model('loginController.userName'));
    var passwordElement = element(by.model('loginController.password'));
    var submitElement = element(by.id('login'));

    userNameElement.sendKeys('root');
    passwordElement.sendKeys('root');
    submitElement.click();

    //browser.waitForAngular();

    expect(browser.getCurrentUrl()).toEqual('http://localhost:8080/#/my-data/LILS');

  });
});

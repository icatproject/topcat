'use strict';

describe('user', function() {
  it('creates download', function() {
    browser.get('/');

    browser.waitForAngular();

    element(by.model('loginController.userName')).sendKeys('root');
    element(by.model('loginController.password')).sendKeys('root');
    element(by.id('login')).click();

    browser.waitForAngular();

    expect(browser.getCurrentUrl()).toEqual('http://localhost:8080/#/my-data/LILS');
    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(false);

    element(by.linkText('Proposal 1 - 5')).click();

    browser.waitForAngular();

    element(by.linkText('Dataset 1')).click();

    expect(browser.getCurrentUrl()).toEqual('http://localhost:8080/#/browse/facility/LILS/proposal/Proposal%201/investigation/5/dataset/13/datafile');

    element(by.className('ui-grid-icon-ok')).click();

    browser.waitForAngular();

    browser.sleep(5000);

    element(by.className('glyphicon-shopping-cart')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD_CART_BUTTON.TEXT"]')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD.MODAL.BUTTON.OK.TEXT"]')).click();

    browser.sleep(5000);

    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(true);
    
  });
});

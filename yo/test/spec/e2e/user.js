'use strict';

describe('user', function() {
  it('creates download and then deletes it', function() {
    browser.get('/');

    browser.waitForAngular();

    element(by.model('loginController.userName')).sendKeys('root');
    element(by.model('loginController.password')).sendKeys('root');
    element(by.id('login')).click();

    browser.waitForAngular();

    expect(browser.getCurrentUrl()).toEqual('http://localhost:8080/#/my-data/LILS');
    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(false);

    element(by.css('a[ng-click="grid.appScope.browse(row.entity)"]')).click();

    browser.waitForAngular();

    element(by.css('a[ng-click="grid.appScope.browse(row.entity)"]')).click();

    element(by.className('ui-grid-icon-ok')).click();

    browser.waitForAngular();

    //browser.sleep(5000);

    element(by.className('glyphicon-shopping-cart')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD_CART_BUTTON.TEXT"]')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD.MODAL.BUTTON.OK.TEXT"]')).click();

    //browser.sleep(5000);
    browser.waitForAngular();

    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(true);
    
    element(by.className('glyphicon-download-alt')).click();

    browser.sleep(1000);

    element(by.css('a[translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT"]')).click();

    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(false);
  });


});

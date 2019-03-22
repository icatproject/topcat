'use strict';

describe('user', function() {
  it('creates download and then deletes it', function() {

	  // Force a failure here, one way or another.  MIGHT get more info out of Travis then...
	  // ... and add this comment just to trigger another Travis run - last one was masked by the webdriver-manager update failure again!
    // browser.get('http://localhost:8080/#/login');
    browser.get('http://localhost:8080/#/login').then(function(){
        expect(1312).toEqual(1066);
    }, function(){
        expect(2525).toEqual(3540);
    });

    var until = protractor.ExpectedConditions;
    browser.wait(until.presenceOf(element(by.css('.is-init'))));

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

    browser.wait(function(){
        return element(by.className('glyphicon-shopping-cart')).isPresent();
    }, 1000 * 10, ".glyphicon-shopping-cart not present");

    element(by.className('glyphicon-shopping-cart')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD_CART_BUTTON.TEXT"]')).click();

    browser.waitForAngular();

    element(by.css('button[translate="CART.DOWNLOAD.MODAL.BUTTON.OK.TEXT"]')).click();

    browser.wait(function(){
        return element(by.className('glyphicon-download-alt')).isPresent();
    }, 1000 * 10, ".glyphicon-download-alt not present");

    element(by.className('glyphicon-download-alt')).click();

    browser.wait(function(){
        return element(by.css('a[translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT"]')).isPresent();
    });

    element(by.css('a[translate="DOWNLOAD.ACTIONS.LINK.REMOVE.TEXT"]')).click();

    expect(element(by.className('glyphicon-download-alt')).isPresent()).toEqual(false);
  });


});

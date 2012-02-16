/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.gwt.client.callback;

import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginInfoPanelUpdateEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This class implements the async callback for the Login Validation check
 * 
 * @author Mr. Srikanth Nagella
 */
public class LoginValidCallback implements AsyncCallback<Boolean> {

    private boolean autoLogin;
    private String facilityName;

    public LoginValidCallback(String facilityName, boolean autoLogin) {
        this.autoLogin = autoLogin;
        this.facilityName = facilityName;
    }

    @Override
    public void onFailure(Throwable caught) {
    }

    @Override
    public void onSuccess(Boolean result) {
        if (result) {
            if (autoLogin) {
                EventPipeLine.getEventBus().fireEventFromSource(new LoginEvent(facilityName), facilityName);
            } else {
                EventPipeLine.getEventBus().fireEventFromSource(new LoginInfoPanelUpdateEvent(facilityName),
                        facilityName);
            }
        } else {
            EventPipeLine.getEventBus().fireEventFromSource(new LogoutEvent(facilityName), facilityName);
        }
    }

}

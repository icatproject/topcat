/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.gwt.client.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This class implements the async callback for the Login Validation check
 * 
 * @author Mr. Srikanth Nagella
 */
public class LoginValidCallback implements AsyncCallback<Boolean> {

    private String serverName;

    public LoginValidCallback(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void onFailure(Throwable caught) {
    }

    @Override
    public void onSuccess(Boolean result) {
        if (result) {
            EventPipeLine.getInstance().successLogin(serverName);
        } else {
            EventPipeLine.getInstance().updateLoginPanelStatus(serverName, result);
        }
    }

}

/**
 * 
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.client.authentication;

import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.widget.WaitDialog;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginAfterRedirect {

    public LoginAfterRedirect() {
    }

    public void login() {
        String facilityName = Window.Location.getParameter("facilityName");
        String authenticationType = Window.Location.getParameter("authenticationType");
        final String redirectUrl = Window.Location.getParameter("url");
        String icatSessionId = Window.Location.getParameter("ticket");
        
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        final String entrypoint = GWT.getHostPageBaseURL();
        
        //must have a facility
        if (facilityName == null) {
            MessageBox.alert("Error", "No Facility specified",
                    new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {                            
                            Window.Location.replace(entrypoint);
                        }
                    });
            return;
        }
        
        //must have a ticket
        if (icatSessionId == null) {
            MessageBox.alert("Error", "Unable to log on: No ticket specified",
                    new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {                            
                            Window.Location.replace(entrypoint);
                        }
                    });
            return;
        }
        
        if (authenticationType.equalsIgnoreCase("external redirect")) {
            urlBuilder.removeParameter("facilityName");
            urlBuilder.removeParameter("authenticationType");

            LoginServiceAsync loginService = LoginService.Util.getInstance();
            final WaitDialog waitDialog = new WaitDialog();
            waitDialog.setMessage(" Logging In...");
            waitDialog.show();
            loginService.login(icatSessionId, authenticationType, facilityName, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    waitDialog.hide();
                    
                    MessageBox.alert("Error", "Unable to log on: " + caught.getMessage(), new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            Window.Location.replace(entrypoint);
                        }
                    });
                }

                @Override
                public void onSuccess(Boolean result) {
                    waitDialog.hide();
                    if(result) {
                        //successful login                                                
                        Window.Location.replace(URL.decodeQueryString(redirectUrl));
                    } else {
                        //failed login
                        MessageBox.alert("Error", "Unable to log on: The ticket is invalid" , new Listener<MessageBoxEvent>() {
                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                Window.Location.replace(entrypoint);
                            }
                        });
                    }
                }
            });
        } else {
            MessageBox.alert("Error", "Unsupported authentication type: " + authenticationType,
                    new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            Window.Location.replace(entrypoint); 
                        }
                    });
            return;
        }
        
    }
    
}
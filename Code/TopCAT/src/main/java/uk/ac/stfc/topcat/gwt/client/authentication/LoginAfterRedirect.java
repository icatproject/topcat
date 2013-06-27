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

import java.util.HashMap;
import java.util.Map;

import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.widget.WaitDialog;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginAfterRedirect {

    public LoginAfterRedirect() {
    }

    public void login() {
        String facilityName = Window.Location.getParameter("facilityName");
        String authenticationType = Window.Location.getParameter("authenticationType");
        Map<String, String> parameters = new HashMap<String, String>();
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();

        if (authenticationType.equalsIgnoreCase("CAS")) {
            // remove the ticket from the url, it might be a parameter or in the
            // hash
            String ticket;
            if (Window.Location.getParameter("ticket") != null) {
                ticket = Window.Location.getParameter("ticket");
                urlBuilder.removeParameter("ticket");
            } else {
                String[] hash = Window.Location.getHash().split("\\&ticket=", 2);
                ticket = hash[1];
                // remove cas ticket which, if in the hash, will be at the end
                urlBuilder.setHash(hash[0]);
            }
            // Encode the url and add the ticket back in. This is to ensure the
            // url passed to icat matches the one passed into cas.
            String url = encodeUrlDelimiters(urlBuilder.buildString()) + "?ticket=" + ticket;
            parameters.put("ticket", url);
        } else {
            MessageBox.alert("Error", "Unsupported authentication type: " + authenticationType,
                    new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            urlBuilder.removeParameter("facilityName");
                            urlBuilder.removeParameter("authenticationType");
                            Window.Location.replace(urlBuilder.buildString());
                        }
                    });
            return;
        }

        urlBuilder.removeParameter("facilityName");
        urlBuilder.removeParameter("authenticationType");

        LoginServiceAsync loginService = LoginService.Util.getInstance();
        final WaitDialog waitDialog = new WaitDialog();
        waitDialog.setMessage(" Logging In...");
        waitDialog.show();
        loginService.login(parameters, authenticationType, facilityName, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                waitDialog.hide();
                MessageBox.alert("Error", "unable to log on : " + caught.getMessage(), new Listener<MessageBoxEvent>() {
                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Window.Location.replace(urlBuilder.buildString());
                    }
                });
            }

            @Override
            public void onSuccess(String result) {
                waitDialog.hide();
                Window.Location.replace(urlBuilder.buildString());
            }
        });
    }

    private String encodeUrlDelimiters(String s) {
        if (s == null) {
            return null;
        }
        s = s.replaceAll(";", "%2F");
        s = s.replaceAll("/", "%2F");
        s = s.replaceAll(":", "%3A");
        s = s.replaceAll("\\?", "%3F");
        s = s.replaceAll("&", "%26");
        s = s.replaceAll("\\=", "%3D");
        s = s.replaceAll("\\+", "%2B");
        s = s.replaceAll("\\$", "%24");
        s = s.replaceAll(",", "%2C");
        s = s.replaceAll("#", "%23");
        return s;
    }
}
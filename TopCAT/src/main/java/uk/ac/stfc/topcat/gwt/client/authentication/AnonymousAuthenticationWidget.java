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

/**
 * Imports
 */
import java.util.HashMap;
import java.util.Map;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * This is an authentication widget that uses the anonymous user name and
 * password.
 */
public class AnonymousAuthenticationWidget extends Composite {
    private AuthenticationModel authenticationModel;
    private TFacility facility = null;
    private LoginInterface loginHandler = null;

    public AnonymousAuthenticationWidget() {
        LayoutContainer mainContainer = new LayoutContainer();
        FlexTable flexTable = new FlexTable();
        flexTable.setSize("304px", "100px");
        
        Text text = new Text("Login anonymously?");
        flexTable.getFlexCellFormatter().setColSpan(2, 0, 2);
        flexTable.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.setWidget(2, 0, text);
        
        final Button btnLogin = new Button("Login");
        btnLogin.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                authenticate();
            }
        });
        flexTable.setWidget(3, 0, btnLogin);
        btnLogin.setSize("50", "25");

        Button btnCancel = new Button("Cancel");
        btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (loginHandler != null)
                    loginHandler.onLoginCancel();
                EventPipeLine.getEventBus().fireEventFromSource(new LogoutEvent(authenticationModel.getFacilityName()),
                        authenticationModel.getFacilityName());
            }
        });

        flexTable.setWidget(3, 1, btnCancel);
        btnCancel.setSize("50", "25");
        flexTable.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_CENTER);

        mainContainer.add(flexTable);
        mainContainer.setHeight("100px");
        initComponent(mainContainer);
        setAutoHeight(true);
        
    }

    public void setAuthenticationModel(AuthenticationModel authenticationModel) {
        this.authenticationModel = authenticationModel;
    }

    public void setFacility(TFacility facility) {
        this.facility = facility;
    }

    public void setLoginHandler(LoginInterface loginHandler) {
        this.loginHandler = loginHandler;
    }

    public void authenticate() {
        if (facility != null && loginHandler != null) {
            Map<String, String> parameters = new HashMap<String, String>();
            loginHandler.onLoginOk(authenticationModel.getFacilityName(), authenticationModel.getType(), parameters);
        }
    }
}
